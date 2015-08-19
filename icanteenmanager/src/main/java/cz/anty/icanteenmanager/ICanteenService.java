package cz.anty.icanteenmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.WrongLoginDataException;
import cz.anty.utils.icanteen.ICanteenManager;
import cz.anty.utils.icanteen.lunch.BurzaLunch;
import cz.anty.utils.icanteen.lunch.BurzaLunchSelector;
import cz.anty.utils.icanteen.lunch.MonthLunch;
import cz.anty.utils.thread.OnceRunThread;

public class ICanteenService extends Service {

    private static final String EXTRA_STOP_BURZA_CHECKER = "STOP_BURZA_CHECKER";

    private final IBinder mBinder = new MyBinder();
    private final OnceRunThread worker = new OnceRunThread();
    private final OnceRunThread burzaCheckerWorker = new OnceRunThread();
    private ICanteenManager manager;
    private List<BurzaLunch> mBurzaLunchList = null;
    private List<MonthLunch> mMonthLunchList = null;
    private Runnable onBurzaChange = null;
    private Runnable onMonthChange = null;
    private final Runnable onLoginChange = new Runnable() {
        @Override
        public void run() {
            worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    initialize();
                }
            });
        }
    };

    @Override
    public void onCreate() {
        if (AppDataManager.isDebugMode(this)) Log.d("ICanteenService", "onCreate");
        super.onCreate();
        worker.setPowerManager(this);
        burzaCheckerWorker.setPowerManager(this);
        AppDataManager.addOnChangeListener(AppDataManager.Type.I_CANTEEN, onLoginChange);
        worker.waitToWorkerStop(worker.startWorker(
                new Runnable() {
                    @Override
                    public void run() {
                        initialize();
                    }
                }));
    }

    private void initialize() {
        if (AppDataManager.isDebugMode(this)) Log.d("ICanteenService", "initialize");
        if (manager != null && manager.isConnected()) {
            manager.disconnect();
        }

        if (AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN, this)) {
            manager = new ICanteenManager(AppDataManager.getUsername(AppDataManager.Type.I_CANTEEN, this),
                    AppDataManager.getPassword(AppDataManager.Type.I_CANTEEN, this));
            try {
                manager.connect();
                if (!manager.isLoggedIn()) {
                    throw new WrongLoginDataException();
                }
            } catch (IOException e) {
                manager.disconnect();
                manager = null;
                if (e instanceof WrongLoginDataException) {
                    Notification n = new NotificationCompat.Builder(this)
                            .setContentTitle(getString(R.string.notify_title_can_not_login))
                            .setContentText(getString(R.string.notify_text_can_not_login))
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .build();

                    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                            .notify(Constants.NOTIFICATION_ID_I_CANTEEN_LOGIN_EXCEPTION, n);
                }
                stopSelf();
                return;
            }
            refreshBurza();
            refreshMonth();
        } else {
            manager = null;
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra(EXTRA_STOP_BURZA_CHECKER, false)
                && burzaCheckerWorker.isWorkerRunning()) burzaCheckerWorker.stopWorker();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (AppDataManager.isDebugMode(this)) Log.d("ICanteenService", "onDestroy");
        AppDataManager.removeOnChangeListener(AppDataManager.Type.I_CANTEEN, onLoginChange);
        super.onDestroy();
    }

    private void burzaChecker(@NonNull BurzaLunchSelector[] selectors) {
        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.notify_title_burza_checker_running))
                .setContentText(getString(R.string.notify_text_burza_checker_running))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(PendingIntent.getService(this, 0,
                        new Intent(this, ICanteenService.class)
                                .putExtra(EXTRA_STOP_BURZA_CHECKER, true), 0))
                .setAutoCancel(false)
                .setOngoing(true)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .build();

        NotificationManager notificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        notificationManager.notify(Constants.NOTIFICATION_ID_I_CANTEEN_BURZA, n);

        final Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshBurza();
            }
        };
        boolean completed = false;
        while (!Thread.interrupted()) {
            worker.waitToWorkerStop(worker.startWorker(refreshRunnable));
            for (BurzaLunch lunch : mBurzaLunchList)
                for (BurzaLunchSelector selector : selectors)
                    if (selector.isSelected(lunch)) {
                        completed = orderBurza(lunch);
                        Thread.currentThread().interrupt();
                    }
        }

        notificationManager.cancel(Constants.NOTIFICATION_ID_I_CANTEEN_BURZA);

        if (completed) {
            Notification n1 = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.notify_title_burza_checker_completed))
                    .setContentText(getString(R.string.notify_text_burza_checker_completed))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .build();

            notificationManager.notify(Constants.NOTIFICATION_ID_I_CANTEEN_BURZA, n1);
        }
    }

    private boolean refreshBurza() {
        if (AppDataManager.isDebugMode(this)) Log.d("ICanteenService", "refreshBurza");
        try {
            List<BurzaLunch> burzaLunchList = mBurzaLunchList;
            mBurzaLunchList = manager.getBurza();
            if (!listEquals(mBurzaLunchList, burzaLunchList) && onBurzaChange != null)
                onBurzaChange.run();
            return true;
        } catch (IOException | IndexOutOfBoundsException e) {
            Log.d("ICanteenService", "refreshBurza", e);
            return false;
        }
    }

    private boolean refreshMonth() {
        if (AppDataManager.isDebugMode(this)) Log.d("ICanteenService", "refreshMonth");
        try {
            List<MonthLunch> monthLunchList = mMonthLunchList;
            mMonthLunchList = manager.getMonth();
            if (!listEquals(mMonthLunchList, monthLunchList) && onMonthChange != null)
                onMonthChange.run();
            return true;
        } catch (IOException e) {
            Log.d("ICanteenService", "refreshMonth", e);
            return false;
        }
    }

    private boolean orderBurza(BurzaLunch lunch) {
        if (AppDataManager.isDebugMode(this)) Log.d("ICanteenService", "orderBurza");
        try {
            manager.orderBurzaLunch(lunch);
            return true;
        } catch (IOException e) {
            Log.d("ICanteenService", "orderBurza", e);
            return false;
        }
    }

    private boolean orderMonth(MonthLunch lunch) {
        if (AppDataManager.isDebugMode(this)) Log.d("ICanteenService", "orderMonth");
        try {
            manager.orderMonthLunch(lunch);
            return true;
        } catch (IOException e) {
            Log.d("ICanteenService", "orderMonth", e);
            return false;
        }
    }

    private boolean listEquals(List<?> list, List<?> list1) {
        if (AppDataManager.isDebugMode(this)) Log.d("ICanteenService", "listEquals");
        if (list == null) return list1 == null;
        if (list1 == null) return false;
        if (list.size() != list1.size()) return false;
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (!list.get(i).equals(list1.get(i))) return false;
        }
        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (AppDataManager.isDebugMode(this)) Log.d("ICanteenService", "onBind");
        return mBinder;
    }

    public class MyBinder extends Binder {

        public void waitToWorkerStop() {
            worker.waitToWorkerStop();
        }


        public void setOnBurzaChangeListener(@Nullable Runnable onBurzaChange) {
            ICanteenService.this.onBurzaChange = onBurzaChange;
        }

        public void setOnMonthChangeListener(@Nullable Runnable onMonthChange) {
            ICanteenService.this.onMonthChange = onMonthChange;
        }

        public Thread refreshBurza() {
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    refreshBurza();
                }
            });
        }

        public Thread refreshMonth() {
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    refreshMonth();
                }
            });
        }

        public Thread orderBurzaLunch(final BurzaLunch lunch) {
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    orderBurza(lunch);
                }
            });
        }

        public Thread orderMonthLunch(final MonthLunch lunch) {
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    orderMonth(lunch);
                }
            });
        }

        public Thread startBurzaChecker(@NonNull final BurzaLunchSelector[] selectors) {
            if (!isBurzaCheckerRunning()) {
                return burzaCheckerWorker.startWorker(new Runnable() {
                    @Override
                    public void run() {
                        burzaChecker(selectors);
                    }
                });
            }
            return null;
        }

        public boolean isBurzaCheckerRunning() {
            return burzaCheckerWorker.isWorkerRunning();
        }

        public List<BurzaLunch> getBurza() {
            waitToWorkerStop();
            return mBurzaLunchList;
        }

        public List<MonthLunch> getMonth() {
            waitToWorkerStop();
            return mMonthLunchList;
        }
    }
}
