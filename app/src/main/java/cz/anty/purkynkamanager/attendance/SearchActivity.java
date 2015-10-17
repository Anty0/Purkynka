package cz.anty.purkynkamanager.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.attendance.receiver.TrackingScheduleReceiver;
import cz.anty.purkynkamanager.settings.AttendanceSettingsActivity;
import cz.anty.purkynkamanager.utils.Log;
import cz.anty.purkynkamanager.utils.attendance.AttendanceConnector;
import cz.anty.purkynkamanager.utils.attendance.man.Man;
import cz.anty.purkynkamanager.utils.attendance.man.Mans;
import cz.anty.purkynkamanager.utils.attendance.man.TrackingMansManager;
import cz.anty.purkynkamanager.utils.list.listView.MultilineItem;
import cz.anty.purkynkamanager.utils.list.listView.TextMultilineItem;
import cz.anty.purkynkamanager.utils.list.recyclerView.AutoLoadMultilineRecyclerAdapter;
import cz.anty.purkynkamanager.utils.list.recyclerView.RecyclerInflater;
import cz.anty.purkynkamanager.utils.list.recyclerView.RecyclerItemClickListener;
import cz.anty.purkynkamanager.utils.thread.OnceRunThread;

public class SearchActivity extends AppCompatActivity {

    public static final String EXTRA_SEARCH = "EXTRA_SEARCH";
    private static final String LOG_TAG = "SearchActivity";

    private final AttendanceConnector connector = new AttendanceConnector();
    private EditText searchEditText;
    private AutoLoadMultilineRecyclerAdapter adapter;
    private RecyclerView recyclerView;
    private OnceRunThread worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new AutoLoadMultilineRecyclerAdapter(this,
                new AutoLoadMultilineRecyclerAdapter.OnLoadNextPageListener() {
                    @Override
                    public void onLoadNextPage(AutoLoadMultilineRecyclerAdapter multilineAdapter, int page) {
                        update(false, page, null);
                    }
                });
        //adapter.setNotifyOnChange(false);
        recyclerView = RecyclerInflater.inflateToActivity(this, R.layout.activity_search, adapter,
                new RecyclerItemClickListener.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        MultilineItem item = adapter.getItem(position);
                        final Man man = item instanceof Man
                                ? (Man) item : null;
                        if (man != null) {
                            if (TrackingActivity.mansManager == null)
                                TrackingActivity.mansManager =
                                        new TrackingMansManager(SearchActivity.this);
                            TrackingActivity.mansManager.processMan(SearchActivity.this, man, new Runnable() {
                                @Override
                                public void run() {
                                    sendBroadcast(new Intent(SearchActivity.this,
                                            TrackingScheduleReceiver.class));
                                }
                            });
                        }
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                });
        searchEditText = (EditText) findViewById(R.id.editText);

        String search = getIntent().getStringExtra(EXTRA_SEARCH);
        if (search != null) {
            searchEditText.setText(search);
        }

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                update(true, 1, s.toString());
            }
        });

        if (worker == null)
            worker = new OnceRunThread(this);

        update(true, 1, search);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, AttendanceSettingsActivity.class));
            return true;
        }
        if (id == R.id.action_show_tracked_people) {
            startActivity(new Intent(this, TrackingActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void update(final boolean clearData, final int page, @Nullable String search) {
        Log.d(LOG_TAG, "update clearData: " + clearData + " page: " + page + " search: " + search);
        final String toSearch = search == null ? searchEditText.getText().toString() : search;
        Log.d(LOG_TAG, "update toSearch: " + toSearch);
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                if (clearData && worker.getWaitingThreadsLength() > 0) return;
                MultilineItem[] data;
                try {
                    List<Man> mans = Mans.parseMans(connector.getSupElements(toSearch, page));
                    data = mans.toArray(new MultilineItem[mans.size()]);
                } catch (IOException e) {
                    Log.d(LOG_TAG, "update", e);
                    //values = new String[]{"Connection exception: " + e.getMessage() + "\n" + "Check your internet connection"};
                    data = new MultilineItem[]{new TextMultilineItem(getText(R.string.list_item_title_connection_exception),
                            getText(R.string.list_item_text_connection_exception) + ": " + e.getMessage())/*,
                            new TextMultilineItem(getString(R.string.to_page) + " -> " + (page + 1), getString(R.string.on_page) + ": " + page)*/};
                }
                Log.d(LOG_TAG, "update data: " + Arrays.toString(data));

                final MultilineItem[] finalData = data;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (clearData) adapter.clearItems();
                        adapter.setAutoLoad(finalData.length != 0
                                && finalData[0] instanceof Man);

                        boolean moveToFirst = adapter.getItemCount() == 0;
                        adapter.addAllItems(finalData);
                        if (moveToFirst) recyclerView.scrollToPosition(0);
                        //adapter.notifyDataSetChanged();
                    }
                });

            }
        });
    }
}
