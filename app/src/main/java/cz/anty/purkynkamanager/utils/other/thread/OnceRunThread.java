package cz.anty.purkynkamanager.utils.other.thread;

import android.content.BroadcastReceiver.PendingResult;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import cz.anty.purkynkamanager.utils.other.Log;

/**
 * Created by anty on 9.6.15.
 *
 * @author anty
 */
public class OnceRunThread {

    private static final String LOG_TAG = "OnceRunThread";

    private final Object waitingThreadsLock = new Object();
    private final Object workerLock = new Object();
    private final Object waitingLock = new Object();
    private final Object powerManagerLock = new Object();
    private PowerManager powerManager = null;
    private int waitingThreads = 0;
    private Thread worker = null;

    public OnceRunThread() {

    }

    public OnceRunThread(@Nullable PowerManager powerManager) {
        setPowerManager(powerManager);
    }

    public OnceRunThread(@NonNull Context context) {
        setPowerManager(context);
    }

    public void setPowerManager(@Nullable PowerManager powerManager) {
        synchronized (powerManagerLock) {
            this.powerManager = powerManager;
        }
    }

    public void setPowerManager(@NonNull Context context) {
        setPowerManager((PowerManager) context.getSystemService(Context.POWER_SERVICE));
    }

    private PowerManager.WakeLock getWakeLock() {
        synchronized (powerManagerLock) {
            return powerManager != null ? powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK, getClass().getName()) : null;
        }
    }

    public Thread startWorker(Runnable runnable) {
        return startWorker(new Thread(runnable), null);
    }

    public Thread startWorker(Runnable runnable, @Nullable PendingResult broadcastResult) {
        return startWorker(new Thread(runnable), broadcastResult);
    }

    public Thread startWorker(@NonNull final Thread thread, @Nullable final PendingResult broadcastResult) {
        Thread.State state = thread.getState();
        if (state != Thread.State.NEW && state != Thread.State.RUNNABLE)
            throw new IllegalStateException("Thread must by NEW or RUNNABLE");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    start(thread, broadcastResult);
                } catch (InterruptedException e) {
                    Log.d(LOG_TAG, "startWorker", e);
                }
            }
        }).start();
        return thread;
    }

    void start(Thread thread, @Nullable PendingResult broadcastResult) throws InterruptedException {
        //waitToWorkerStop();
        synchronized (waitingThreadsLock) {
            waitingThreads++;
        }
        setWorker(thread);
        synchronized (waitingThreadsLock) {
            waitingThreads--;
        }
        if (!worker.isInterrupted()) {
            PowerManager.WakeLock wakeLock = getWakeLock();
            if (wakeLock != null)
                wakeLock.acquire();

            worker.start();
            worker.join();

            if (wakeLock != null)
                wakeLock.release();
        }
        if (broadcastResult != null
                && getWaitingThreadsLength() == 0
                && Build.VERSION.SDK_INT >= 11)
            broadcastResult.finish();
    }

    public int getWaitingThreadsLength() {
        synchronized (waitingThreadsLock) {
            return waitingThreads;
        }
    }

    private void setWorker(Thread worker) throws InterruptedException {
        synchronized (waitingLock) {
            waitToWorkerStop();
            synchronized (workerLock) {
                this.worker = worker;
            }
        }
    }

    public Object getWorkerLock() {
        return waitingLock;
    }

    public boolean stopActualWorker() {
        synchronized (workerLock) {
            if (isWorkerRunning()) {
                worker.interrupt();
                return true;
            }
        }
        return false;
    }

    public boolean isWorkerRunning() {
        synchronized (workerLock) {
            return worker != null && worker.getState() != Thread.State.TERMINATED;
        }
    }

    public void waitToWorkerStop() throws InterruptedException {
        synchronized (waitingLock) {
            while (isWorkerRunning()) {
                Thread.sleep(10);
            }
        }
    }

    public void waitToWorkerStop(@NonNull Thread thread) throws InterruptedException {
        while (thread.getState() != Thread.State.TERMINATED)
            waitToWorkerStop();
    }
}
