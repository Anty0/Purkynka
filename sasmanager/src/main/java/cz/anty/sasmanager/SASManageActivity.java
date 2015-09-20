package cz.anty.sasmanager;

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

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Log;
import cz.anty.utils.ServiceManager;
import cz.anty.utils.listItem.MultilineAdapter;
import cz.anty.utils.listItem.MultilineItem;
import cz.anty.utils.listItem.TextMultilineItem;
import cz.anty.utils.sas.mark.Lesson;
import cz.anty.utils.sas.mark.Mark;
import cz.anty.utils.sas.mark.MarksManager;
import cz.anty.utils.settings.SASManagerSettingsActivity;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;

public class SASManageActivity extends AppCompatActivity {

    private ListView listView;
    private MultilineAdapter<MultilineItem> adapter;
    private OnceRunThreadWithSpinner refreshThread;
    private MarksShort marksShort = MarksShort.DATE;
    private MarksManager.Semester semester = MarksManager.Semester.AUTO.getStableSemester();
    private SASManagerService.SASBinder binder = null;
    private ServiceManager.BinderConnection<SASManagerService.SASBinder> binderConnection
            = new ServiceManager.BinderConnection<SASManagerService.SASBinder>() {
        @Override
        public void onBinderConnected(final SASManagerService.SASBinder sasBinder) {
            Log.d("SASManageActivity", "onBinderConnected");
            binder = sasBinder;
            refreshThread.startWorker(new Runnable() {
                @Override
                public void run() {
                    sasBinder.setOnStateChangedListener(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onStateChanged();
                                }
                            });
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onStateChanged();
                        }
                    });

                    sasBinder.setOnMarksChangeListener(new Runnable() {
                        @Override
                        public void run() {
                            onUpdate(false);
                        }
                    });
                    onUpdate(true);
                }
            }, getString(R.string.wait_text_loading));
        }

        @Override
        public void onBinderDisconnected() {
            Log.d("SASManageActivity", "onBinderDisconnected");
            try {
                refreshThread.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d("SASManageActivity", "onBinderDisconnected", e);
            }
            binder.setOnMarksChangeListener(null);
            binder.setOnStateChangedListener(null);
            binder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("SASManageActivity", "onCreate");
        super.onCreate(savedInstanceState);

        if (SASSplashActivity.serviceManager == null
                || !SASSplashActivity.serviceManager.isConnected()) {
            startActivity(new Intent(this, SASSplashActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_list);

        if (refreshThread == null)
            refreshThread = new OnceRunThreadWithSpinner(this);

        listView = ((ListView) findViewById(R.id.listView));
        adapter = new MultilineAdapter<>(this);
        listView.setAdapter(adapter);

        if (SASSplashActivity.serviceManager != null) {
            SASSplashActivity.serviceManager
                    .addBinderConnection(binderConnection);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("SASManageActivity", "onDestroy");
        if (SASSplashActivity.serviceManager != null) {
            SASSplashActivity.serviceManager
                    .removeBinderConnection(binderConnection);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("SASManageActivity", "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage, menu);

        switch (semester) {
            case FIRST:
                menu.findItem(R.id.action_semester_first).setChecked(true);
                break;
            case SECOND:
                menu.findItem(R.id.action_semester_second).setChecked(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("SASManageActivity", "onOptionsItemSelected");
        int i = item.getItemId();

        if (i == R.id.action_settings) {
            startActivity(new Intent(this, SASManagerSettingsActivity.class));
            return true;
        } else if (i == R.id.action_refresh) {
            if (binder != null) {
                binder.refresh();
                onUpdate(true);
            }
            return true;
        } else if (i == R.id.action_log_out) {
            logOut();
            return true;
        } else if (i == R.id.action_sort_date) {
            marksShort = MarksShort.DATE;
            item.setChecked(true);
            onUpdate(true);
            return true;
        } else if (i == R.id.action_sort_lesson) {
            marksShort = MarksShort.LESSONS;
            item.setChecked(true);
            onUpdate(true);
            return true;
        } else if (i == R.id.action_semester_first) {
            semester = MarksManager.Semester.FIRST;
            item.setChecked(true);
            onUpdate(true);
            return true;
        } else if (i == R.id.action_semester_second) {
            semester = MarksManager.Semester.SECOND;
            item.setChecked(true);
            onUpdate(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onStateChanged() {
        Log.d("SASManageActivity", "onStateChanged");
        if (binder == null) return;

        switch (binder.getState()) {
            case LOG_IN_EXCEPTION:
                logInException();
                break;
        }
    }

    private void onUpdate(boolean showProgressBar) {
        Log.d("SASManageActivity", "onUpdate: " + showProgressBar);
        Log.d("SASManageActivity", "onUpdate: Starting thread");
        //((TextView) findViewById(R.id.textView3)).setText(R.string.loading);
        refreshThread.startWorker(new Runnable() {
            @Override
            public void run() {
                Log.d("SASManageActivity", "onUpdate: Thread running");
                MultilineItem[] data;
                AdapterView.OnItemClickListener onClickListener = null;
                try {
                    if (binder != null) {
                        switch (marksShort) {
                            case LESSONS:
                                data = binder.getLessons(semester);

                                final MultilineItem[] finalData = data;
                                onClickListener = new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        ListView listView = new ListView(SASManageActivity.this);
                                        Lesson lesson = (Lesson) finalData[position];
                                        Mark[] marks = lesson.getMarks();

                                        MultilineAdapter<Mark> adapter = new MultilineAdapter<>(SASManageActivity.this,
                                                R.layout.text_multi_line_list_item, marks);

                                        listView.setAdapter(adapter);
                                        listView.setOnItemClickListener(generateMarkOnClickListener(Arrays.asList(marks)
                                                .toArray(new MultilineItem[marks.length])));
                                        new AlertDialog.Builder(SASManageActivity.this)
                                                .setTitle(lesson.getFullName())
                                                .setIcon(R.mipmap.ic_launcher_sas)
                                                .setView(listView)
                                                .setPositiveButton(R.string.but_ok, null)
                                                .setCancelable(true)
                                                .show();
                                    }
                                };
                                break;
                            case DATE:
                            default:
                                Log.d("SASManageActivity", "onUpdate: Loading marks");
                                data = binder.getMarks(semester);

                                onClickListener = generateMarkOnClickListener(data);
                                break;
                        }
                    } else {
                        throw new NullPointerException();
                    }
                } catch (NullPointerException | InterruptedException e) {
                    Log.d("SASManageActivity", "onUpdate", e);
                    data = new MultilineItem[]{new TextMultilineItem(getString(R.string.exception_title_sas_manager_binder_null),
                            getString(R.string.exception_message_sas_manager_binder_null))};
                }

                final AdapterView.OnItemClickListener finalOnClickListener = onClickListener;
                Log.d("SASManageActivity", "onUpdate: Starting list update");
                adapter.setNotifyOnChange(false);
                adapter.clear();
                for (MultilineItem item : data) {
                    adapter.add(item);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("SASManageActivity", "onUpdate: Updating list");
                        adapter.notifyDataSetChanged();
                        listView.setOnItemClickListener(finalOnClickListener);
                        Log.d("SASManageActivity", "onUpdate: List updated");
                    }
                });
            }
        }, showProgressBar ? getString(R.string.wait_text_loading) : null);
        Log.d("SASManageActivity", "onUpdate: Thread started");
    }

    private AdapterView.OnItemClickListener generateMarkOnClickListener(final MultilineItem[] data) {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Mark mark = (Mark) data[position];
                new AlertDialog.Builder(SASManageActivity.this)
                        .setTitle("".equals(mark.getNote()) ? mark.getLongLesson() : mark.getNote())
                        .setIcon(R.mipmap.ic_launcher_sas)
                                //.setView(listView)
                        .setMessage(getString(R.string.text_date) + ": " + mark.getDateAsString()
                                + "\n" + getString(R.string.text_short_lesson_name) + ": " + mark.getShortLesson()
                                + "\n" + getString(R.string.text_long_lesson_name) + ": " + mark.getLongLesson()
                                + "\n" + getString(R.string.text_value) + ": " + mark.getValueToShow()
                                + "\n" + getString(R.string.text_weight) + ": " + mark.getWeight()
                                + "\n" + getString(R.string.text_type) + ": " + mark.getType()
                                + "\n" + getString(R.string.text_note) + ": " + mark.getNote()
                                + "\n" + getString(R.string.text_teacher) + ": " + mark.getTeacher())
                        .setPositiveButton(R.string.but_ok, null)
                        .setCancelable(true)
                        .show();
            }
        };
    }

    private void logOut() {
        Log.d("SASManageActivity", "logOut");
        AppDataManager.logout(AppDataManager.Type.SAS);
        //binder.waitToWorkerStop();
        //sendBroadcast(new Intent(this, StartActivityReceiver.class));
        //new StartActivityReceiver().onReceive(this, null);
        startActivity(new Intent(this, SASSplashActivity.class));
        finish();
    }

    private void logInException() {
        Log.d("SASManageActivity", "logInException");
        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(String.format(getString(R.string.exception_title_login),
                        AppDataManager.getUsername(AppDataManager.Type.SAS)))
                .setMessage(R.string.exception_message_login)
                .setPositiveButton(R.string.but_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refreshThread.startWorker(new Runnable() {
                            @Override
                            public void run() {
                                if (binder != null) {
                                    binder.refresh();
                                    try {
                                        binder.waitToWorkerStop();
                                    } catch (InterruptedException e) {
                                        Log.d("SASManageActivity", "logInException", e);
                                    }
                                    if (binder.getState() == SASManagerService.State.LOG_IN_EXCEPTION) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                logInException();
                                            }
                                        });
                                    }
                                }
                            }
                        }, getString(R.string.wait_text_logging_in));
                    }
                })
                .setNeutralButton(R.string.but_log_out,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logOut();
                            }
                        }

                )
                .setNegativeButton(R.string.but_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                .setIcon(R.mipmap.ic_launcher_sas)
                .setCancelable(false)
                .show();
    }

    private enum MarksShort {
        DATE, LESSONS
    }

}
