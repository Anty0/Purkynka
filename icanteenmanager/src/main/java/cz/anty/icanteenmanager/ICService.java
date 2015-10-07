package cz.anty.icanteenmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.WrongLoginDataException;
import cz.anty.utils.icanteen.ICManager;
import cz.anty.utils.icanteen.lunch.LunchesManager;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunch;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunchSelector;
import cz.anty.utils.icanteen.lunch.month.MonthLunch;
import cz.anty.utils.icanteen.lunch.month.MonthLunchDay;
import cz.anty.utils.service.BindImplService;
import cz.anty.utils.thread.OnceRunThread;

public class ICService extends BindImplService<ICService.ICBinder> {

    public static final String EXTRA_UPDATE_MONTH = "UPDATE_MONTH";

    private final ICBinder mBinder = new ICBinder();
    private final OnceRunThread worker = new OnceRunThread();
    private ICManager mManager;
    private LunchesManager mLunchesManager;
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
        Log.d(getClass().getSimpleName(), "onCreate");
        super.onCreate();
        worker.setPowerManager(this);
        mLunchesManager = new LunchesManager(this);
        AppDataManager.addOnChangeListener(AppDataManager.Type.I_CANTEEN, onLoginChange);
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                initialize();
            }
        });
    }

    private void initialize() {
        Log.d(getClass().getSimpleName(), "initialize");
        if (mManager != null && mManager.isConnected()) {
            mManager.disconnect();
        }

        if (AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN)) {
            mManager = new ICManager(AppDataManager.getUsername(AppDataManager.Type.I_CANTEEN),
                    AppDataManager.getPassword(AppDataManager.Type.I_CANTEEN));
            try {
                mManager.connect();
                if (!mManager.isLoggedIn()) {
                    throw new WrongLoginDataException();
                }
                cancelWrongLoginDataNotification();
            } catch (IOException e) {
                mManager.disconnect();
                mManager = null;
                if (e instanceof WrongLoginDataException) {
                    onWrongLoginData();
                    stopSelf();
                    return;
                }
            }
            refreshBurza();
            refreshMonth();
        } else {
            mManager = null;
            stopSelf();
        }
    }

    private void onWrongLoginData() {
        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle(getText(R.string.notify_title_can_not_login))
                .setContentText(getText(R.string.notify_text_can_not_login))
                .setSmallIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon iC
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .notify(Constants.NOTIFICATION_ID_I_CANTEEN_LOGIN_EXCEPTION, n);

    }

    private void cancelWrongLoginDataNotification() {
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .cancel(Constants.NOTIFICATION_ID_I_CANTEEN_LOGIN_EXCEPTION);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getBooleanExtra
                (EXTRA_UPDATE_MONTH, false))
            refreshMonth();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(getClass().getSimpleName(), "onDestroy");
        if (ICSplashActivity.serviceManager != null)
            ICSplashActivity.serviceManager.forceDisconnect();

        AppDataManager.removeOnChangeListener(AppDataManager.Type.I_CANTEEN, onLoginChange);
        synchronized (worker.getWorkerLock()) {
            try {
                worker.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d(getClass().getSimpleName(), "onDestroy", e);
            }

            if (mManager != null) {
                mManager.disconnect();
                mManager = null;
            }
            mLunchesManager = null;
            if (onBurzaChange != null)
                onBurzaChange.run();
            if (onMonthChange != null)
                onMonthChange.run();
        }
        super.onDestroy();
    }

    private boolean refreshBurza() {
        Log.d(getClass().getSimpleName(), "refreshBurza");
        try {
            if (mLunchesManager == null) return false;
            BurzaLunch[] oldLunches = mLunchesManager.getBurzaLunches();
            List<BurzaLunch> newLunches = mManager != null ? mManager.getBurza() : null;
            mLunchesManager.setItems(newLunches != null ? newLunches.toArray(
                    new BurzaLunch[newLunches.size()]) : oldLunches);
            if (onBurzaChange != null && !Arrays.equals(oldLunches, mLunchesManager.getBurzaLunches()))
                onBurzaChange.run();
            return true;
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "refreshBurza", e);
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
            return false;
        }
    }

    private boolean refreshMonth() {
        Log.d(getClass().getSimpleName(), "refreshMonth");
        try {
            List<MonthLunchDay> newLunchDays = mManager != null ? mManager.getMonth() : null;
            if (newLunchDays != null) {
                if (mLunchesManager.setItems(newLunchDays
                        .toArray(new MonthLunchDay[newLunchDays.size()]))) {
                    onNewMonthLunches();
                }
                if (onMonthChange != null)
                    onMonthChange.run();
            }
            return true;
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "refreshMonth", e);
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
            return false;
        }
    }

    private void onNewMonthLunches() {
        AppDataManager.setICNewMonthLunches(true);

        if (AppDataManager.isICNotifyNewMonthLunches()) {
            PendingIntent pIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, ICSplashActivity.class), 0);

            Notification n = new NotificationCompat.Builder(this)
                    .setContentTitle(getText(R.string.notify_title_new_lunches))
                    .setContentText(getText(R.string.notify_text_new_lunches))
                            //.setSmallIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon iC
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .build();

            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                    .notify(Constants.NOTIFICATION_ID_I_CANTEEN_MONTH, n);
        }
    }


    private boolean orderBurza(BurzaLunch lunch) {
        Log.d(getClass().getSimpleName(), "orderBurza");
        try {
            if (mManager != null) {
                mManager.orderBurzaLunch(lunch);
                return true;
            }
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "orderBurza", e);
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
        }
        return false;
    }

    private boolean orderMonth(MonthLunch lunch) {
        Log.d(getClass().getSimpleName(), "orderMonth");
        try {
            if (mManager != null) {
                mManager.orderMonthLunch(lunch);
                return true;
            }
        } catch (Exception e) {
            Log.d("ICService", "orderMonth", e);
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
        }
        return false;
    }

    private boolean toBurzaMonth(MonthLunch lunch) {
        Log.d(getClass().getSimpleName(), "toBurzaMonth");
        try {
            if (mManager != null) {
                mManager.toBurzaMonthLunch(lunch);
                return true;
            }
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "toBurzaMonth", e);
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
        }
        return false;
    }

    @Override
    public ICBinder getBinder() {
        Log.d(getClass().getSimpleName(), "getBinder");
        return mBinder;
    }

    public class ICBinder extends Binder {

        public void waitToWorkerStop() throws InterruptedException {
            worker.waitToWorkerStop();
        }

        public void setOnBurzaChangeListener(@Nullable Runnable onBurzaChange) {
            ICService.this.onBurzaChange = onBurzaChange;
        }

        public void setOnMonthChangeListener(@Nullable Runnable onMonthChange) {
            ICService.this.onMonthChange = onMonthChange;
        }

        public Thread refreshBurza() {
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    ICService.this.refreshBurza();
                }
            });
        }

        public Thread refreshMonth() {
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    ICService.this.refreshMonth();
                }
            });
        }

        public Thread orderBurzaLunch(final BurzaLunch lunch) {
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    orderBurza(lunch);
                    ICService.this.refreshBurza();
                    ICService.this.refreshMonth();
                }
            });
        }

        public Thread orderMonthLunch(final MonthLunch lunch) {
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    orderMonth(lunch);
                    ICService.this.refreshMonth();
                }
            });
        }

        public Thread toBurzaMonthLunch(final MonthLunch lunch) {
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    toBurzaMonth(lunch);
                    ICService.this.refreshMonth();
                }
            });
        }

        public void startBurzaChecker(@NonNull final BurzaLunchSelector selector) {
            startService(new Intent(ICService.this, ICBurzaCheckerService.class)
                            .putExtra(ICBurzaCheckerService.EXTRA_BURZA_CHECKER_STATE, ICBurzaCheckerService.BURZA_CHECKER_STATE_START)
                            .putExtra(ICBurzaCheckerService.EXTRA_BURZA_CHECKER_SELECTOR_AS_STRING, selector.toString())
            );
        }

        public BurzaLunch[] getBurza(Thread refreshTread) throws InterruptedException {
            worker.waitToWorkerStop(refreshTread);
            if (mLunchesManager == null) return null;
            return mLunchesManager.getBurzaLunches();
        }

        public BurzaLunch[] getBurza() throws InterruptedException {
            worker.waitToWorkerStop();
            if (mLunchesManager == null) return null;
            return mLunchesManager.getBurzaLunches();
        }

        public MonthLunchDay[] getMonth(Thread refreshTread) throws InterruptedException {
            worker.waitToWorkerStop(refreshTread);
            if (mLunchesManager == null) return null;
            return mLunchesManager.getMonthLunches();
        }

        public MonthLunchDay[] getMonth() throws InterruptedException {
            worker.waitToWorkerStop();
            if (mLunchesManager == null) return null;
            return mLunchesManager.getMonthLunches();
        }
    }
}
