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
import android.util.Log;

import java.io.IOException;
import java.util.List;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.WrongLoginDataException;
import cz.anty.utils.icanteen.ICanteenManager;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunch;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunchSelector;
import cz.anty.utils.icanteen.lunch.month.MonthLunch;
import cz.anty.utils.icanteen.lunch.month.MonthLunchDay;
import cz.anty.utils.thread.OnceRunThread;

public class ICanteenService extends Service {

    private final IBinder mBinder = new MyBinder();
    private final OnceRunThread worker = new OnceRunThread();
    private ICanteenManager manager;
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
        if (AppDataManager.isDebugMode(this)) Log.d("ICanteenService", "onCreate");
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
                            .setSmallIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon iC
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
    public void onDestroy() {
        if (AppDataManager.isDebugMode(this)) Log.d("ICanteenService", "onDestroy");
        try {
            worker.waitToWorkerStop();
        } catch (InterruptedException e) {
            Log.d("ICanteenService", "onDestroy", e);
        }
        AppDataManager.removeOnChangeListener(AppDataManager.Type.I_CANTEEN, onLoginChange);
        super.onDestroy();
    }

    private boolean refreshBurza() {
        if (AppDataManager.isDebugMode(this))
            Log.d("ICanteenService", "refreshBurza startStage: " + mBurzaLunchList);
        try {
            List<BurzaLunch> burzaLunchList = mBurzaLunchList;
            mBurzaLunchList = manager.getBurza();
            if (!listEquals(mBurzaLunchList, burzaLunchList) && onBurzaChange != null)
                onBurzaChange.run();
            if (AppDataManager.isDebugMode(this))
                Log.d("ICanteenService", "refreshBurza finalStage: " + mBurzaLunchList);
            return true;
        } catch (IOException | IndexOutOfBoundsException e) {
            Log.d("ICanteenService", "refreshBurza", e);
            return false;
        }
    }

    private boolean refreshMonth() {
        if (AppDataManager.isDebugMode(this))
            Log.d("ICanteenService", "refreshMonth startStage: " + mMonthLunchList);
        try {
            List<MonthLunchDay> monthLunchList = mMonthLunchList;
            mMonthLunchList = manager.getMonth();
            if (!listEquals(mMonthLunchList, monthLunchList) && onMonthChange != null)
                onMonthChange.run();
            if (AppDataManager.isDebugMode(this))
                Log.d("ICanteenService", "refreshMonth finalStage: " + mMonthLunchList);
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

        public List<BurzaLunch> getBurza() throws InterruptedException {
            waitToWorkerStop();
            return mBurzaLunchList;
        }

        public List<MonthLunchDay> getMonth() throws InterruptedException {
            waitToWorkerStop();
            return mMonthLunchList;
        }
    }
}
