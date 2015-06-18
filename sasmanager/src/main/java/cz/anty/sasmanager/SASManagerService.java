package cz.anty.sasmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cz.anty.utils.LoginDataManager;
import cz.anty.utils.OnceRunThread;
import cz.anty.utils.WrongLoginDataException;
import cz.anty.utils.sas.SASManager;
import cz.anty.utils.sas.mark.Mark;
import cz.anty.utils.sas.mark.MarksManager;

public class SASManagerService extends Service {

    private final IBinder mBinder = new MyBinder();
    private final OnceRunThread worker = new OnceRunThread();
    private State state = State.STOPPED;
    private SASManager sasManager;
    private MarksManager marks;
    private Runnable onMarksChange = null;
    private Runnable onStateChangedListener = null;
    private final Runnable onLoginChange = new Runnable() {
        @Override
        public void run() {
            marks.clear(MarksManager.Semester.FIRST);
            marks.clear(MarksManager.Semester.SECOND);
            marks.apply(MarksManager.Semester.FIRST);
            marks.apply(MarksManager.Semester.SECOND);
            worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    initialize();
                }
            });
        }
    };

    public void setState(State state) {
        if (state != this.state) {
            this.state = state;
            if (onStateChangedListener != null)
                onStateChangedListener.run();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (marks == null)
            marks = new MarksManager(this);
        LoginDataManager.addOnChangeListener(LoginDataManager.Type.SAS, onLoginChange);
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                initialize();
            }
        });
    }

    private void initialize() {
        if (sasManager != null && sasManager.isConnected()) {
            sasManager.disconnect();
            setState(State.DISCONNECTED);
        }

        if (LoginDataManager.isLoggedIn(LoginDataManager.Type.SAS, SASManagerService.this)) {
            sasManager = new SASManager(LoginDataManager.getUsername(LoginDataManager.Type.SAS, SASManagerService.this),
                    LoginDataManager.getPassword(LoginDataManager.Type.SAS, SASManagerService.this));
            refreshMarks(true);
            //setState(State.CONNECTED);
        } else {
            sasManager = null;
            setState(State.DISCONNECTED);
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        LoginDataManager.removeOnChangeListener(LoginDataManager.Type.SAS, onLoginChange);
        setState(State.STOPPED);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                refreshMarks(false);
            }
        });
        //return super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    private void refreshMarks(boolean deepRefresh) {
        if (sasManager == null) return;
        try {
            if (!sasManager.isConnected()) {
                sasManager.connect();
                setState(State.CONNECTED);
                if (sasManager.isLoggedIn())
                    setState(State.LOGGED_IN);
            }
            try {
                MarksManager.Semester semester = MarksManager.Semester.AUTO.getStableSemester();
                List<Mark> newMarks = sasManager.getMarks(semester);
                int numberOfNewMarks = newMarks.size() - marks.get(semester).length;
                //System.out.println("old: " + marks.get(semester).length + " new: " + newMarks.size());
                marks.clear(semester);
                marks.addAll(newMarks, semester);
                marks.apply(semester);
                if (numberOfNewMarks > 0) {
                    onMarksChange(semester, numberOfNewMarks);
                }
                semester = semester.reverse();
                if (deepRefresh || marks.get(semester).length == 0) {
                    List<Mark> newMarks2 = sasManager.getMarks(semester);
                    //int numberOfNewMarks2 = newMarks2.size() - marks.get(semester).length;
                    marks.clear(semester);
                    marks.addAll(newMarks2, semester);
                    marks.apply(semester);
                                /*if (numberOfNewMarks2 > 0) {
                                    onMarksChange(semester, numberOfNewMarks2);
                                }*/
                }
                setState(State.LOGGED_IN);
            } catch (WrongLoginDataException e) {
                Log.d(null, null, e);
                setState(State.LOG_IN_EXCEPTION);
            }
        } catch (IOException e) {
            Log.d(null, null, e);
            setState(State.CONNECT_EXCEPTION);
        }
    }

    private void onMarksChange(MarksManager.Semester semester, int newMarks) {
        if (onMarksChange != null)
            onMarksChange.run();

        Mark[] marks = this.marks.get(semester);
        StringBuilder builderBig = new StringBuilder();
        builderBig.append(marks[0]);
        for (int i = 1; i < newMarks; i++) {
            builderBig.append("\n").append(marks[i]);
        }

        StringBuilder builder = new StringBuilder("From: ");
        builder.append(marks[0].getShortLesson());
        for (int i = 1; i < newMarks; i++) {
            builder.append(", ").append(marks[i].getShortLesson());
        }

        // prepare intent which is triggered if the
        // notification is selected

        Intent intent = new Intent(SASManagerService.this, SASSplashActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(SASManagerService.this, 0, intent, 0);

        // build notification
        // the addAction re-use the same intent to keep the example short
        Notification n = new NotificationCompat.Builder(SASManagerService.this)
                .setContentTitle(newMarks + " " + (newMarks > 1 ? getString(R.string.new_marks) : getString(R.string.new_mark)))
                .setContentText(builder)
                .setSmallIcon(R.mipmap.ic_launcher_sas)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                        //.addAction(R.mipmap.ic_launcher, "And more", pIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(builderBig))
                .build();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .notify(calendar.get(Calendar.HOUR) * 60 * 60 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND) + 10, n);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public enum State {
        STOPPED, CONNECTED, CONNECT_EXCEPTION, LOGGED_IN, LOG_IN_EXCEPTION, DISCONNECTED
    }

    public class MyBinder extends Binder {

        public boolean isWorkerRunning() {
            return worker.isWorkerRunning();
        }

        public void waitToWorkerStop() {
            worker.waitToWorkerStop();
        }

        public void setOnMarksChangeListener(@Nullable Runnable onMarksChange) {
            SASManagerService.this.onMarksChange = onMarksChange;
        }

        public void setOnStateChangedListener(@Nullable Runnable onStateChanged) {
            SASManagerService.this.onStateChangedListener = onStateChanged;
        }

        public State getState() {
            return state;
        }

        /*public boolean isLoggedIn() throws IOException {
            return sasManager != null && sasManager.isLoggedIn();
        }*/

        public void refresh() {
            worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    refreshMarks(true);
                }
            });
        }

        /*public SASManagerService getService() {
            return SASManagerService.this;
        }*/

        public Mark[] getMarks(MarksManager.Semester semester) {
            waitToWorkerStop();
            return marks.get(semester);
        }

    }

}
