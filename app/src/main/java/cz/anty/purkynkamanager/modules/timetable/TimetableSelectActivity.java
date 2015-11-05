package cz.anty.purkynkamanager.modules.timetable;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatSpinner;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;

import java.io.IOException;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.timetable.receiver.TimetableScheduleReceiver;
import cz.anty.purkynkamanager.modules.timetable.widget.TimetableLessonWidget;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.list.items.TextMultilineItem;
import cz.anty.purkynkamanager.utils.other.list.listView.MultilineAdapter;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.RecyclerItemClickListener;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.base.RecyclerInflater;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThreadWithSpinner;
import cz.anty.purkynkamanager.utils.other.timetable.Timetable;
import cz.anty.purkynkamanager.utils.other.timetable.TimetableConnector;
import cz.anty.purkynkamanager.utils.other.timetable.TimetableManager;
import cz.anty.purkynkamanager.utils.settings.TimetableSettingsActivity;

public class TimetableSelectActivity extends AppCompatActivity {

    public static final String EXTRA_SHOW_ADD_TIMETABLE_DIALOG = "ADD_TIMETABLE";
    private static final String LOG_TAG = "TimetableSelectActivity";
    public static TimetableManager timetableManager;
    private OnceRunThreadWithSpinner worker;
    private MultilineRecyclerAdapter<Timetable> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        TimetableLessonWidget.callUpdate(this);
        sendBroadcast(new Intent(this, TimetableScheduleReceiver.class));

        if (worker == null)
            worker = new OnceRunThreadWithSpinner(this);
        if (timetableManager == null)
            timetableManager = new TimetableManager(this);

        adapter = new MultilineRecyclerAdapter<>();
        RecyclerInflater.inflateToActivity(this).useSwipeRefresh(false).inflate().setAdapter(adapter)
                .setItemTouchListener(new RecyclerItemClickListener.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        startActivity(new Intent(TimetableSelectActivity.this, TimetableManageActivity.class)
                                .putExtra(TimetableManageActivity.EXTRA_TIMETABLE_NAME, adapter.getItem(position).getName()));
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        final Timetable timetable = adapter.getItem(position);
                        new AlertDialog.Builder(TimetableSelectActivity.this, R.style.AppTheme_Dialog_T)
                                .setTitle(timetable.getName())
                                .setIcon(R.mipmap.ic_launcher_t)
                                .setMessage(R.string.dialog_message_what_to_do)
                                .setPositiveButton(R.string.but_rename, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final EditText input = new AppCompatEditText(TimetableSelectActivity.this);
                                        input.setText(timetable.getName());

                                        new AlertDialog.Builder(TimetableSelectActivity.this, R.style.AppTheme_Dialog_T)
                                                .setTitle(getText(R.string.but_rename) + ": " + timetable.getName())
                                                .setIcon(R.mipmap.ic_launcher_t)
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

        if (getIntent().getBooleanExtra(EXTRA_SHOW_ADD_TIMETABLE_DIALOG, false)) {
            getIntent().putExtra(EXTRA_SHOW_ADD_TIMETABLE_DIALOG, false);
            addTimetable();
        }
    }

    private void initialize() {
        Log.d(LOG_TAG, "initialize");
        final Timetable[] timetables = timetableManager.getTimetables();
        adapter.clearItems();
        adapter.addAllItems(timetables);
    }

    private void addTimetable() {
        ScrollView mainScrollView = new ScrollView(this);
        final RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(LinearLayout.VERTICAL);
        mainScrollView.addView(radioGroup);

        RadioButton radioButtonDownload = new AppCompatRadioButton(this);
        radioButtonDownload.setId(R.id.text_view_title);
        radioButtonDownload.setText(R.string.radio_button_text_download_timetable);
        radioGroup.addView(radioButtonDownload);

        RadioButton radioButtonCreate = new AppCompatRadioButton(this);
        radioButtonCreate.setId(R.id.text_view_text);
        radioButtonCreate.setText(R.string.radio_button_text_create_free_timetable);
        radioGroup.addView(radioButtonCreate);

        radioGroup.check(R.id.text_view_title);

        new AlertDialog.Builder(this, R.style.AppTheme_Dialog_T)
                .setTitle(R.string.dialog_title_new_timetable)
                .setIcon(R.mipmap.ic_launcher_t)
                .setView(mainScrollView)
                .setPositiveButton(R.string.but_next, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (radioGroup.getCheckedRadioButtonId() == R.id.text_view_title) {
                            worker.startWorker(new Runnable() {
                                @Override
                                public void run() {
                                    final String[] classes;
                                    try {
                                        classes = TimetableConnector.getClasses();
                                    } catch (IOException e) {
                                        Log.d(LOG_TAG, "addTimetable", e);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                new AlertDialog.Builder(TimetableSelectActivity.this, R.style.AppTheme_Dialog_T)
                                                        .setTitle(R.string.exception_title_connection)
                                                        .setIcon(R.mipmap.ic_launcher_t)
                                                        .setMessage(R.string.exception_message_connection)
                                                        .setPositiveButton(R.string.but_ok, null)
                                                        .setCancelable(true)
                                                        .show();
                                            }
                                        });
                                        return;
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            MultilineAdapter<TextMultilineItem> adapter =
                                                    new MultilineAdapter<>(TimetableSelectActivity.this,
                                                            R.layout.base_list_item_multi_line_text);
                                            adapter.setNotifyOnChange(false);
                                            adapter.clear();
                                            for (String className : classes) {
                                                adapter.add(new TextMultilineItem(className, null).setTag(className));
                                            }
                                            adapter.notifyDataSetChanged();

                                            final Spinner spinner = new AppCompatSpinner(TimetableSelectActivity.this);
                                            Utils.setPadding(spinner, 15, 2, 15, 2);
                                            spinner.setAdapter(adapter);


                                            new AlertDialog.Builder(TimetableSelectActivity.this, R.style.AppTheme_Dialog_T)
                                                    .setTitle(R.string.dialog_title_new_timetable)
                                                    .setIcon(R.mipmap.ic_launcher_t)
                                                    .setMessage(R.string.dialog_message_select_your_class_name)
                                                    .setView(spinner)
                                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                            worker.startWorker(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    try {
                                                                        Object item = spinner.getSelectedItem();
                                                                        TextMultilineItem multilineItem = item instanceof TextMultilineItem
                                                                                ? (TextMultilineItem) item : null;
                                                                        if (multilineItem != null) {
                                                                            Timetable timetable = timetableManager
                                                                                    .addTimetable(TimetableSelectActivity.this,
                                                                                            multilineItem.getTag().toString());
                                                                            TimetableConnector.tryLoadTimetable(timetable);
                                                                        }
                                                                    } catch (IllegalArgumentException | IOException e) {
                                                                        Log.d(LOG_TAG, "initialize", e);

                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                boolean connection = e instanceof IOException;
                                                                                new AlertDialog.Builder(TimetableSelectActivity.this, R.style.AppTheme_Dialog_T)
                                                                                        .setTitle(connection ? R.string.exception_title_connection
                                                                                                : R.string.dialog_title_timetable_still_exists)
                                                                                        .setIcon(R.mipmap.ic_launcher_t)
                                                                                        .setMessage(connection ? R.string.exception_message_connection
                                                                                                : R.string.dialog_message_timetable_still_exists)
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
                                                            }, getText(R.string.wait_text_loading));
                                                        }
                                                    })
                                                    .setNegativeButton(R.string.but_cancel, null)
                                                    .setCancelable(true)
                                                    .show();
                                        }
                                    });
                                }
                            }, getText(R.string.wait_text_loading));
                            return;
                        }

                        final EditText input = new AppCompatEditText(TimetableSelectActivity.this);
                        Utils.setPadding(input, 15, 2, 15, 2);

                        new AlertDialog.Builder(TimetableSelectActivity.this, R.style.AppTheme_Dialog_T)
                                .setTitle(R.string.dialog_title_new_timetable)
                                .setIcon(R.mipmap.ic_launcher_t)
                                .setMessage(R.string.dialog_message_insert_timetable_name)
                                .setView(input)
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        worker.startWorker(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    timetableManager.addTimetable(TimetableSelectActivity.this,
                                                            input.getText().toString());
                                                } catch (IllegalArgumentException e) {
                                                    Log.d(LOG_TAG, "initialize", e);

                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            new AlertDialog.Builder(TimetableSelectActivity.this, R.style.AppTheme_Dialog_T)
                                                                    .setTitle(R.string.dialog_title_timetable_still_exists)
                                                                    .setIcon(R.mipmap.ic_launcher_t)
                                                                    .setMessage(R.string.dialog_message_timetable_still_exists)
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
                                        }, getText(R.string.wait_text_loading));
                                    }
                                })
                                .setNegativeButton(R.string.but_cancel, null)
                                .setCancelable(true)
                                .show();
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
