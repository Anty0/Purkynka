package cz.anty.purkynkamanager.modules.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.attendance.receiver.TrackingReceiver;
import cz.anty.purkynkamanager.modules.attendance.receiver.TrackingScheduleReceiver;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.attendance.man.Man;
import cz.anty.purkynkamanager.utils.other.attendance.man.TrackingMansManager;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.RecyclerItemClickListener;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.base.RecyclerInflater;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThread;

public class TrackingActivity extends AppCompatActivity {

    private static final String LOG_TAG = "TrackingActivity";

    public static TrackingMansManager mansManager = null;
    private RecyclerInflater.RecyclerManager recyclerManager;
    private MultilineRecyclerAdapter<Man> adapter;
    private OnceRunThread worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_ITEM_UNLOCKED_BONUS, false)) {
            finish();
            return;
        }

        adapter = new MultilineRecyclerAdapter<>();
        recyclerManager = RecyclerInflater.inflateToActivity(this).inflate().setAdapter(adapter)
                .setItemTouchListener(new RecyclerItemClickListener.SimpleClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        final Man man = adapter.getItem(position);
                        if (man != null) {
                            mansManager.processMan(TrackingActivity.this, man, new Runnable() {
                                @Override
                                public void run() {
                                    sendBroadcast(new Intent(TrackingActivity.this,
                                            TrackingScheduleReceiver.class));
                                    update(false);
                                }
                            });
                        }
                    }
                }).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        update(true);
                    }
                });

        worker = new OnceRunThread(this);

        if (mansManager == null) {
            mansManager = new TrackingMansManager(this);
            update(true);
            return;
        }
        update(false);
    }

    private void update(final boolean refresh) {
        Log.d(LOG_TAG, "update");
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                recyclerManager.setRefreshing(true);
                if (refresh) TrackingReceiver.refreshTrackingMans
                        (TrackingActivity.this, mansManager, true);

                final Man[] data = mansManager.get();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clearItems();
                        adapter.addAllItems(data);
                    }
                });
                recyclerManager.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            update(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
