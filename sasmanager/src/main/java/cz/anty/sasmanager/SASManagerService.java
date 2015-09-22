package cz.anty.sasmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import java.io.IOException;
import java.util.List;

import cz.anty.sasmanager.widget.SASManageWidget;
import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.WrongLoginDataException;
import cz.anty.utils.sas.SASManager;
import cz.anty.utils.sas.mark.Lesson;
import cz.anty.utils.sas.mark.Mark;
import cz.anty.utils.sas.mark.MarksManager;
import cz.anty.utils.thread.OnceRunThread;

public class SASManagerService extends Service {

    public static final String FORCE_UPDATE_WIDGET = "UPDATE_WIDGET";

    private final IBinder mBinder = new SASBinder();
    private final OnceRunThread worker = new OnceRunThread();
    private State state = State.STOPPED;
    private SASManager sasManager;
    private MarksManager marks;
    private Runnable onMarksChange = null;
    private Runnable onStateChangedListener = null;
    private final Runnable onLoginChange = new Runnable() {
        @Override
        public void run() {
            worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    getSharedPreferences(Constants.SETTINGS_NAME_MARKS, MODE_PRIVATE).edit()
                            .putLong(Constants.SETTING_NAME_LAST_REFRESH, 0).apply();
                    marks.clear(MarksManager.Semester.FIRST);
                    marks.clear(MarksManager.Semester.SECOND);
                    marks.apply(MarksManager.Semester.FIRST);
                    marks.apply(MarksManager.Semester.SECOND);
                    init();
                }
            });
        }
    };

    private void setState(State state) {
        Log.d("SASManagerService", "setState: " + state);
        if (state != this.state) {
            if (state.isException() || this.state.isException()) {
                updateState(state);
                return;
            }
            if (this.state.getValue() >= State.getMaxValue()) {
                updateState(State.getStateByValue(State.getMinValue()));
            } else {
                updateState(State.getStateByValue(this.state.getValue() + 1));
            }
            setState(state);
        }
    }

    private void updateState(State state) {
        Log.d("SASManagerService", "updateState: " + state);
        this.state = state;
        if (onStateChangedListener != null)
            onStateChangedListener.run();
    }

    @Override
    public void onCreate() {
        Log.d("SASManagerService", "onCreate");
        super.onCreate();
        worker.setPowerManager(this);

        if (marks == null)
            marks = new MarksManager(this);
        AppDataManager.addOnChangeListener(AppDataManager.Type.SAS, onLoginChange);

        worker.startWorker(new Runnable() {
                    @Override
                    public void run() {
                        init();
                    }
        });
    }

    private void init() {
        Log.d("SASManagerService", "init");
        if (sasManager != null && sasManager.isConnected()) {
            sasManager.disconnect();
            setState(State.DISCONNECTED);
        }

        if (AppDataManager.isLoggedIn(AppDataManager.Type.SAS)) {
            sasManager = new SASManager(AppDataManager.getUsername(AppDataManager.Type.SAS),
                    AppDataManager.getPassword(AppDataManager.Type.SAS));
            refreshMarks(false, true, false);
            //setState(State.CONNECTED);
        } else {
            sasManager = null;
            setState(State.DISCONNECTED);
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        Log.d("SASManagerService", "onDestroy");
        if (SASSplashActivity.serviceManager != null)
            SASSplashActivity.serviceManager.forceDisconnect();

        if (sasManager != null && sasManager.isConnected()) {
            sasManager.disconnect();
            setState(State.DISCONNECTED);
        }
        sasManager = null;

        AppDataManager.removeOnChangeListener(AppDataManager.Type.SAS, onLoginChange);
        setState(State.STOPPED);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SASManagerService", "onStartCommand");
        if (intent != null) {
            final boolean updateWidget = intent.getBooleanExtra(FORCE_UPDATE_WIDGET, false);
            worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    if (refreshMarks(false, false, updateWidget)) {
                        SASManageWidget.callUpdate(SASManagerService.this, marks.toString());
                    }
                }
            });
        }
        return START_NOT_STICKY;
    }

    private boolean refreshMarks(boolean force, boolean deepRefresh, boolean updateWidget) {
        Log.d("SASManagerService", "refreshMarks: force=" + force + " deep=" + deepRefresh + " updateWidget=" + updateWidget);
        if (sasManager == null) return updateWidget;
        if (!force && System.currentTimeMillis() - getSharedPreferences(Constants.SETTINGS_NAME_MARKS, MODE_PRIVATE)
                .getLong(Constants.SETTING_NAME_LAST_REFRESH, 0) < Constants.WAIT_TIME_SAS_MARKS_REFRESH) {
            setState(State.LOGGED_IN);
            return updateWidget;
        }
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
                    updateWidget = false;
                }
                if (deepRefresh) {
                    semester = semester.reverse();
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
                getSharedPreferences(Constants.SETTINGS_NAME_MARKS, MODE_PRIVATE).edit()
                        .putLong(Constants.SETTING_NAME_LAST_REFRESH, System.currentTimeMillis()).apply();
            } catch (WrongLoginDataException e) {
                Log.d("SASManagerService", "refreshMarks", e);
                setState(State.LOG_IN_EXCEPTION);
            }
        } catch (IOException e) {
            Log.d("SASManagerService", "refreshMarks", e);
            setState(State.CONNECT_EXCEPTION);
        }
        return updateWidget;
    }

    private void onMarksChange(MarksManager.Semester semester, int newMarks) {
        Log.d("SASManagerService", "onMarksChange: " + semester + " - " + newMarks);
        if (onMarksChange != null)
            onMarksChange.run();
        SASManageWidget.callUpdate(this, marks.toString());

        if (!AppDataManager.isSASMarksAutoUpdate())
            return;

        Mark[] marks = this.marks.get(semester);
        StringBuilder builderBig = new StringBuilder();
        builderBig.append(marks[0]);
        for (int i = 1; i < newMarks; i++) {
            builderBig.append("\n").append(marks[i]);
        }

        StringBuilder builder = new StringBuilder(getString(R.string.text_from) + ": ");
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
                .setContentTitle(String.format(newMarks > 1 ? getString(R.string.notify_title_new_marks)
                        : getString(R.string.notify_title_new_mark), newMarks))
                .setContentText(builder)
                .setSmallIcon(R.mipmap.ic_launcher_sas)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                        //.addAction(R.mipmap.ic_launcher, "And more", pIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(builderBig).setSummaryText(builder))
                .build();

        //Calendar calendar = Calendar.getInstance(Locale.getDefault());
        //calendar.get(Calendar.HOUR) * 60 * 60 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND) + 10
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .notify(Constants.NOTIFICATION_ID_SAS_MANAGER_SERVICE, n);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("SASManagerService", "onBind");
        return mBinder;
    }

    public enum State {
        STOPPED, CONNECTED, CONNECT_EXCEPTION, LOGGED_IN, LOG_IN_EXCEPTION, DISCONNECTED;

        public static int getMinValue() {
            return 0;
        }

        public static int getMaxValue() {
            return 3;
        }

        public static State getStateByValue(int value) {
            switch (value) {
                case 3:
                    return DISCONNECTED;
                case 2:
                    return LOGGED_IN;
                case 1:
                    return CONNECTED;
                case 0:
                default:
                    return STOPPED;
            }
        }

        public int getValue() {
            switch (this) {
                case DISCONNECTED:
                    return 3;
                case LOGGED_IN:
                case LOG_IN_EXCEPTION:
                    return 2;
                case CONNECTED:
                case CONNECT_EXCEPTION:
                    return 1;
                case STOPPED:
                default:
                    return 0;
            }
        }

        public boolean isException() {
            return this.equals(CONNECT_EXCEPTION) || this.equals(LOG_IN_EXCEPTION);
        }
    }

    public class SASBinder extends Binder {

        /*public boolean isWorkerRunning() {
            return worker.isWorkerRunning();
        }*/

        public void waitToWorkerStop() throws InterruptedException {
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

        public Thread refresh() {
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    refreshMarks(true, true, false);
                }
            });
        }

        public Lesson[] getLessons(MarksManager.Semester semester) throws InterruptedException {
            waitToWorkerStop();
            return marks.getAsLessons(semester);
        }

        public Mark[] getMarks(MarksManager.Semester semester) throws InterruptedException {
            waitToWorkerStop();
            return marks.get(semester);
        }

        public String getMarksAsString(MarksManager.Semester semester) throws InterruptedException {
            waitToWorkerStop();
            return marks.toString(semester);
        }

    }

}
