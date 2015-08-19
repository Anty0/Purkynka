package cz.anty.attendancemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cz.anty.attendancemanager.receiver.TrackingScheduleReceiver;
import cz.anty.utils.AppDataManager;
import cz.anty.utils.attendance.AttendanceConnector;
import cz.anty.utils.attendance.man.Man;
import cz.anty.utils.attendance.man.Mans;
import cz.anty.utils.attendance.man.TrackingMansManager;
import cz.anty.utils.listItem.AutoLoadMultilineAdapter;
import cz.anty.utils.listItem.MultilineItem;
import cz.anty.utils.listItem.TextMultilineItem;
import cz.anty.utils.settings.AttendanceSettingsActivity;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;

public class SearchActivity extends AppCompatActivity {

    public static final String EXTRA_SEARCH = "EXTRA_SEARCH";

    private final AttendanceConnector connector = new AttendanceConnector();
    private EditText searchEditText;
    private AutoLoadMultilineAdapter adapter;
    private OnceRunThreadWithSpinner worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchEditText = (EditText) findViewById(R.id.editText);
        if (savedInstanceState != null) {
            String search = savedInstanceState.getString(EXTRA_SEARCH);
            if (search != null) {
                searchEditText.setText(search);
                update(true, true, 1, search);
            }
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
                update(false, true, 1, s.toString());
            }
        });
        ListView resultListView = ((ListView) findViewById(R.id.listView));
        adapter = new AutoLoadMultilineAdapter(this, R.layout.text_multi_line_list_item,
                new AutoLoadMultilineAdapter.OnLoadNextListListener() {
                    @Override
                    public void onLoadNextList(AutoLoadMultilineAdapter multilineAdapter, int page) {
                        update(false, false, page, null);
                    }
                });
        resultListView.setAdapter(adapter);
        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MultilineItem item = adapter.getItem(position);
                final Man man = item instanceof Man
                        ? (Man) item : null;
                if (man != null) {
                    new TrackingMansManager(SearchActivity.this).processMan(man, new Runnable() {
                        @Override
                        public void run() {
                            sendBroadcast(new Intent(SearchActivity.this, TrackingScheduleReceiver.class));
                        }
                    });
                }
            }
        });

        if (worker == null)
            worker = new OnceRunThreadWithSpinner(this);
        if (adapter.isEmpty())
            update(false, true, 1, null);
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

    private void update(final boolean showMessage, final boolean clearData, final int page, @Nullable String search) {
        if (AppDataManager.isDebugMode(this)) Log.d("SearchActivity",
                "update showMessage: " + showMessage + " clearData: " + clearData + " page: " + page + " search: " + search);
        final String toSearch = search == null ? searchEditText.getText().toString() : search;
        if (AppDataManager.isDebugMode(this))
            Log.d("SearchActivity", "update toSearch: " + toSearch);
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                if (!showMessage && clearData && worker.getWaitingThreadsLength() > 0) return;
                //String[] values;
                MultilineItem[] data;
                try {
                    List<Man> mans = Mans.parseMans(connector.getSupElements(toSearch, page));
                    data = mans.toArray(new MultilineItem[mans.size()/* + 1*/]);
                    //data[data.length - 1] = new TextMultilineItem(getString(R.string.to_page) + " -> " + (page + 1), getString(R.string.on_page) + ": " + page);
                    /*values = new String[mans.size()];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = mans.get(i).toString();
                    }*/
                } catch (IOException e) {
                    if (AppDataManager.isDebugMode(SearchActivity.this))
                        Log.d("SearchActivity", "update", e);
                    //values = new String[]{"Connection exception: " + e.getMessage() + "\n" + "Check your internet connection"};
                    data = new MultilineItem[]{new TextMultilineItem(getString(R.string.list_item_title_connection_exception),
                            getString(R.string.list_item_text_connection_exception) + ": " + e.getMessage())/*,
                            new TextMultilineItem(getString(R.string.to_page) + " -> " + (page + 1), getString(R.string.on_page) + ": " + page)*/};
                }
                if (AppDataManager.isDebugMode(SearchActivity.this))
                    Log.d("SearchActivity", "update data: " + Arrays.toString(data));

                /*final ArrayList<String> list = new ArrayList<>();
                Collections.addAll(list, values);
                final StableArrayAdapter adapter = new StableArrayAdapter(SearchActivity.this,
                        android.R.layout.simple_list_item_1, list);*/

                adapter.setNotifyOnChange(false);
                if (clearData) adapter.clear();
                adapter.setAutoLoad(data.length != 0
                        && data[0] instanceof Man);
                for (MultilineItem item : data) {
                    adapter.add(item);
                }
                //final MultilineItem[] finalData = data;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });

            }
        }, showMessage ? getString(R.string.wait_text_searching) : null);
    }
}
