package cz.anty.attendancemanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Arrays;

import cz.anty.attendancemanager.receiver.TrackingReceiver;
import cz.anty.attendancemanager.receiver.TrackingScheduleReceiver;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.attendance.man.Man;
import cz.anty.utils.attendance.man.TrackingMansManager;
import cz.anty.utils.listItem.MultilineAdapter;
import cz.anty.utils.listItem.MultilineItem;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;

public class TrackingActivity extends AppCompatActivity {

    private TrackingMansManager mansManager;
    private MultilineAdapter adapter;
    private OnceRunThreadWithSpinner worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViewById(R.id.editText).setVisibility(View.GONE);
        ListView resultListView = ((ListView) findViewById(R.id.listView));
        adapter = new MultilineAdapter(this, R.layout.text_multi_line_list_item);
        mansManager = new TrackingMansManager(this);
        resultListView.setAdapter(adapter);
        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Man man = adapter.getItem(position) instanceof Man
                        ? (Man) adapter.getItem(position) : null;
                if (man != null) {
                    if (mansManager.contains(man)) {
                        new AlertDialog.Builder(TrackingActivity.this)
                                .setTitle(man.getName())
                                        //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon A
                                .setMessage(getString(R.string.dialog_text_attendance_stop_tracking)
                                        .replace(Constants.STRINGS_CONST_NAME, man.getName()))
                                .setPositiveButton(R.string.but_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mansManager.remove(man).apply();
                                        update();
                                        sendBroadcast(new Intent(TrackingActivity.this, TrackingScheduleReceiver.class));
                                    }
                                })
                                .setNegativeButton(R.string.but_no, null)
                                .setCancelable(true)
                                .show();
                    } else {
                        new AlertDialog.Builder(TrackingActivity.this)
                                .setTitle(man.getName())
                                        //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon A
                                .setMessage(getString(R.string.dialog_text_attendance_tracking)
                                        .replace(Constants.STRINGS_CONST_NAME, man.getName()))
                                .setPositiveButton(R.string.but_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new AlertDialog.Builder(TrackingActivity.this)
                                                .setTitle(R.string.dialog_title_terms_warning)
                                                        //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon A
                                                .setMessage(getString(R.string.dialog_text_terms_attendance_tracking)
                                                        .replace(Constants.STRINGS_CONST_NAME, man.getName()))
                                                .setPositiveButton(R.string.but_accept, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        mansManager.add(man).apply();
                                                        update();
                                                        sendBroadcast(new Intent(TrackingActivity.this, TrackingScheduleReceiver.class));
                                                    }
                                                })
                                                .setNegativeButton(R.string.but_cancel, null)
                                                .setCancelable(true)
                                                .show();
                                    }
                                })
                                .setNegativeButton(R.string.but_no, null)
                                .setCancelable(true)
                                .show();
                    }
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
                //String[] values;

                TrackingReceiver.refreshTrackingMans(TrackingActivity.this, mansManager, true);
                MultilineItem[] data = mansManager.get();
                //data[data.length - 1] = new TextMultilineItem(getString(R.string.to_page) + " -> " + (page + 1), getString(R.string.on_page) + ": " + page);
                    /*values = new String[mans.size()];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = mans.get(i).toString();
                    }*/

                Log.d("SearchActivity", "update data: " + Arrays.toString(data));

                /*final ArrayList<String> list = new ArrayList<>();
                Collections.addAll(list, values);
                final StableArrayAdapter adapter = new StableArrayAdapter(SearchActivity.this,
                        android.R.layout.simple_list_item_1, list);*/

                adapter.setNotifyOnChange(false);
                adapter.clear();
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
