package cz.anty.attendancemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Arrays;

import cz.anty.attendancemanager.receiver.TrackingReceiver;
import cz.anty.attendancemanager.receiver.TrackingScheduleReceiver;
import cz.anty.utils.Log;
import cz.anty.utils.attendance.man.Man;
import cz.anty.utils.attendance.man.TrackingMansManager;
import cz.anty.utils.listItem.MultilineAdapter;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;

public class TrackingActivity extends AppCompatActivity {

    private TrackingMansManager mansManager;
    private MultilineAdapter<Man> adapter;
    private OnceRunThreadWithSpinner worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViewById(R.id.editText).setVisibility(View.GONE);
        ListView resultListView = ((ListView) findViewById(R.id.listView));
        adapter = new MultilineAdapter<>(this);
        mansManager = new TrackingMansManager(this);
        resultListView.setAdapter(adapter);
        resultListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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

        if (worker == null)
            worker = new OnceRunThreadWithSpinner(this);
        if (adapter.isEmpty())
            update();
    }

    private void update() {
        Log.d("TrackingActivity", "update");
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                TrackingReceiver.refreshTrackingMans(TrackingActivity.this, mansManager, true);
                Man[] data = mansManager.get();

                Log.d("SearchActivity", "update data: " + Arrays.toString(data));

                adapter.setNotifyOnChange(false);
                adapter.clear();
                for (Man item : data) {
                    adapter.add(item);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
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
