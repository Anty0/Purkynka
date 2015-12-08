package cz.anty.purkynkamanager.modules.sas;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.sas.receiver.StartServiceScheduleReceiver;
import cz.anty.purkynkamanager.modules.sas.widget.SASManageWidget;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.WrongLoginDataException;
import cz.anty.purkynkamanager.utils.other.sas.SASManager;
import cz.anty.purkynkamanager.utils.other.sas.mark.Lesson;
import cz.anty.purkynkamanager.utils.other.sas.mark.Mark;
import cz.anty.purkynkamanager.utils.other.sas.mark.MarksManager;
import cz.anty.purkynkamanager.utils.other.service.BindImplService;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThread;

public class SASManagerService extends BindImplService<SASManagerService.SASBinder> {

    public static final String FORCE_UPDATE_WIDGET = "UPDATE_WIDGET";
    public static final String FORCE_UPDATE_MARKS = "UPDATE_MARKS";

    private final SASBinder mBinder = new SASBinder();
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
            final boolean updateMarks = intent.getBooleanExtra(FORCE_UPDATE_MARKS, false);
            worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    if (refreshMarks(updateMarks, false, updateWidget)) {
                        SASManageWidget.callUpdate(SASManagerService.this, marks.toString());
                    }
                    sendBroadcast(new Intent(SASManagerService.this, StartServiceScheduleReceiver.class));
                }
            });
        }
        return START_NOT_STICKY;
    }

    private boolean refreshMarks(boolean force, boolean deepRefresh, boolean updateWidget) {
        Log.d("SASManagerService", "refreshMarks: force=" + force +
                " deep=" + deepRefresh + " updateWidget=" + updateWidget);
        if (sasManager == null) return updateWidget;
        if (!force && System.currentTimeMillis() - getSharedPreferences(Constants.SETTINGS_NAME_MARKS, MODE_PRIVATE)
                .getLong(Constants.SETTING_NAME_LAST_REFRESH, 0) < Constants.WAIT_TIME_SAS_MARKS_REFRESH) {
            setState(State.LOGGED_IN);
            return updateWidget;
        }
        try {
            if (!sasManager.isConnected()) {
                sasManager.connect();
                //setState(State.CONNECTED);
                setState(State.LOGGED_IN);
            }
            try {
                MarksManager.Semester semester = MarksManager.Semester.AUTO.getStableSemester();
                List<Mark> newMarks = sasManager.getMarks(semester);
                List<Mark> oldMarks = Arrays.asList(marks.get(semester));
                List<Mark> changes = new ArrayList<>();
                for (Mark mark : newMarks) {
                    int index = oldMarks.indexOf(mark);
                    if (index == -1) {
                        changes.add(mark);
                        continue;
                    }
                    mark.setValueModification(oldMarks
                            .get(index).getValueModification());
                }
                //System.out.println("old: " + marks.get(semester).length + " new: " + newMarks.size());
                marks.clear(semester);
                marks.addAll(newMarks, semester);
                marks.apply(semester);
                if (changes.size() > 0) {
                    onMarksChange(changes);
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

    private void onMarksChange(@Nullable List<Mark> changedMarks) {
        Log.d("SASManagerService", "onMarksChange");
        if (onMarksChange != null)
            onMarksChange.run();
        SASManageWidget.callUpdate(this, marks.toString());

        if (!AppDataManager.isSASMarksAutoUpdate())
            return;

        if (changedMarks == null) return;

        StringBuilder builderBig = new StringBuilder();
        builderBig.append(changedMarks.get(0));
        for (int i = 1; i < changedMarks.size(); i++) {
            builderBig.append("\n").append(changedMarks.get(i));
        }

        StringBuilder builder = new StringBuilder(getText(R.string.text_from) + ": ");
        builder.append(changedMarks.get(0).getShortLesson());
        for (int i = 1; i < changedMarks.size(); i++) {
            builder.append(", ").append(changedMarks.get(i).getShortLesson());
        }

        // prepare intent which is triggered if the
        // notification is selected

        Intent intent = new Intent(this, SASSplashActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // build notification
        // the addAction re-use the same intent to keep the example short
        int size = changedMarks.size();
        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getQuantityString(R.plurals
                        .notify_title_new_marks, size, size))
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
    public SASBinder getBinder() {
        Log.d("SASManagerService", "getBinder");
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

        public Lesson[] getLessonsFast(MarksManager.Semester semester) {
            return marks.getAsLessons(semester);
        }

        public Mark[] getMarks(MarksManager.Semester semester) throws InterruptedException {
            waitToWorkerStop();
            return marks.get(semester);
        }

        public Mark[] getMarksFast(MarksManager.Semester semester) {
            return marks.get(semester);
        }

        public String getMarksAsString(MarksManager.Semester semester) throws InterruptedException {
            waitToWorkerStop();
            return marks.toString(semester);
        }

        public void saveChanges(MarksManager.Semester semester) {
            marks.apply(semester);
            onMarksChange(null);
        }

    }

}
