package cz.anty.icanteenmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunch;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunchSelector;
import cz.anty.utils.icanteen.lunch.month.MonthLunchDay;
import cz.anty.utils.service.ServiceManager;
import cz.anty.utils.thread.OnceRunThread;

public class ICBurzaCheckerService extends Service {

    public static final String EXTRA_BURZA_CHECKER_SELECTOR_AS_STRING = "BURZA_CHECKER_SELECTOR";

    public static final String EXTRA_BURZA_CHECKER_STATE = "BURZA_CHECKER_STATE";
    public static final String BURZA_CHECKER_STATE_START = "START";
    public static final String BURZA_CHECKER_STATE_STOP = "STOP";

    private final OnceRunThread worker = new OnceRunThread();
    private ICService.ICBinder binder = null;
    private ServiceManager.BinderConnection<ICService.ICBinder> binderConnection
            = new ServiceManager.BinderConnection<ICService.ICBinder>() {
        @Override
        public void onBinderConnected(ICService.ICBinder ICBinder) {
            Log.d("BurzaCheckerService", "onBinderConnected");
            binder = ICBinder;

        }

        @Override
        public void onBinderDisconnected() {
            Log.d("BurzaCheckerService", "onBinderDisconnected");
            binder = null;
        }
    };

    @Override
    public void onCreate() {
        Log.d("BurzaCheckerService", "onCreate");
        super.onCreate();

        worker.setPowerManager(this);
        if (!worker.isWorkerRunning())
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                    .cancel(Constants.NOTIFICATION_ID_I_CANTEEN_BURZA);

        if (ICSplashActivity.serviceManager != null) {
            ICSplashActivity.serviceManager
                    .addBinderConnection(binderConnection);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BurzaCheckerService", "onStartCommand");

        if (intent != null)
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
                            break;
                        } catch (Exception e) {
                            Log.d("BurzaCheckerService", "onStartCommand EXTRA_BURZA_CHECKER_STATE", e);
                            stopSelf();
                        }
                    }
                    Toast.makeText(this, R.string.toast_text_can_not_start_burza_checker, Toast.LENGTH_LONG).show();
                    break;
                case BURZA_CHECKER_STATE_STOP:
                    if (worker.isWorkerRunning()) {
                        worker.stopActualWorker();
                        break;
                    }
                    stopSelf();
                    break;
                default:
                    if (!worker.isWorkerRunning()) {
                        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                                .cancel(Constants.NOTIFICATION_ID_I_CANTEEN_BURZA);
                        stopSelf();
                    }
                    break;
            }

        return START_STICKY;
    }

    private void burzaChecker(@NonNull BurzaLunchSelector selector) {
        Log.d("BurzaCheckerService", "burzaChecker");

        NotificationManager notificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        notificationManager.cancel(Constants.NOTIFICATION_ID_I_CANTEEN_BURZA_RESULT);

        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.notify_title_burza_checker_running))
                .setContentText(getString(R.string.notify_text_burza_checker_running))
                .setSmallIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon iC
                .setContentIntent(PendingIntent.getService(this, 0,
                        new Intent(this, ICBurzaCheckerService.class)
                                .putExtra(EXTRA_BURZA_CHECKER_STATE, BURZA_CHECKER_STATE_STOP), 0))
                .setAutoCancel(false)
                .setOngoing(true)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .build();

        startForeground(Constants.NOTIFICATION_ID_I_CANTEEN_BURZA, n);

        boolean completed = false;
        while (!Thread.interrupted() && binder != null) {
            try {
                for (BurzaLunch lunch : binder.getBurza(binder.refreshBurza()))
                    if (selector.isSelected(lunch)) {
                        binder.orderBurzaLunch(lunch);
                        completed = true;
                        Thread.currentThread().interrupt();
                        break;
                    }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        stopForeground(true);

        boolean successfully = false;
        if (binder != null) {
            try {
                for (MonthLunchDay lunchDay : binder.getMonth(binder.refreshMonth())) {
                    if (selector.isSelected(lunchDay)
                            && lunchDay.getOrderedLunch() != null) {
                        Notification n1 = new NotificationCompat.Builder(this)
                                .setContentTitle(getString(R.string.notify_title_burza_checker_completed))
                                .setContentText(getString(R.string.notify_text_burza_checker_completed))
                                .setSmallIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon iC
                                .setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .build();

                        notificationManager.notify(Constants.NOTIFICATION_ID_I_CANTEEN_BURZA_RESULT, n1);
                        successfully = true;
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (!completed || successfully) stopSelf();
        else burzaChecker(selector);
    }

    @Override
    public void onDestroy() {
        Log.d("BurzaCheckerService", "onDestroy");

        if (worker.isWorkerRunning())
            worker.stopActualWorker();

        if (ICSplashActivity.serviceManager != null) {
            ICSplashActivity.serviceManager
                    .removeBinderConnection(binderConnection);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("BurzaCheckerService", "onBind");
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
}
