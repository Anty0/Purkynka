package cz.anty.attendancemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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

    public static TrackingMansManager mansManager = null;
    private MultilineRecyclerAdapter<Man> adapter;
    private OnceRunThreadWithSpinner worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new MultilineRecyclerAdapter<>();
        RecyclerAdapter.inflateToActivity(this, null, adapter,
                new RecyclerItemClickListener.ClickListener() {
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

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                });

        worker = new OnceRunThreadWithSpinner(this);

        if (mansManager == null) {
            mansManager = new TrackingMansManager(this);
            update(true);
            return;
        }
        update(false);
    }

    private void update(final boolean refresh) {
        Log.d("TrackingActivity", "update");
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                if (refresh) TrackingReceiver
                        .refreshTrackingMans(TrackingActivity.this, mansManager, true);

                final Man[] data = mansManager.get();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clearItems();
                        adapter.addAllItems(data);
                    }
                });

            }
        }, getText(R.string.wait_text_loading));
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
            update(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
