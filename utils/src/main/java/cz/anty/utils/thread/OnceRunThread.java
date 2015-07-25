package cz.anty.utils.thread;

import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import cz.anty.utils.AppDataManager;

/**
 * Created by anty on 9.6.15.
 *
 * @author anty
 */
public class OnceRunThread {

    private final Object waitingThreadsLock = new Object();
    private final Object waitingLock = new Object();
    private final Object powerManagerLock = new Object();
    private PowerManager powerManager;
    private int waitingThreads = 0;
    private Thread worker = null;

    public OnceRunThread(@Nullable PowerManager powerManager) {
        this.powerManager = powerManager;
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

    public Thread startWorker(final Runnable runnable) {
        return startWorker(new Thread(runnable));
    }

    public Thread startWorker(@NonNull final Thread thread) {
        Thread.State state = thread.getState();
        if (state != Thread.State.NEW && state != Thread.State.RUNNABLE)
            throw new IllegalStateException("Thread must by NEW or RUNNABLE");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    start(thread);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
        return thread;
    }

    void start(Thread thread) throws InterruptedException {
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
    }

    public int getWaitingThreadsLength() {
        synchronized (waitingThreadsLock) {
            return waitingThreads;
        }
    }

    private void setWorker(Thread worker) {
        synchronized (waitingLock) {
            waitToWorkerStop();
            this.worker = worker;
        }
    }

    public boolean isWorkerRunning() {
        synchronized (waitingLock) {
            return !(worker == null || worker.getState() == Thread.State.TERMINATED);
        }
    }

    public boolean waitToWorkerStop() {
        synchronized (waitingLock) {
            while (isWorkerRunning()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    if (AppDataManager.isDebugMode(null)) Log.d(null, null, e);
                    return false;
                }
            }
        }
        return true;
    }

    public void waitToWorkerStop(@NonNull Thread thread) {
        while (thread.getState() != Thread.State.TERMINATED)
            waitToWorkerStop();
    }
}
