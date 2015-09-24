package cz.anty.icanteenmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Binder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import java.io.IOException;
import java.util.List;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.WrongLoginDataException;
import cz.anty.utils.icanteen.ICManager;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunch;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunchSelector;
import cz.anty.utils.icanteen.lunch.month.MonthLunch;
import cz.anty.utils.icanteen.lunch.month.MonthLunchDay;
import cz.anty.utils.service.BindImplService;
import cz.anty.utils.thread.OnceRunThread;

public class ICService extends BindImplService<ICService.ICBinder> {

    private final ICBinder mBinder = new ICBinder();
    private final OnceRunThread worker = new OnceRunThread();
    private ICManager mManager;
    private List<BurzaLunch> mBurzaLunchList = null;
    private List<MonthLunchDay> mMonthLunchList = null;
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
                .setContentTitle(getString(R.string.notify_title_can_not_login))
                .setContentText(getString(R.string.notify_text_can_not_login))
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
            mBurzaLunchList = null;
            mMonthLunchList = null;
            if (onBurzaChange != null)
                onBurzaChange.run();
            if (onMonthChange != null)
                onMonthChange.run();
        }
        super.onDestroy();
    }

    private boolean refreshBurza() {
        Log.d(getClass().getSimpleName(), "refreshBurza startStage: " + mBurzaLunchList);
        try {
            List<BurzaLunch> burzaLunchList = mBurzaLunchList;
            mBurzaLunchList = mManager != null ? mManager.getBurza() : mBurzaLunchList;
            if (onBurzaChange != null && !(mBurzaLunchList != null
                    ? mBurzaLunchList.equals(burzaLunchList) : burzaLunchList == null))
                onBurzaChange.run();
            Log.d(getClass().getSimpleName(), "refreshBurza finalStage: " + mBurzaLunchList);
            return true;
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "refreshBurza", e);
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
            return false;
        }
    }

    private boolean refreshMonth() {
        Log.d(getClass().getSimpleName(), "refreshMonth startStage: " + mMonthLunchList);
        try {
            List<MonthLunchDay> monthLunchList = mMonthLunchList;
            mMonthLunchList = mManager != null ? mManager.getMonth() : mMonthLunchList;
            if (onMonthChange != null && !(mMonthLunchList != null
                    ? mMonthLunchList.equals(monthLunchList) : monthLunchList == null))
                onMonthChange.run();
            Log.d(getClass().getSimpleName(), "refreshMonth finalStage: " + mMonthLunchList);
            return true;
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "refreshMonth", e);
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
            return false;
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

        public List<BurzaLunch> getBurza(Thread refreshTread) throws InterruptedException {
            worker.waitToWorkerStop(refreshTread);
            return mBurzaLunchList;
        }

        public List<BurzaLunch> getBurza() throws InterruptedException {
            worker.waitToWorkerStop();
            return mBurzaLunchList;
        }

        public List<MonthLunchDay> getMonth(Thread refreshTread) throws InterruptedException {
            worker.waitToWorkerStop(refreshTread);
            return mMonthLunchList;
        }

        public List<MonthLunchDay> getMonth() throws InterruptedException {
            worker.waitToWorkerStop();
            return mMonthLunchList;
        }
    }
}
