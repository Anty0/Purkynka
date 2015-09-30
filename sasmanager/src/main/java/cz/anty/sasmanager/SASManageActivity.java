package cz.anty.sasmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Log;
import cz.anty.utils.list.listView.MultilineItem;
import cz.anty.utils.list.listView.TextMultilineItem;
import cz.anty.utils.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerItemClickListener;
import cz.anty.utils.sas.mark.Lesson;
import cz.anty.utils.sas.mark.Mark;
import cz.anty.utils.sas.mark.MarksManager;
import cz.anty.utils.service.ServiceManager;
import cz.anty.utils.settings.SASManagerSettingsActivity;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;

public class SASManageActivity extends AppCompatActivity {

    private MultilineRecyclerAdapter<MultilineItem> adapter;
    private OnceRunThreadWithSpinner refreshThread;
    private MarksShort marksShort = MarksShort.DATE;
    private MarksManager.Semester semester = MarksManager.Semester.AUTO.getStableSemester();
    private SASManagerService.SASBinder binder = null;
    private ServiceManager.BinderConnection<SASManagerService.SASBinder> binderConnection
            = new ServiceManager.BinderConnection<SASManagerService.SASBinder>() {
        @Override
        public void onBinderConnected(final SASManagerService.SASBinder sasBinder) {
            Log.d(getClass().getSimpleName(), "onBinderConnected");
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
            Log.d(getClass().getSimpleName(), "onBinderDisconnected");
            try {
                refreshThread.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d(getClass().getSimpleName(), "onBinderDisconnected", e);
            }
            binder.setOnMarksChangeListener(null);
            binder.setOnStateChangedListener(null);
            binder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(getClass().getSimpleName(), "onCreate");
        super.onCreate(savedInstanceState);

        if (SASSplashActivity.serviceManager == null
                || !SASSplashActivity.serviceManager.isConnected()) {
            startActivity(new Intent(this, SASSplashActivity.class));
            finish();
            return;
        }

        if (refreshThread == null)
            refreshThread = new OnceRunThreadWithSpinner(this);

        adapter = new MultilineRecyclerAdapter<>();
        adapter.setNotifyOnChange(false);
        RecyclerAdapter.inflateToActivity(this, null, adapter,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        detectClass(adapter.getItem(position));
                    }

                    private void detectClass(MultilineItem item) {
                        if (item instanceof Lesson) {
                            lessonOnClick((Lesson) item);
                            return;
                        }
                        if (item instanceof Mark) {
                            markOnClick((Mark) item);
                        }
                    }

                    private void lessonOnClick(Lesson lesson) {
                        Mark[] marks = lesson.getMarks();

                        final MultilineRecyclerAdapter<Mark> adapter =
                                new MultilineRecyclerAdapter<>(marks);

                        View result = RecyclerAdapter.inflate(SASManageActivity.this, null, false, null, adapter,
                                new RecyclerItemClickListener.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        detectClass(adapter.getItem(position));
                                    }
                                });

                        new AlertDialog.Builder(SASManageActivity.this)
                                .setTitle(lesson.getFullName())
                                .setIcon(R.mipmap.ic_launcher_sas)
                                .setView(result)
                                .setPositiveButton(R.string.but_ok, null)
                                .setCancelable(true)
                                .show();
                    }

                    private void markOnClick(Mark mark) {
                        String note = mark.getNote();
                        new AlertDialog.Builder(SASManageActivity.this)
                                .setTitle("".equals(note) ? mark.getLongLesson() : note)
                                .setIcon(R.mipmap.ic_launcher_sas)
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
                });

        if (SASSplashActivity.serviceManager != null) {
            SASSplashActivity.serviceManager
                    .addBinderConnection(binderConnection);
        }

        if (AppDataManager.isFirstStart(AppDataManager.Type.SAS)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_sas_widget_alert)
                            //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon T
                    .setMessage(R.string.dialog_message_sas_widget_alert)
                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            AppDataManager.setFirstStart(AppDataManager.Type.SAS, false);
                        }
                    })
                    .setNegativeButton(R.string.but_later, null)
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(getClass().getSimpleName(), "onDestroy");
        if (SASSplashActivity.serviceManager != null) {
            SASSplashActivity.serviceManager
                    .removeBinderConnection(binderConnection);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getClass().getSimpleName(), "onCreateOptionsMenu");
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
        Log.d(getClass().getSimpleName(), "onOptionsItemSelected");
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
        Log.d(getClass().getSimpleName(), "onStateChanged");
        if (binder == null) return;

        switch (binder.getState()) {
            case LOG_IN_EXCEPTION:
                logInException();
                break;
        }
    }

    private void onUpdate(boolean showProgressBar) {
        Log.d(getClass().getSimpleName(), "onUpdate: " + showProgressBar);
        Log.d(getClass().getSimpleName(), "onUpdate: Starting thread");
        //((TextView) findViewById(R.id.textView3)).setText(R.string.loading);
        refreshThread.startWorker(new Runnable() {
            @Override
            public void run() {
                Log.d(getClass().getSimpleName(), "onUpdate: Thread running");
                MultilineItem[] data;
                try {
                    if (binder != null) {
                        switch (marksShort) {
                            case LESSONS:
                                Log.d(getClass().getSimpleName(), "onUpdate: Loading lessons");
                                data = binder.getLessons(semester);
                                break;
                            case DATE:
                            default:
                                Log.d(getClass().getSimpleName(), "onUpdate: Loading marks");
                                data = binder.getMarks(semester);
                                break;
                        }
                    } else {
                        throw new NullPointerException();
                    }
                } catch (NullPointerException | InterruptedException e) {
                    Log.d(getClass().getSimpleName(), "onUpdate", e);
                    data = new MultilineItem[]{new TextMultilineItem(getString(R.string.exception_title_sas_manager_binder_null),
                            getString(R.string.exception_message_sas_manager_binder_null))};
                }

                Log.d(getClass().getSimpleName(), "onUpdate: Updating list");
                adapter.clearItems();
                adapter.addAllItems(data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        Log.d(getClass().getSimpleName(), "onUpdate: List updated");
                    }
                });
            }
        }, showProgressBar ? getString(R.string.wait_text_loading) : null);
        Log.d(getClass().getSimpleName(), "onUpdate: Thread started");
    }

    private void logOut() {
        Log.d(getClass().getSimpleName(), "logOut");
        AppDataManager.logout(AppDataManager.Type.SAS);
        //binder.waitToWorkerStop();
        //sendBroadcast(new Intent(this, StartActivityReceiver.class));
        //new StartActivityReceiver().onReceive(this, null);
        startActivity(new Intent(this, SASSplashActivity.class));
        finish();
    }

    private void logInException() {
        Log.d(getClass().getSimpleName(), "logInException");
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
                                        Log.d(getClass().getSimpleName(), "logInException", e);
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
