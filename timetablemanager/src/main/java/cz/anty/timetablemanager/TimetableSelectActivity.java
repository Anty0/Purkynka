package cz.anty.timetablemanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import cz.anty.attendancemanager.ScheduleReceiver;
import cz.anty.utils.WrongLoginDataException;
import cz.anty.utils.listItem.StableArrayAdapter;
import cz.anty.utils.settings.TimetableSettingsActivity;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;
import cz.anty.utils.timetable.Timetable;
import cz.anty.utils.timetable.TimetableConnector;
import cz.anty.utils.timetable.TimetableManager;

public class TimetableSelectActivity extends AppCompatActivity {

    private TimetableManager timetableManager;
    private OnceRunThreadWithSpinner worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_select);
        ((CheckBox) findViewById(R.id.checkBox))
                .setChecked(getSharedPreferences("AttendanceData", MODE_PRIVATE)
                        .getBoolean("DISPLAY_WARNING", false));

        if (worker == null)
            worker = new OnceRunThreadWithSpinner(this);
        if (timetableManager == null)
            timetableManager = new TimetableManager(this);
        initialize();
    }

    private void initialize() {
        ListView listView = ((ListView) findViewById(R.id.listView));
        final Timetable[] timetables = timetableManager.getTimetables();
        String[] values = new String[timetables.length + 1];
        for (int i = 0; i < timetables.length; i++) {
            values[i] = timetables[i].toString();
        }
        values[timetables.length] = getString(R.string.but_list_add_timetable);

        final ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, values);
        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                //final String item = (String) parent.getItemAtPosition(position);
                if (position >= timetables.length) {
                    final LinearLayout layout = new LinearLayout(TimetableSelectActivity.this);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    final EditText input = new EditText(TimetableSelectActivity.this);
                    layout.addView(input);

                    final CheckBox autoLoadCheckBox = new CheckBox(TimetableSelectActivity.this);
                    autoLoadCheckBox.setChecked(true);
                    autoLoadCheckBox.setText(R.string.check_box_auto_load);
                    layout.addView(autoLoadCheckBox);

                    new AlertDialog.Builder(TimetableSelectActivity.this)
                            .setTitle(R.string.alert_new_timetable)
                                    //TODO add set icon with icon "T"
                            .setMessage(R.string.alert_timetable_name)
                            .setView(layout)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    worker.startWorker(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Timetable newTimetable = timetableManager.addTimetable(input.getText().toString());
                                                if (autoLoadCheckBox.isChecked())
                                                    TimetableConnector.tryLoadTimetable(newTimetable);
                                            } catch (WrongLoginDataException e) {
                                                Log.d(null, null, e);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        new AlertDialog.Builder(TimetableSelectActivity.this)
                                                                .setTitle(getString(R.string.exception_name_title) + ": " + input.getText())
                                                                        //TODO add set icon with icon "T"
                                                                .setMessage(R.string.exception_name_message)
                                                                .setPositiveButton(R.string.but_ok, null)
                                                                .setCancelable(true)
                                                                .show();
                                                    }
                                                });
                                            } catch (IOException e) {
                                                Log.d(null, null, e);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        new AlertDialog.Builder(TimetableSelectActivity.this)
                                                                .setTitle(R.string.exception_connection_title)
                                                                        //TODO add set icon with icon "T"
                                                                .setMessage(R.string.exception_connection_message)
                                                                .setPositiveButton(R.string.but_ok, null)
                                                                .setCancelable(true)
                                                                .show();
                                                    }
                                                });
                                            } catch (Exception e) {
                                                Log.d(null, null, e);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        new AlertDialog.Builder(TimetableSelectActivity.this)
                                                                .setTitle(R.string.title_timetable_still_exists)
                                                                        //TODO add set icon with icon "T"
                                                                .setMessage(R.string.message_timetable_still_exists)
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
                                    }, getString(R.string.loading));
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
                                //TODO add set icon with icon "T"
                        .setMessage(R.string.message_what_to_do)
                        .setPositiveButton(R.string.but_rename, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final EditText input = new EditText(TimetableSelectActivity.this);
                                input.setText(timetable.getName());

                                new AlertDialog.Builder(TimetableSelectActivity.this)
                                        .setTitle(getString(R.string.but_rename) + ": " + timetable.getName())
                                                //TODO add set icon with icon "T"
                                        .setMessage(R.string.alert_timetable_name)
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

    public void onCheckBoxClick(View view) {
        getSharedPreferences("AttendanceData", MODE_PRIVATE).edit()
                .putBoolean("DISPLAY_WARNING", ((CheckBox) view).isChecked())
                .apply();
        sendBroadcast(new Intent(this, ScheduleReceiver.class));
    }
}
