package cz.anty.purkynkamanager.modules.attendance;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.attendance.receiver.TrackingScheduleReceiver;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.attendance.AttendanceConnector;
import cz.anty.purkynkamanager.utils.other.attendance.man.Man;
import cz.anty.purkynkamanager.utils.other.attendance.man.Mans;
import cz.anty.purkynkamanager.utils.other.attendance.man.TrackingMansManager;
import cz.anty.purkynkamanager.utils.other.list.listView.MultilineItem;
import cz.anty.purkynkamanager.utils.other.list.listView.TextMultilineItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.AutoLoadMultilineRecyclerAdapter;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.RecyclerInflater;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.RecyclerItemClickListener;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThread;
import cz.anty.purkynkamanager.utils.settings.AttendanceSettingsActivity;

public class SearchActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SearchActivity";

    private final AttendanceConnector mConnector = new AttendanceConnector();
    private String mSearchText = "";
    private AutoLoadMultilineRecyclerAdapter mAdapter;
    private RecyclerInflater.RecyclerManager mRecyclerManager;
    private OnceRunThread worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new AutoLoadMultilineRecyclerAdapter(this,
                new AutoLoadMultilineRecyclerAdapter.OnLoadNextPageListener() {
                    @Override
                    public void onLoadNextPage(AutoLoadMultilineRecyclerAdapter multilineAdapter, int page) {
                        update(false, page);
                    }
                });

        mRecyclerManager = RecyclerInflater.inflateToActivity(this).inflate().setAdapter(mAdapter)
                .setItemTouchListener(new RecyclerItemClickListener.SimpleClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        if (!getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                                .getBoolean(Constants.SETTING_NAME_ITEM_UNLOCKED_BONUS, false))
                            return;

                        MultilineItem item = mAdapter.getItem(position);
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
                }).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        update(true, 1);
                    }
                });

        if (worker == null)
            worker = new OnceRunThread(this);

        if (!handleIntent(getIntent()))
            update(true, 1);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private boolean handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mSearchText = intent.getStringExtra(SearchManager.QUERY);
            update(true, 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        if (!getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_ITEM_UNLOCKED_BONUS, false)) {
            menu.findItem(R.id.action_show_tracked_people).setVisible(false);
        }

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /*if (id == R.id.action_search) {
            onSearchRequested();
            return true;
        }*/
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

    private void update(final boolean clearData, final int page) {
        Log.d(LOG_TAG, "update clearData: " + clearData + " page: " + page + " search: " + mSearchText);
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                if (clearData && worker.getWaitingThreadsLength() > 0) return;
                if (page <= 1) mRecyclerManager.setRefreshing(true);
                MultilineItem[] data;
                try {
                    List<Man> mans = Mans.parseMans(mConnector.getSupElements(mSearchText, page));
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
                        if (clearData) mAdapter.clearItems();
                        mAdapter.setAutoLoad(finalData.length != 0
                                && finalData[0] instanceof Man);

                        mAdapter.addAllItems(finalData);
                        if (clearData) mRecyclerManager
                                .getRecyclerView().scrollToPosition(0);
                        //mAdapter.notifyDataSetChanged();
                    }
                });
                if (page <= 1) mRecyclerManager.setRefreshing(false);
            }
        });
    }
}
