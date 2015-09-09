package cz.anty.icanteenmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import java.io.IOException;
import java.util.List;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.WrongLoginDataException;
import cz.anty.utils.icanteen.ICanteenManager;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunch;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunchSelector;
import cz.anty.utils.icanteen.lunch.month.MonthLunch;
import cz.anty.utils.icanteen.lunch.month.MonthLunchDay;
import cz.anty.utils.thread.OnceRunThread;

public class ICanteenService extends Service {

    private final MyBinder mBinder = new MyBinder();
    private final OnceRunThread worker = new OnceRunThread();
    private ICanteenManager mManager;
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
        Log.d("ICanteenService", "onCreate");
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
        Log.d("ICanteenService", "initialize");
        if (mManager != null && mManager.isConnected()) {
            mManager.disconnect();
        }

        if (AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN)) {
            mManager = new ICanteenManager(AppDataManager.getUsername(AppDataManager.Type.I_CANTEEN),
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
    public void onDestroy() {
        Log.d("ICanteenService", "onDestroy");
        AppDataManager.removeOnChangeListener(AppDataManager.Type.I_CANTEEN, onLoginChange);
        synchronized (worker.getWorkerLock()) {
            try {
                worker.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d("ICanteenService", "onDestroy", e);
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
        Log.d("ICanteenService", "refreshBurza startStage: " + mBurzaLunchList);
        try {
            List<BurzaLunch> burzaLunchList = mBurzaLunchList;
            mBurzaLunchList = mManager != null ? mManager.getBurza() : mBurzaLunchList;
            if (onBurzaChange != null && (mBurzaLunchList != null
                    ? mBurzaLunchList.equals(burzaLunchList) : burzaLunchList != null))
                onBurzaChange.run();
            Log.d("ICanteenService", "refreshBurza finalStage: " + mBurzaLunchList);
            return true;
        } catch (IOException | IndexOutOfBoundsException e) {
            Log.d("ICanteenService", "refreshBurza", e);
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
            return false;
        }
    }

    private boolean refreshMonth() {
        Log.d("ICanteenService", "refreshMonth startStage: " + mMonthLunchList);
        try {
            List<MonthLunchDay> monthLunchList = mMonthLunchList;
            mMonthLunchList = mManager != null ? mManager.getMonth() : mMonthLunchList;
            if (onMonthChange != null && (mMonthLunchList != null
                    ? mMonthLunchList.equals(monthLunchList) : monthLunchList != null))
                onMonthChange.run();
            Log.d("ICanteenService", "refreshMonth finalStage: " + mMonthLunchList);
            return true;
        } catch (IOException e) {
            Log.d("ICanteenService", "refreshMonth", e);
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
            return false;
        }
    }

    private boolean orderBurza(BurzaLunch lunch) {
        Log.d("ICanteenService", "orderBurza");
        try {
            if (mManager != null) {
                mManager.orderBurzaLunch(lunch);
                return true;
            }
        } catch (IOException e) {
            Log.d("ICanteenService", "orderBurza", e);
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
        }
        return false;
    }

    private boolean orderMonth(MonthLunch lunch) {
        Log.d("ICanteenService", "orderMonth");
        try {
            if (mManager != null) {
                mManager.orderMonthLunch(lunch);
                return true;
            }
        } catch (IOException e) {
            Log.d("ICanteenService", "orderMonth", e);
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
        }
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("ICanteenService", "onBind");
        return mBinder;
    }

    public class MyBinder extends Binder {

        public void waitToWorkerStop() throws InterruptedException {
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
                    ICanteenService.this.refreshBurza();
                }
            });
        }

        public Thread refreshMonth() {
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    ICanteenService.this.refreshMonth();
                }
            });
        }

        public Thread orderBurzaLunch(final BurzaLunch lunch) {
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    orderBurza(lunch);
                    ICanteenService.this.refreshBurza();
                }
            });
        }

        public Thread orderMonthLunch(final MonthLunch lunch) {
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    orderMonth(lunch);
                    ICanteenService.this.refreshMonth();
                }
            });
        }

        public void startBurzaChecker(@NonNull final BurzaLunchSelector selector) {
            startService(new Intent(ICanteenService.this, ICanteenBurzaCheckerService.class)
                            .putExtra(ICanteenBurzaCheckerService.EXTRA_BURZA_CHECKER_STATE, ICanteenBurzaCheckerService.BURZA_CHECKER_STATE_START)
                            .putExtra(ICanteenBurzaCheckerService.EXTRA_BURZA_CHECKER_SELECTOR_AS_STRING, selector.toString())
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
