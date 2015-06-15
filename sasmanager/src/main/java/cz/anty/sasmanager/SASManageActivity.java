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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cz.anty.utils.LoginDataManager;
import cz.anty.utils.OnceRunThreadWithProgress;
import cz.anty.utils.StableArrayAdapter;
import cz.anty.utils.sas.mark.Lesson;
import cz.anty.utils.sas.mark.Mark;
import cz.anty.utils.sas.mark.Marks;
import cz.anty.utils.sas.mark.MarksManager;

public class SASManageActivity extends AppCompatActivity {

    private ListView listView;
    private SASManagerService.MyBinder binder = null;
    private OnceRunThreadWithProgress refreshThread;
    private MarksShort marksShort = MarksShort.DATE;
    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
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
                            onUpdate();
                        }
                    });
                    onUpdate();
                }
            });
        }

        public void onServiceDisconnected(ComponentName className) {
            refreshThread.waitToWorkerStop();
            SASManageActivity.this.binder.setOnMarksChangeListener(null);
            SASManageActivity.this.binder.setOnStateChangedListener(null);
            SASManageActivity.this.binder = null;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*SharedPreferences preferences = getSharedPreferences("LoginData", MODE_PRIVATE);
        if (preferences.getString("LOGIN", "").equals("") || preferences.getString("PASSWORD", "").equals("")) {
            this.finish();
            startActivity(new Intent(this, SASLoginActivity.class));
            return;
        }*/

        setContentView(R.layout.activity_manage);
        listView = ((ListView) findViewById(R.id.listView));

        if (refreshThread == null)
            refreshThread = new OnceRunThreadWithProgress(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, SASManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == R.id.action_settings) {
            //startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (i == R.id.action_refresh) {
            if (binder != null) {
                binder.refresh();
                onUpdate();
            }
            return true;
        } else if (i == R.id.action_log_out) {
            logOut();
            return true;
        } else if (i == R.id.action_sort_date) {
            marksShort = MarksShort.DATE;
            onUpdate();
        } else if (i == R.id.action_sort_lesson) {
            marksShort = MarksShort.LESSONS;
            onUpdate();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onStateChanged() {
        if (binder == null) return;
        //onUpdate();
        switch (binder.getState()) {
            case LOG_IN_EXCEPTION:
                logInException();
                break;
        }
    }

    public void onUpdate() {
        //((TextView) findViewById(R.id.textView3)).setText(R.string.loading);
        refreshThread.startWorker(new Runnable() {
            @Override
            public void run() {
                /*final StringBuilder builder = new StringBuilder(getString(R.string.state) + ": ");
                if (binder != null) {
                    Mark[] marks = binder.getMarks(MarksManager.Semester.AUTO);
                    builder.append(binder.getState()).append("\n").append(getString(R.string.marks)).append(":");
                    for (Mark mark : marks) {
                        builder.append("\n").append(mark.toString());
                    }
                } else
                    builder.append("null").append("\n").append(getString(R.string.marks)).append(":");*/
                String[] values;
                AdapterView.OnItemClickListener onClickListener = null;
                if (binder != null) {
                    switch (marksShort) {
                        case LESSONS:
                            final List<Lesson> lessons = Marks.toLessons(Arrays.asList(binder.getMarks(MarksManager.Semester.AUTO)));
                            values = new String[lessons.size()];
                            for (int i = 0; i < values.length; i++) {
                                values[i] = lessons.get(i).toString();
                            }
                            onClickListener = new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    ListView listView = new ListView(SASManageActivity.this);
                                    Lesson lesson = lessons.get(position);
                                    Mark[] marks = lesson.getMarks();
                                    String[] values = new String[marks.length];
                                    for (int i = 0; i < values.length; i++) {
                                        values[i] = marks[i].toString();
                                    }

                                    ArrayList<String> list = new ArrayList<>();
                                    Collections.addAll(list, values);

                                    final StableArrayAdapter adapter = new StableArrayAdapter(SASManageActivity.this,
                                            android.R.layout.simple_list_item_1, list);

                                    listView.setAdapter(adapter);
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
                            Mark[] marks = binder.getMarks(MarksManager.Semester.AUTO);
                            values = new String[marks.length];
                            for (int i = 0; i < values.length; i++) {
                                values[i] = marks[i].toString();
                            }
                            break;
                    }
                } else {
                    values = new String[]{getString(R.string.manage_null_exception_message)};
                }
                ArrayList<String> list = new ArrayList<>();
                Collections.addAll(list, values);

                final StableArrayAdapter adapter = new StableArrayAdapter(SASManageActivity.this,
                        android.R.layout.simple_list_item_1, list);

                final AdapterView.OnItemClickListener finalOnClickListener = onClickListener;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //String [] values = new String[] {getString(R.string.sas_app_name), getString(R.string.wifi_app_name)};
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(finalOnClickListener);

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

                        });*/
                        //((TextView) findViewById(R.id.textView3)).setText(builder);
                    }
                });
            }
        }, getString(R.string.loading));
    }

    private void logOut() {
        LoginDataManager.logout(LoginDataManager.Type.SAS, this);
        binder.waitToWorkerStop();
        //sendBroadcast(new Intent(this, StartActivityReceiver.class));
        //new StartActivityReceiver().onReceive(this, null);
        startActivity(new Intent(this, SASSplashActivity.class));
        finish();
    }

    private void logInException() {
        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(R.string.login_exception_title)
                .setMessage(R.string.login_exception_message)
                .setPositiveButton(R.string.but_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refreshThread.startWorker(new Runnable() {
                            @Override
                            public void run() {
                                if (binder != null) {
                                    binder.refresh();
                                    binder.waitToWorkerStop();
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
                        }, getString(R.string.logging_in));
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
