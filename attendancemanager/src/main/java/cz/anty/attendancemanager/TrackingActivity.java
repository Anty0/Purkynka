package cz.anty.attendancemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Arrays;

import cz.anty.attendancemanager.receiver.TrackingReceiver;
import cz.anty.attendancemanager.receiver.TrackingScheduleReceiver;
import cz.anty.utils.Log;
import cz.anty.utils.attendance.man.Man;
import cz.anty.utils.attendance.man.TrackingMansManager;
import cz.anty.utils.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerItemClickListener;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;

public class TrackingActivity extends AppCompatActivity {

    private TrackingMansManager mansManager;
    private MultilineRecyclerAdapter<Man> adapter;
    private OnceRunThreadWithSpinner worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mansManager = new TrackingMansManager(this);
        adapter = new MultilineRecyclerAdapter<>();
        RecyclerAdapter.inflateToActivity(this, null, adapter,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        final Man man = adapter.getItem(position);
                        if (man != null) {
                            mansManager.processMan(man, new Runnable() {
                                @Override
                                public void run() {
                                    sendBroadcast(new Intent(TrackingActivity.this,
                                            TrackingScheduleReceiver.class));
                                }
                            });
                        }
                    }
                });

        worker = new OnceRunThreadWithSpinner(this);
        update();
    }

    private void update() {
        Log.d("TrackingActivity", "update");
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                TrackingReceiver.refreshTrackingMans(TrackingActivity.this, mansManager, true);
                final Man[] data = mansManager.get();

                Log.d("SearchActivity", "update data: " + Arrays.toString(data));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clearItems();
                        adapter.addAllItems(data);
                    }
                });

            }
        }, getString(R.string.wait_text_loading));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tracking, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            update();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
