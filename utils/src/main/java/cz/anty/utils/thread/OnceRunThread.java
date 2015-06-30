package cz.anty.utils.thread;

import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by anty on 9.6.15.
 *
 * @author anty
 */
public class OnceRunThread {

    private final Object waitingThreadsLock = new Object();
    private PowerManager powerManager;
    private int waitingThreads = 0;
    private Thread worker = null;

    public OnceRunThread(@Nullable PowerManager powerManager) {
        this.powerManager = powerManager;
    }

    public synchronized void setPowerManager(@Nullable PowerManager powerManager) {
        this.powerManager = powerManager;
    }

    private synchronized PowerManager.WakeLock getWakeLock() {
        return powerManager != null ? powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, getClass().getName()) : null;
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

    protected void start(Thread thread) throws InterruptedException {
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
        return waitingThreads;
    }

    private synchronized void setWorker(Thread worker) {
        waitToWorkerStop();
        this.worker = worker;
    }

    public synchronized boolean isWorkerRunning() {
        return !(worker == null || worker.getState() == Thread.State.TERMINATED);
    }

    public synchronized void waitToWorkerStop() {
        while (isWorkerRunning()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Log.d(null, null, e);
            }
        }
    }

    public synchronized void waitToWorkerStop(@NonNull Thread thread) {
        while (thread.getState() != Thread.State.TERMINATED)
            waitToWorkerStop();
    }
}
