package cz.anty.icanteenmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunch;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunchSelector;
import cz.anty.utils.icanteen.lunch.month.MonthLunchDay;
import cz.anty.utils.thread.OnceRunThread;

public class BurzaCheckerService extends Service {

    public static final String EXTRA_BURZA_CHECKER_SELECTOR_AS_STRING = "BURZA_CHECKER_SELECTOR";

    public static final String EXTRA_BURZA_CHECKER_STATE = "BURZA_CHECKER_STATE";
    public static final String BURZA_CHECKER_STATE_START = "START";
    public static final String BURZA_CHECKER_STATE_STOP = "STOP";
    private final OnceRunThread worker = new OnceRunThread();
    private ICanteenService.MyBinder binder = null;
    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            if (AppDataManager.isDebugMode(BurzaCheckerService.this))
                Log.d("BurzaCheckerService", "onServiceConnected");
            BurzaCheckerService.this.binder = (ICanteenService.MyBinder) binder;
        }

        public void onServiceDisconnected(ComponentName className) {
            if (AppDataManager.isDebugMode(BurzaCheckerService.this))
                Log.d("BurzaCheckerService", "onServiceDisconnected");
            BurzaCheckerService.this.binder = null;
        }

    };

    @Override
    public void onCreate() {
        if (AppDataManager.isDebugMode(this))
            Log.d("BurzaCheckerService", "onCreate");
        super.onCreate();
        worker.setPowerManager(this);
        bindService(new Intent(this, ICanteenService.class),
                mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (AppDataManager.isDebugMode(this))
            Log.d("BurzaCheckerService", "onStartCommand");

        switch (intent.getStringExtra(EXTRA_BURZA_CHECKER_STATE)) {
            case BURZA_CHECKER_STATE_START:
                if (!worker.isWorkerRunning()) {
                    try {
                        final BurzaLunchSelector selector = BurzaLunchSelector
                                .parseBurzaLunchSelector(intent
                                        .getStringExtra(EXTRA_BURZA_CHECKER_SELECTOR_AS_STRING));
                        worker.startWorker(new Runnable() {
                            @Override
                            public void run() {
                                burzaChecker(selector);
                            }
                        });
                    } catch (Exception e) {
                        Log.d("BurzaCheckerService", "onStartCommand BURZA_CHECKER_STATE_START", e);
                        stopSelf();
                    }
                }
                break;
            case BURZA_CHECKER_STATE_STOP:
                if (worker.isWorkerRunning()) {
                    worker.stopActualWorker();
                    break;
                }
                stopSelf();
                break;
            default:
                if (!worker.isWorkerRunning())
                    stopSelf();
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void burzaChecker(@NonNull BurzaLunchSelector selector) {
        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.notify_title_burza_checker_running))
                .setContentText(getString(R.string.notify_text_burza_checker_running))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(PendingIntent.getService(this, 0,
                        new Intent(this, BurzaCheckerService.class)
                                .putExtra(EXTRA_BURZA_CHECKER_STATE, BURZA_CHECKER_STATE_STOP), 0))
                .setAutoCancel(false)
                .setOngoing(true)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .build();

        startForeground(Constants.NOTIFICATION_ID_I_CANTEEN_BURZA, n);

        boolean completed = false;
        while (!Thread.interrupted() && binder != null) {
            binder.refreshBurza();
            for (BurzaLunch lunch : binder.getBurza())
                if (selector.isSelected(lunch)) {
                    binder.orderBurzaLunch(lunch);
                    binder.refreshMonth();
                    for (MonthLunchDay lunchDay : binder.getMonth()) {
                        if (lunchDay.getDate().getTime() == lunch.getDate().getTime()
                                && lunchDay.getOrderedLunch() != null) {
                            completed = true;
                            break;
                        }
                    }
                    Thread.currentThread().interrupt();
                }
        }

        stopForeground(true);

        if (completed) {
            Notification n1 = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.notify_title_burza_checker_completed))
                    .setContentText(getString(R.string.notify_text_burza_checker_completed))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .build();

            NotificationManager notificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
            notificationManager.notify(Constants.NOTIFICATION_ID_I_CANTEEN_BURZA, n1);
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if (AppDataManager.isDebugMode(this))
            Log.d("BurzaCheckerService", "onDestroy");
        unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (AppDataManager.isDebugMode(this))
            Log.d("BurzaCheckerService", "onBind");
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
}
