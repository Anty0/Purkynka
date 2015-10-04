package cz.anty.timetablemanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.IOException;

import cz.anty.timetablemanager.receiver.TimetableScheduleReceiver;
import cz.anty.timetablemanager.widget.TimetableLessonWidget;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.WrongLoginDataException;
import cz.anty.utils.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerItemClickListener;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;
import cz.anty.utils.timetable.Timetable;
import cz.anty.utils.timetable.TimetableConnector;
import cz.anty.utils.timetable.TimetableManager;

public class TimetableSelectActivity extends AppCompatActivity {

    static TimetableManager timetableManager;
    private OnceRunThreadWithSpinner worker;
    private MultilineRecyclerAdapter<Timetable> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(getClass().getSimpleName(), "onCreate");
        super.onCreate(savedInstanceState);
        TimetableLessonWidget.callUpdate(this);
        sendBroadcast(new Intent(this, TimetableScheduleReceiver.class));

        if (worker == null)
            worker = new OnceRunThreadWithSpinner(this);
        if (timetableManager == null)
            timetableManager = new TimetableManager(this);

        adapter = new MultilineRecyclerAdapter<>();
        RecyclerAdapter.inflateToActivity(this, null, adapter,
                new RecyclerItemClickListener.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        TimetableManageActivity.toShow = adapter.getItem(position);
                        startActivity(new Intent(TimetableSelectActivity.this, TimetableManageActivity.class));
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        final Timetable timetable = adapter.getItem(position);
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
                                                .setMessage(R.string.dialog_message_insert_timetable_name)
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

                    }
                });
        initialize();
    }

    private void initialize() {
        Log.d(getClass().getSimpleName(), "initialize");
        final Timetable[] timetables = timetableManager.getTimetables();
        adapter.clearItems();
        adapter.addAllItems(timetables);
    }

    private void addTimetable() { //TODO better adding system, with alert about widget
        ScrollView mainScrollView = new ScrollView(this);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        mainScrollView.addView(layout);

        final EditText input = new EditText(this);
        layout.addView(input);

        final CheckBox autoLoadCheckBox = new CheckBox(this);
        autoLoadCheckBox.setChecked(true);
        autoLoadCheckBox.setText(R.string.check_box_text_auto_load);
        layout.addView(autoLoadCheckBox);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_new_timetable)
                        //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon T
                .setMessage(R.string.dialog_message_insert_timetable_name)
                .setView(mainScrollView)
                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        worker.startWorker(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Timetable newTimetable = timetableManager
                                            .addTimetable(input.getText().toString());
                                    if (autoLoadCheckBox.isChecked())
                                        TimetableConnector.tryLoadTimetable(newTimetable);
                                } catch (Exception e) {
                                    Log.d(TimetableSelectActivity.this.getClass().getSimpleName(), "initialize", e);
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
                                        final SharedPreferences preferences = getSharedPreferences(
                                                Constants.SETTINGS_NAME_TIMETABLES, Context.MODE_PRIVATE);
                                        if (preferences.getBoolean(Constants.SETTING_NAME_FIRST_START, true)) {
                                            new AlertDialog.Builder(TimetableSelectActivity.this)
                                                    .setTitle(R.string.dialog_title_timetable_widget_alert)
                                                            //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon T
                                                    .setMessage(R.string.dialog_message_timetable_widget_alert)
                                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                            preferences.edit()
                                                                    .putBoolean(Constants.SETTING_NAME_FIRST_START, false)
                                                                    .apply();
                                                        }
                                                    })
                                                    .setNegativeButton(R.string.but_later, null)
                                                    .setCancelable(false)
                                                    .show();
                                        }
                                    }
                                });
                            }
                        }, getString(R.string.wait_text_loading));
                    }
                })
                .setNegativeButton(R.string.but_cancel, null)
                .setCancelable(true)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timetable_select, menu);
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
        if (id == R.id.action_add) {
            addTimetable();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
