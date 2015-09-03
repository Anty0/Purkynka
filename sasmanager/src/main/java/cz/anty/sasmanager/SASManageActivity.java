package cz.anty.sasmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Arrays;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
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
    private MultilineAdapter adapter;
    private SASManagerService.MyBinder binder = null;
    private OnceRunThreadWithSpinner refreshThread;
    private MarksShort marksShort = MarksShort.DATE;
    private MarksManager.Semester semester = MarksManager.Semester.AUTO.getStableSemester();
    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            if (AppDataManager.isDebugMode(SASManageActivity.this))
                Log.d("SASManageActivity", "onServiceConnected");
            SASManageActivity.this.binder = (SASManagerService.MyBinder) binder;
            refreshThread.startWorker(new Runnable() {
                @Override
                public void run() {
                    SASManageActivity.this.binder.setOnStateChangedListener(new Runnable() {
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

                    SASManageActivity.this.binder.setOnMarksChangeListener(new Runnable() {
                        @Override
                        public void run() {
                            onUpdate(false);
                        }
                    });
                    onUpdate(true);
                }
            });
        }

        public void onServiceDisconnected(ComponentName className) {
            if (AppDataManager.isDebugMode(SASManageActivity.this))
                Log.d("SASManageActivity", "onServiceDisconnected");
            try {
                refreshThread.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d("SASManageActivity", "onServiceDisconnected", e);
            }
            SASManageActivity.this.binder.setOnMarksChangeListener(null);
            SASManageActivity.this.binder.setOnStateChangedListener(null);
            SASManageActivity.this.binder = null;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppDataManager.isDebugMode(this)) Log.d("SASManageActivity", "onCreate");
        super.onCreate(savedInstanceState);

        /*SharedPreferences preferences = getSharedPreferences("LoginData", MODE_PRIVATE);
        if (preferences.getString("LOGIN", "").equals("") || preferences.getString("PASSWORD", "").equals("")) {
            this.finish();
            startActivity(new Intent(this, SASLoginActivity.class));
            return;
        }*/

        setContentView(R.layout.activity_list);
        listView = ((ListView) findViewById(R.id.listView));
        adapter = new MultilineAdapter(this, R.layout.text_multi_line_list_item);
        listView.setAdapter(adapter);

        if (refreshThread == null)
            refreshThread = new OnceRunThreadWithSpinner(this);
    }

    @Override
    protected void onStart() {
        if (AppDataManager.isDebugMode(this)) Log.d("SASManageActivity", "onStart");
        super.onStart();
        Intent intent = new Intent(this, SASManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (AppDataManager.isDebugMode(this)) Log.d("SASManageActivity", "onStop");
        super.onStop();
        unbindService(mConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppDataManager.isDebugMode(this)) Log.d("SASManageActivity", "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (AppDataManager.isDebugMode(this)) Log.d("SASManageActivity", "onOptionsItemSelected");
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
            onUpdate(true);
            return true;
        } else if (i == R.id.action_sort_lesson) {
            marksShort = MarksShort.LESSONS;
            onUpdate(true);
            return true;
        } else if (i == R.id.action_semester_first) {
            semester = MarksManager.Semester.FIRST;
            onUpdate(true);
            return true;
        } else if (i == R.id.action_semester_second) {
            semester = MarksManager.Semester.SECOND;
            onUpdate(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onStateChanged() {
        if (AppDataManager.isDebugMode(this)) Log.d("SASManageActivity", "onStateChanged");
        if (binder == null) return;
        //onUpdate();
        switch (binder.getState()) {
            case LOG_IN_EXCEPTION:
                logInException();
                break;
        }
    }

    private void onUpdate(boolean showProgressBar) {
        if (AppDataManager.isDebugMode(this))
            Log.d("SASManageActivity", "onUpdate: " + showProgressBar);
        if (AppDataManager.isDebugMode(this))
            Log.d("SASManageActivity", "onUpdate: Starting thread");
        //((TextView) findViewById(R.id.textView3)).setText(R.string.loading);
        refreshThread.startWorker(new Runnable() {
            @Override
            public void run() {
                if (AppDataManager.isDebugMode(SASManageActivity.this))
                    Log.d("SASManageActivity", "onUpdate: Thread running");
                /*final StringBuilder builder = new StringBuilder(getString(R.string.state) + ": ");
                if (binder != null) {
                    Mark[] marks = binder.getMarks(MarksManager.Semester.AUTO);
                    builder.append(binder.getState()).append("\n").append(getString(R.string.marks)).append(":");
                    for (Mark mark : marks) {
                        builder.append("\n").append(mark.toString());
                    }
                } else
                    builder.append("null").append("\n").append(getString(R.string.marks)).append(":");*/
                MultilineItem[] data;
                AdapterView.OnItemClickListener onClickListener = null;
                try {
                    if (binder != null) {
                        switch (marksShort) {
                            case LESSONS:
                                //final List<Lesson> lessons = Marks.toLessons(Arrays.asList(binder.getMarks(semester)));
                                //data = lessons.toArray(new MultilineItem[lessons.size()]);
                                data = binder.getLessons(semester);
                            /*values = new String[lessons.size()];
                            for (int i = 0; i < values.length; i++) {
                                values[i] = lessons.get(i).toString();
                            }*/
                                final MultilineItem[] finalData = data;
                                onClickListener = new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        ListView listView = new ListView(SASManageActivity.this);
                                        Lesson lesson = (Lesson) finalData[position];
                                        Mark[] marks = lesson.getMarks();
                                    /*String[] values = new String[marks.length];
                                    for (int i = 0; i < values.length; i++) {
                                        values[i] = marks[i].toString();
                                    }

                                    ArrayList<String> list = new ArrayList<>();
                                    Collections.addAll(list, values);

                                    final StableArrayAdapter adapter = new StableArrayAdapter(SASManageActivity.this,
                                            android.R.layout.simple_list_item_1, list);*/
                                        MultilineAdapter adapter = new MultilineAdapter(SASManageActivity.this,
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
                            /*Mark[] marks*/
                                if (AppDataManager.isDebugMode(SASManageActivity.this))
                                    Log.d("SASManageActivity", "onUpdate: Loading marks");
                                data = binder.getMarks(semester);
                            /*values = new String[marks.length];
                            for (int i = 0; i < values.length; i++) {
                                values[i] = marks[i].toString();
                            }*/
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
                /*ArrayList<String> list = new ArrayList<>();
                Collections.addAll(list, values);

                final StableArrayAdapter adapter = new StableArrayAdapter(SASManageActivity.this,
                        android.R.layout.simple_list_item_1, list);*/

                final AdapterView.OnItemClickListener finalOnClickListener = onClickListener;
                if (AppDataManager.isDebugMode(SASManageActivity.this))
                    Log.d("SASManageActivity", "onUpdate: Starting list update");
                adapter.setNotifyOnChange(false);
                adapter.clear();
                for (MultilineItem item : data) {
                    adapter.add(item);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (AppDataManager.isDebugMode(SASManageActivity.this))
                            Log.d("SASManageActivity", "onUpdate: Updating list");
                        adapter.notifyDataSetChanged();
                        listView.setOnItemClickListener(finalOnClickListener);
                        if (AppDataManager.isDebugMode(SASManageActivity.this))
                            Log.d("SASManageActivity", "onUpdate: List updated");
                    }
                });
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //String [] values = new String[] {getString(R.string.sas_app_name), getString(R.string.wifi_app_name)};
                        //listView.setAdapter(adapter);

                        //adapter.notifyDataSetChanged();

                        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, final View view,
                                                    int position, long id) {
                                //final String item = (String) parent.getItemAtPosition(position);
                                switch (position) {
                                    case 1:
                                        startActivity(new Intent(SASManageActivity.this, WifiLoginActivity.class));
                                        break;
                                    case 0:
                                    default:
                                        startActivity(new Intent(SASManageActivity.this, SASSplashActivity.class));
                                        break;
                                }
                            }

                        });
                        //((TextView) findViewById(R.id.textView3)).setText(builder);
                    }
                });*/
            }
        }, showProgressBar ? getString(R.string.wait_text_loading) : null);
        if (AppDataManager.isDebugMode(this))
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
        if (AppDataManager.isDebugMode(this)) Log.d("SASManageActivity", "logOut");
        AppDataManager.logout(AppDataManager.Type.SAS, this);
        //binder.waitToWorkerStop();
        //sendBroadcast(new Intent(this, StartActivityReceiver.class));
        //new StartActivityReceiver().onReceive(this, null);
        startActivity(new Intent(this, SASSplashActivity.class));
        finish();
    }

    private void logInException() {
        if (AppDataManager.isDebugMode(this)) Log.d("SASManageActivity", "logInException");
        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.exception_title_login)
                        .replace(Constants.STRINGS_CONST_NAME,
                                AppDataManager.getUsername(AppDataManager.Type.SAS, this)))
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
