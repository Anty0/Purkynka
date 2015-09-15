package cz.anty.timetablemanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import java.io.IOException;

import cz.anty.timetablemanager.receiver.TimetableScheduleReceiver;
import cz.anty.utils.Log;
import cz.anty.utils.WrongLoginDataException;
import cz.anty.utils.listItem.MultilineAdapter;
import cz.anty.utils.listItem.TextMultilineItem;
import cz.anty.utils.settings.TimetableSettingsActivity;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;
import cz.anty.utils.timetable.Timetable;
import cz.anty.utils.timetable.TimetableConnector;
import cz.anty.utils.timetable.TimetableManager;

public class TimetableSelectActivity extends AppCompatActivity {

    private TimetableManager timetableManager;
    private OnceRunThreadWithSpinner worker;
    private MultilineAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TimetableSelectActivity", "onCreate");
        super.onCreate(savedInstanceState);
        sendBroadcast(new Intent(this, TimetableScheduleReceiver.class));
        setContentView(R.layout.activity_timetable_select);

        if (worker == null)
            worker = new OnceRunThreadWithSpinner(this);
        if (timetableManager == null)
            timetableManager = new TimetableManager(this);

        listView = ((ListView) findViewById(R.id.listView));
        adapter = new MultilineAdapter(this);
        listView.setAdapter(adapter);
    }

    private void initialize() {
        Log.d("TimetableSelectActivity", "initialize");
        final Timetable[] timetables = timetableManager.getTimetables();
        adapter.setNotifyOnChange(false);
        adapter.clear();
        for (Timetable timetable : timetables) {
            adapter.add(timetable);
        }
        adapter.add(new TextMultilineItem(getString(R.string
                .list_item_text_add_timetable), null));
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                //final String item = (String) parent.getItemAtPosition(position);
                if (position >= timetables.length) {

                    ScrollView mainScrollView = new ScrollView(TimetableSelectActivity.this);

                    final LinearLayout layout = new LinearLayout(TimetableSelectActivity.this);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    mainScrollView.addView(layout);

                    final EditText input = new EditText(TimetableSelectActivity.this);
                    layout.addView(input);

                    final CheckBox autoLoadCheckBox = new CheckBox(TimetableSelectActivity.this);
                    autoLoadCheckBox.setChecked(true);
                    autoLoadCheckBox.setText(R.string.check_box_text_auto_load);
                    layout.addView(autoLoadCheckBox);

                    new AlertDialog.Builder(TimetableSelectActivity.this)
                            .setTitle(R.string.dialog_title_new_timetable)
                                    //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon T
                            .setMessage(R.string.dialog_text_insert_timetable_name)
                            .setView(mainScrollView)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    worker.startWorker(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Timetable newTimetable = timetableManager.addTimetable(input.getText().toString());
                                                if (autoLoadCheckBox.isChecked())
                                                    TimetableConnector.tryLoadTimetable(newTimetable);
                                            } catch (Exception e) {
                                                Log.d("TimetableSelectActivity", "initialize", e);
                                                final String title, message;
                                                if (e instanceof WrongLoginDataException) {
                                                    title = getString(R.string.exception_title_name) + ": " + input.getText();
                                                    message = getString(R.string.exception_message_name);
                                                } else if (e instanceof IOException) {
                                                    title = getString(R.string.exception_title_connection);
                                                    message = getString(R.string.exception_message_connection);
                                                } else {
                                                    title = getString(R.string.dialog_title_timetable_still_exists);
                                                    message = getString(R.string.dialog_message_timetable_still_exists);
                                                }
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        new AlertDialog.Builder(TimetableSelectActivity.this)
                                                                .setTitle(title)
                                                                        //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon T
                                                                .setMessage(message)
                                                                .setPositiveButton(R.string.but_ok, null)
                                                                .setCancelable(true)
                                                                .show();
                                                    }
                                                });
                                            }
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    initialize();
                                                }
                                            });
                                        }
                                    }, getString(R.string.wait_text_loading));
                                }
                            })
                            .setNegativeButton(R.string.but_cancel, null)
                            .setCancelable(true)
                            .show();
                    return;
                }
                TimetableManageActivity.toShow = timetables[position];
                startActivity(new Intent(TimetableSelectActivity.this, TimetableManageActivity.class));
            }

        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= timetables.length) return false;
                final Timetable timetable = timetables[position];
                new AlertDialog.Builder(TimetableSelectActivity.this)
                        .setTitle(timetable.getName())
                                //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon T
                        .setMessage(R.string.dialog_message_what_to_do)
                        .setPositiveButton(R.string.but_rename, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final EditText input = new EditText(TimetableSelectActivity.this);
                                input.setText(timetable.getName());

                                new AlertDialog.Builder(TimetableSelectActivity.this)
                                        .setTitle(getString(R.string.but_rename) + ": " + timetable.getName())
                                                //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon T
                                        .setMessage(R.string.dialog_text_insert_timetable_name)
                                        .setView(input)
                                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                timetable.setName(input.getText().toString());
                                                initialize();
                                            }
                                        })
                                        .setNegativeButton(R.string.but_cancel, null)
                                        .setCancelable(true)
                                        .show();
                            }
                        })
                        .setNegativeButton(R.string.but_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                timetableManager.removeTimetable(timetable);
                                initialize();
                            }
                        })
                        .setNeutralButton(R.string.but_cancel, null)
                        .setCancelable(true)
                        .show();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_default, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, TimetableSettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialize();
    }
}
