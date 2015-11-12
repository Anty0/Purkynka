package cz.anty.purkynkamanager.modules.icanteen;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.icanteen.widget.ICTodayLunchWidget;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.WrongLoginDataException;
import cz.anty.purkynkamanager.utils.other.icanteen.ICManager;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.LunchesManager;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.burza.BurzaLunch;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.burza.BurzaLunchSelector;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunch;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunchDay;
import cz.anty.purkynkamanager.utils.other.service.BindImplService;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThread;

public class ICService extends BindImplService<ICService.ICBinder> {

    public static final String EXTRA_UPDATE_MONTH = "UPDATE_MONTH";
    private static final String LOG_TAG = "ICService";
    private final ICBinder mBinder = new ICBinder();
    private final OnceRunThread worker = new OnceRunThread();
    private final Object mManagerLock = new Object();
    private Handler mHandler;
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
                    initialize(true);
                }
            });
        }
    };

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate();
        worker.setPowerManager(this);
        mHandler = new Handler(getMainLooper());
        mLunchesManager = new LunchesManager(this);
        AppDataManager.addOnChangeListener(AppDataManager.Type.I_CANTEEN, onLoginChange);
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                initialize(true);
            }
        });
    }

    private void initialize(boolean callRefresh) {
        Log.d(LOG_TAG, "initialize");
        synchronized (mManagerLock) {
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
                if (callRefresh) {
                    mLunchesManager.tryProcessOrders();
                    refreshBurza(true);
                    refreshMonth(true);
                }
            } else {
                mManager = null;
                stopSelf();
            }
        }
    }

    private void onWrongLoginData() {
        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle(getText(R.string.notify_title_can_not_login))
                .setContentText(getText(R.string.notify_text_can_not_login))
                .setSmallIcon(R.mipmap.ic_launcher_ic)
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
        if (intent != null && intent.getBooleanExtra(EXTRA_UPDATE_MONTH, false))
            worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    if (mLunchesManager != null)
                        mLunchesManager.tryProcessOrders();
                    ICService.this.refreshMonth(true);
                }
            });
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        if (ICSplashActivity.serviceManager != null)
            ICSplashActivity.serviceManager.forceDisconnect();

        AppDataManager.removeOnChangeListener(AppDataManager.Type.I_CANTEEN, onLoginChange);
        synchronized (worker.getWorkerLock()) {
            try {
                worker.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d(LOG_TAG, "onDestroy", e);
            }

            synchronized (mManagerLock) {
                if (mManager != null) {
                    mManager.disconnect();
                    mManager = null;
                }
            }
            if (mLunchesManager != null) {
                mLunchesManager.apply();
                mLunchesManager = null;
            }
            ICTodayLunchWidget.callUpdate(this, false);
            if (onBurzaChange != null)
                onBurzaChange.run();
            if (onMonthChange != null)
                onMonthChange.run();
        }
        super.onDestroy();
    }

    private boolean refreshBurza(boolean silent) {
        Log.d(LOG_TAG, "refreshBurza");
        try {
            if (mLunchesManager == null) throw new NullPointerException("mLunchesManager is null");
            BurzaLunch[] oldLunches;
            List<BurzaLunch> newLunches;
            synchronized (mManagerLock) {
                if (mManager == null) initialize(false);
                oldLunches = mLunchesManager.getBurzaLunches();
                newLunches = mManager != null ? mManager.getBurza() : null;
            }
            mLunchesManager.setItems(newLunches != null ? newLunches.toArray(
                    new BurzaLunch[newLunches.size()]) : oldLunches);
            if (onBurzaChange != null && !Arrays
                    .equals(oldLunches, mLunchesManager.getBurzaLunches()))
                onBurzaChange.run();
            return true;
        } catch (Exception e) {
            Log.d(LOG_TAG, "refreshBurza", e);
            if (!silent)
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ICService.this, R.string.toast_text_can_not_refresh_lunches,
                                Toast.LENGTH_LONG).show();// TODO: 12.11.2015 better toast texts
                    }
                });
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
            return false;
        }
    }

    private boolean refreshMonth(boolean silent) {
        Log.d(LOG_TAG, "refreshMonth");
        try {
            if (mLunchesManager == null) throw new NullPointerException("mLunchesManager is null");
            List<MonthLunchDay> newLunchDays;
            synchronized (mManagerLock) {
                if (mManager == null) initialize(false);
                newLunchDays = mManager != null ? mManager.getMonth() : null;
            }
            if (newLunchDays != null) {
                if (mLunchesManager.setItems(newLunchDays
                        .toArray(new MonthLunchDay[newLunchDays.size()]))) {
                    onNewMonthLunches();
                }
                if (mLunchesManager != null)
                    mLunchesManager.apply();
                if (onMonthChange != null)
                    onMonthChange.run();
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.d(LOG_TAG, "refreshMonth", e);
            if (!silent)
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ICService.this, R.string.toast_text_can_not_refresh_lunches,
                                Toast.LENGTH_LONG).show();// TODO: 12.11.2015 better toast texts
                    }
                });
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
            return false;
        } finally {
            ICTodayLunchWidget.callUpdate(this, false);
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
                    .setSmallIcon(R.mipmap.ic_launcher_ic)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .build();

            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                    .notify(Constants.NOTIFICATION_ID_I_CANTEEN_MONTH, n);
        }
    }


    private boolean orderBurza(BurzaLunch lunch) {
        Log.d(LOG_TAG, "orderBurza");
        try {
            if (mLunchesManager == null) throw new NullPointerException("mLunchesManager is null");
            synchronized (mManagerLock) {
                if (mManager == null) initialize(false);
                if (mManager != null) {
                    mManager.orderBurzaLunch(lunch);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "orderBurza", e);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ICService.this, R.string.toast_text_can_not_order_lunch,
                            Toast.LENGTH_LONG).show();// TODO: 12.11.2015 better toast texts
                }
            });
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
        }
        return false;
    }

    private boolean orderMonth(MonthLunch lunch) {
        Log.d(LOG_TAG, "orderMonth");
        try {
            if (mLunchesManager == null) throw new NullPointerException("mLunchesManager is null");
            synchronized (mManagerLock) {
                if (mManager == null) initialize(true);
                if (mManager != null) {
                    mManager.orderMonthLunch(lunch);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.d("ICService", "orderMonth", e);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ICService.this, R.string.toast_text_can_not_order_lunch,
                            Toast.LENGTH_LONG).show();// TODO: 12.11.2015 better toast texts
                }
            });
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
        }
        return false;
    }

    private boolean toBurzaMonth(MonthLunch lunch) {
        Log.d(LOG_TAG, "toBurzaMonth");
        try {
            if (mLunchesManager == null) throw new NullPointerException("mLunchesManager is null");
            synchronized (mManagerLock) {
                if (mManager == null) initialize(true);
                if (mManager != null) {
                    mManager.toBurzaMonthLunch(lunch);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "toBurzaMonth", e);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ICService.this, R.string.toast_text_can_not_order_lunch,
                            Toast.LENGTH_LONG).show();// TODO: 12.11.2015 better toast texts
                }
            });
            if (e instanceof WrongLoginDataException)
                onWrongLoginData();
        }
        return false;
    }

    @Override
    public ICBinder getBinder() {
        Log.d(LOG_TAG, "getBinder");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mLunchesManager != null)
            mLunchesManager.apply();

        return super.onUnbind(intent);
    }

    public static class MonthLunchOrderRequest
            extends LunchesManager.SimpleLunchOrderRequest {
        private final MonthLunch mMonthLunch;

        private MonthLunchOrderRequest(MonthLunch lunch) {
            mMonthLunch = lunch;
        }

        @Override
        public boolean doOrder() throws Throwable {
            return ICSplashActivity.serviceManager != null &&
                    ICSplashActivity.serviceManager.isConnected() &&
                    ICSplashActivity.serviceManager.getBinder().doOrderLunch(mMonthLunch);
        }

        @Override
        public CharSequence getTitle(Context context, int position) {
            return context.getText(R.string.but_order) + " - " + MonthLunchDay
                    .DATE_SHOW_FORMAT.format(mMonthLunch.getDate());
        }

        @Override
        public CharSequence getText(Context context, int position) {
            return super.getText(context, position) + " - " + mMonthLunch.getName();
        }
    }

    public static class BurzaLunchOrderRequest
            extends LunchesManager.SimpleLunchOrderRequest {
        private final BurzaLunch mBurzaLunch;

        private BurzaLunchOrderRequest(BurzaLunch lunch) {
            mBurzaLunch = lunch;
        }

        @Override
        public boolean doOrder() throws Throwable {
            return ICSplashActivity.serviceManager != null &&
                    ICSplashActivity.serviceManager.isConnected() &&
                    ICSplashActivity.serviceManager.getBinder().doOrderLunch(mBurzaLunch);
        }

        @Override
        public CharSequence getTitle(Context context, int position) {
            return context.getText(R.string.but_order_from_burza) + " - " + BurzaLunch
                    .DATE_FORMAT.format(mBurzaLunch.getDate());
        }

        @Override
        public CharSequence getText(Context context, int position) {
            return super.getText(context, position) + " - " + mBurzaLunch.getName();
        }
    }

    public static class MonthToBurzaLunchOrderRequest
            extends LunchesManager.SimpleLunchOrderRequest {
        private final MonthLunch mMonthLunch;

        private MonthToBurzaLunchOrderRequest(MonthLunch lunch) {
            mMonthLunch = lunch;
        }

        @Override
        public boolean doOrder() throws Throwable {
            return ICSplashActivity.serviceManager != null &&
                    ICSplashActivity.serviceManager.isConnected() &&
                    ICSplashActivity.serviceManager.getBinder().doToBurzaMonthLunch(mMonthLunch);
        }

        @Override
        public CharSequence getTitle(Context context, int position) {
            return context.getText(R.string.text_to_burza) + "/" +
                    context.getText(R.string.text_from_burza) + " - " +
                    MonthLunchDay.DATE_SHOW_FORMAT.format(mMonthLunch.getDate());
        }

        @Override
        public CharSequence getText(Context context, int position) {
            return super.getText(context, position) + " - " + mMonthLunch.getName();
        }
    }

    public class ICBinder extends Binder {

        public boolean isWorkerRunning() {
            return worker.isWorkerRunning();
        }

        public void waitToWorkerStop() throws InterruptedException {
            worker.waitToWorkerStop();
        }

        public void setOnBurzaChangeListener(@Nullable Runnable onBurzaChange) {
            ICService.this.onBurzaChange = onBurzaChange;
        }

        public void setOnMonthChangeListener(@Nullable Runnable onMonthChange) {
            ICService.this.onMonthChange = onMonthChange;
        }

        public void removeDisabledLunch(MonthLunchDay lunchDay) {
            if (mLunchesManager == null) return;
            mLunchesManager.removeDisabledLunch(lunchDay);
            if (onMonthChange != null)
                onMonthChange.run();
            if (mLunchesManager != null)
                mLunchesManager.apply();
        }

        public void removeAllDisabledLunches() {
            if (mLunchesManager == null) return;
            mLunchesManager.removeAllDisabledLunches();
            if (onMonthChange != null)
                onMonthChange.run();
            if (mLunchesManager != null)
                mLunchesManager.apply();
        }

        public Thread refreshBurza() {
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    ICService.this.refreshBurza(false);
                }
            });
        }

        public Thread refreshMonth() {
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    ICService.this.refreshMonth(false);
                }
            });
        }

        public Thread processOrders() {
            if (mLunchesManager == null) {
                Toast.makeText(ICService.this,
                        R.string.toast_text_can_not_order_lunch,
                        Toast.LENGTH_LONG).show();// TODO: 12.11.2015 better toast texts
                return null;
            }
            return worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    if (mLunchesManager.getLunchOrderRequests()
                            .isEmpty()) return;
                    mLunchesManager.tryProcessOrders();
                    ICService.this.refreshMonth(false);
                    ICService.this.refreshBurza(false);
                }
            });
        }

        public List<LunchesManager.LunchOrderRequest> getOrderRequests() {
            if (mLunchesManager == null) return new ArrayList<>();
            return mLunchesManager.getLunchOrderRequests();
        }

        public void removeOrderRequest(MonthLunchOrderRequest request) {
            if (mLunchesManager != null)
                mLunchesManager.removeLunchOrderRequest(request);
        }

        public void removeOrderRequest(BurzaLunchOrderRequest request) {
            if (mLunchesManager != null)
                mLunchesManager.removeLunchOrderRequest(request);
        }

        public void removeOrderRequest(MonthToBurzaLunchOrderRequest request) {
            if (mLunchesManager != null)
                mLunchesManager.removeLunchOrderRequest(request);
        }

        public void orderLunch(final BurzaLunch lunch) {
            if (mLunchesManager == null) {
                Toast.makeText(ICService.this,
                        R.string.toast_text_can_not_order_lunch,
                        Toast.LENGTH_LONG).show();// TODO: 12.11.2015 better toast texts
                return;
            }
            mLunchesManager.addLunchOrderRequest(new BurzaLunchOrderRequest(lunch));
            processOrders();
        }

        public void orderLunch(final MonthLunch lunch) {
            if (mLunchesManager == null) {
                Toast.makeText(ICService.this,
                        R.string.toast_text_can_not_order_lunch,
                        Toast.LENGTH_LONG).show();// TODO: 12.11.2015 better toast texts
                return;
            }
            mLunchesManager.addLunchOrderRequest(new MonthLunchOrderRequest(lunch));
            processOrders();
        }

        public void toBurzaMonthLunch(final MonthLunch lunch) {
            if (mLunchesManager == null) {
                Toast.makeText(ICService.this,
                        R.string.toast_text_can_not_order_lunch,
                        Toast.LENGTH_LONG).show();// TODO: 12.11.2015 better toast texts
                return;
            }
            mLunchesManager.addLunchOrderRequest(new MonthToBurzaLunchOrderRequest(lunch));
            processOrders();
        }

        private boolean doOrderLunch(final BurzaLunch lunch) {
            return orderBurza(lunch);
        }

        private boolean doOrderLunch(final MonthLunch lunch) {
            return orderMonth(lunch);
        }

        private boolean doToBurzaMonthLunch(final MonthLunch lunch) {
            return toBurzaMonth(lunch);
        }

        public boolean isPendingOrders() {
            return mLunchesManager != null && mLunchesManager.isPendingOrders();
        }

        public void startBurzaChecker(@NonNull final BurzaLunchSelector selector) {
            startService(new Intent(ICService.this, ICBurzaCheckerService.class)
                            .putExtra(ICBurzaCheckerService.EXTRA_BURZA_CHECKER_STATE, ICBurzaCheckerService.BURZA_CHECKER_STATE_START)
                            .putExtra(ICBurzaCheckerService.EXTRA_BURZA_CHECKER_SELECTOR_AS_STRING, selector.toString())
            );
        }

        public void stopBurzaChecker() {
            startService(new Intent(ICService.this, ICBurzaCheckerService.class)
                    .putExtra(ICBurzaCheckerService.EXTRA_BURZA_CHECKER_STATE,
                            ICBurzaCheckerService.BURZA_CHECKER_STATE_STOP));
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

        public BurzaLunch[] getBurzaFast() {
            if (mLunchesManager == null) return null;
            return mLunchesManager.getBurzaLunches();
        }

        public MonthLunchDay[] getFullMonth(Thread refreshTread) throws InterruptedException {
            worker.waitToWorkerStop(refreshTread);
            if (mLunchesManager == null) return null;
            return mLunchesManager.getAllMonthLunches();
        }

        public MonthLunchDay[] getNewMonth(Thread refreshTread) throws InterruptedException {
            worker.waitToWorkerStop(refreshTread);
            if (mLunchesManager == null) return null;
            return mLunchesManager.getNewMonthLunches();
        }

        public MonthLunchDay[] getOldMonth(Thread refreshTread) throws InterruptedException {
            worker.waitToWorkerStop(refreshTread);
            if (mLunchesManager == null) return null;
            return mLunchesManager.getOldMonthLunches();
        }

        public MonthLunchDay[] getFullMonth() throws InterruptedException {
            worker.waitToWorkerStop();
            if (mLunchesManager == null) return null;
            return mLunchesManager.getAllMonthLunches();
        }

        public MonthLunchDay[] getNewMonth() throws InterruptedException {
            worker.waitToWorkerStop();
            if (mLunchesManager == null) return null;
            return mLunchesManager.getNewMonthLunches();
        }

        public MonthLunchDay[] getOldMonth() throws InterruptedException {
            worker.waitToWorkerStop();
            if (mLunchesManager == null) return null;
            return mLunchesManager.getOldMonthLunches();
        }

        public MonthLunchDay[] getFullMonthFast() {
            if (mLunchesManager == null) return null;
            return mLunchesManager.getAllMonthLunches();
        }

        public MonthLunchDay[] getNewMonthFast() {
            if (mLunchesManager == null) return null;
            return mLunchesManager.getNewMonthLunches();
        }

        public MonthLunchDay[] getOldMonthFast() {
            if (mLunchesManager == null) return null;
            return mLunchesManager.getOldMonthLunches();
        }
    }
}
