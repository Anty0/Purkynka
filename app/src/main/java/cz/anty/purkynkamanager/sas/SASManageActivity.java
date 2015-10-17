package cz.anty.purkynkamanager.sas;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.settings.SASManagerSettingsActivity;
import cz.anty.purkynkamanager.utils.AppDataManager;
import cz.anty.purkynkamanager.utils.Constants;
import cz.anty.purkynkamanager.utils.Log;
import cz.anty.purkynkamanager.utils.list.listView.MultilineItem;
import cz.anty.purkynkamanager.utils.list.listView.TextMultilineItem;
import cz.anty.purkynkamanager.utils.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.purkynkamanager.utils.list.recyclerView.RecyclerInflater;
import cz.anty.purkynkamanager.utils.list.recyclerView.RecyclerItemClickListener;
import cz.anty.purkynkamanager.utils.list.recyclerView.SpecialItemAnimator;
import cz.anty.purkynkamanager.utils.sas.mark.Lesson;
import cz.anty.purkynkamanager.utils.sas.mark.Mark;
import cz.anty.purkynkamanager.utils.sas.mark.MarksManager;
import cz.anty.purkynkamanager.utils.service.ServiceManager;
import cz.anty.purkynkamanager.utils.thread.OnceRunThreadWithSpinner;

public class SASManageActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SASManageActivity";

    private MultilineRecyclerAdapter<MultilineItem> adapter;
    private OnceRunThreadWithSpinner refreshThread;
    private TextView exceptionTextView;

    private MarksShort marksShort = MarksShort.DATE;
    private MarksManager.Semester semester = MarksManager.Semester.AUTO.getStableSemester();
    private boolean isInException = false;
    private boolean showedException = false;

    private SASManagerService.SASBinder binder = null;
    private ServiceManager.BinderConnection<SASManagerService.SASBinder> binderConnection
            = new ServiceManager.BinderConnection<SASManagerService.SASBinder>() {
        @Override
        public void onBinderConnected(final SASManagerService.SASBinder sasBinder) {
            Log.d(LOG_TAG, "onBinderConnected");
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
                            onUpdate(false, false);
                        }
                    });
                    onUpdate(true, true);
                }
            }, getText(R.string.wait_text_loading));
        }

        @Override
        public void onBinderDisconnected() {
            Log.d(LOG_TAG, "onBinderDisconnected");
            try {
                refreshThread.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d(LOG_TAG, "onBinderDisconnected", e);
            }
            binder.setOnMarksChangeListener(null);
            binder.setOnStateChangedListener(null);
            binder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);

        if (SASSplashActivity.serviceManager == null
                || !SASSplashActivity.serviceManager.isConnected()) {
            startActivity(new Intent(this, SASSplashActivity.class));
            finish();
            return;
        }

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .cancel(Constants.NOTIFICATION_ID_SAS_MANAGER_SERVICE);

        if (refreshThread == null)
            refreshThread = new OnceRunThreadWithSpinner(this);

        adapter = new MultilineRecyclerAdapter<>(this);
        adapter.setNotifyOnChange(false);
        RecyclerInflater.inflateToActivity(this, R.layout.activity_sas_manage, adapter,
                new RecyclerItemClickListener.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        detectClass(adapter.getItem(position));
                    }

                    @Override
                    public void onLongClick(View view, int position) {

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
                                new MultilineRecyclerAdapter<>(SASManageActivity.this, marks);

                        View result = RecyclerInflater.inflate(SASManageActivity.this, null, false, adapter,
                                new RecyclerItemClickListener.ClickListener() {
                                    @Override
                                    public void onClick(View view, int position) {
                                        detectClass(adapter.getItem(position));
                                    }

                                    @Override
                                    public void onLongClick(View view, int position) {

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
                                .setMessage(getText(R.string.text_date) + ": " + mark.getDateAsString()
                                        + "\n" + getText(R.string.text_short_lesson_name) + ": " + mark.getShortLesson()
                                        + "\n" + getText(R.string.text_long_lesson_name) + ": " + mark.getLongLesson()
                                        + "\n" + getText(R.string.text_value) + ": " + mark.getValueToShow()
                                        + "\n" + getText(R.string.text_weight) + ": " + mark.getWeight()
                                        + "\n" + getText(R.string.text_type) + ": " + mark.getType()
                                        + "\n" + getText(R.string.text_note) + ": " + mark.getNote()
                                        + "\n" + getText(R.string.text_teacher) + ": " + mark.getTeacher())
                                .setPositiveButton(R.string.but_ok, null)
                                .setCancelable(true)
                                .show();
                    }
                });

        exceptionTextView = (TextView) findViewById(R.id.text_view_exception);

        if (SASSplashActivity.serviceManager != null) {
            SASSplashActivity.serviceManager
                    .addBinderConnection(binderConnection);
        }

        if (AppDataManager.isFirstStart(AppDataManager.Type.SAS)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_sas_widget_alert)
                    .setIcon(R.mipmap.ic_launcher_sas)
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
        Log.d(LOG_TAG, "onDestroy");
        if (SASSplashActivity.serviceManager != null) {
            SASSplashActivity.serviceManager
                    .removeBinderConnection(binderConnection);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onCreateOptionsMenu");
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
        Log.d(LOG_TAG, "onOptionsItemSelected");
        int i = item.getItemId();

        if (i == R.id.action_settings) {
            startActivity(new Intent(this, SASManagerSettingsActivity.class));
            return true;
        } else if (i == R.id.action_refresh) {
            if (binder != null) {
                binder.refresh();
                onUpdate(true, false);
            }
            return true;
        } else if (i == R.id.action_log_out) {
            logOut();
            return true;
        } else if (i == R.id.action_sort_date) {
            marksShort = MarksShort.DATE;
            item.setChecked(true);
            onUpdate(true, true);
            return true;
        } else if (i == R.id.action_sort_lesson) {
            marksShort = MarksShort.LESSONS;
            item.setChecked(true);
            onUpdate(true, true);
            return true;
        } else if (i == R.id.action_semester_first) {
            semester = MarksManager.Semester.FIRST;
            item.setChecked(true);
            onUpdate(true, true);
            return true;
        } else if (i == R.id.action_semester_second) {
            semester = MarksManager.Semester.SECOND;
            item.setChecked(true);
            onUpdate(true, true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onStateChanged() {
        Log.d(LOG_TAG, "onStateChanged");
        if (binder == null) return;

        SASManagerService.State state
                = binder.getState();
        switch (state) {
            case LOG_IN_EXCEPTION:
                logInException();
                break;
        }
        isInException = state.isException();
        onUpdate(false, true);
    }

    private void onUpdate(boolean showProgressBar, final boolean fast) {
        Log.d(LOG_TAG, "onUpdate: " + showProgressBar);
        Log.d(LOG_TAG, "onUpdate: Starting thread");
        //((TextView) findViewById(R.id.textView3)).setText(R.string.loading);
        refreshThread.startWorker(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "onUpdate: Thread running");
                MultilineItem[] data;
                try {
                    if (binder != null) {
                        switch (marksShort) {
                            case LESSONS:
                                Log.d(LOG_TAG, "onUpdate: Loading lessons");
                                if (fast) data = binder.getLessonsFast(semester);
                                else data = binder.getLessons(semester);
                                break;
                            case DATE:
                            default:
                                Log.d(LOG_TAG, "onUpdate: Loading marks");
                                if (fast) data = binder.getMarksFast(semester);
                                else data = binder.getMarks(semester);
                                break;
                        }
                    } else {
                        throw new NullPointerException();
                    }
                } catch (NullPointerException | InterruptedException e) {
                    Log.d(LOG_TAG, "onUpdate", e);
                    data = new MultilineItem[]{new TextMultilineItem(getText(R.string.exception_title_sas_manager_binder_null),
                            getText(R.string.exception_message_sas_manager_binder_null))};
                }

                Log.d(LOG_TAG, "onUpdate: Updating list");
                adapter.clearItems();
                adapter.addAllItems(data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        Log.d(LOG_TAG, "onUpdate: List updated");

                        if (isInException && !showedException) {
                            showedException = true;
                            exceptionTextView.setVisibility(View.VISIBLE);
                            ViewCompat.animate(exceptionTextView).setDuration(100)
                                    .scaleY(1).start();
                        }
                        if (!isInException && showedException) {
                            showedException = false;
                            final ViewPropertyAnimatorCompat animator = ViewCompat
                                    .animate(exceptionTextView).setDuration(100)
                                    .scaleY(0);
                            animator.setListener(
                                    new SpecialItemAnimator.VpaListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(View view) {
                                            animator.setListener(null);
                                            exceptionTextView.setVisibility(View.GONE);
                                        }
                                    }).start();
                        }
                        Log.d(LOG_TAG, "onUpdate: exception view updated");
                    }
                });
            }
        }, showProgressBar ? getText(R.string.wait_text_loading) : null);
        Log.d(LOG_TAG, "onUpdate: Thread started");
    }

    private void logOut() {
        Log.d(LOG_TAG, "logOut");
        AppDataManager.logout(AppDataManager.Type.SAS);
        //binder.waitToWorkerStop();
        //sendBroadcast(new Intent(this, StartActivityReceiver.class));
        //new StartActivityReceiver().onReceive(this, null);
        startActivity(new Intent(this, SASSplashActivity.class));
        finish();
    }

    private void logInException() {
        Log.d(LOG_TAG, "logInException");
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
                                        Log.d(LOG_TAG, "logInException", e);
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
                        }, getText(R.string.wait_text_logging_in));
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
