package cz.anty.utils;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by anty on 9.6.15.
 *
 * @author anty
 */
public class OnceRunThread {

    private final Object waitLock = new Object();
    private int waitingThreads = 0;
    private Thread worker = null;

    public void startWorker(final Runnable runnable) {
        startWorker(new Thread(runnable));
    }

    public void startWorker(@NonNull final Thread thread) {
        if (thread.getState() != Thread.State.NEW)
            throw new IllegalStateException("Thread must by NEW and RUNNABLE");
        new Thread(new Runnable() {
            @Override
            public void run() {
                start(thread);
            }
        }).start();
    }

    protected void start(Thread thread) {
        //waitToWorkerStop();
        synchronized (waitLock) {
            waitingThreads++;
        }
        setWorker(thread);
        synchronized (waitLock) {
            waitingThreads--;
        }
        worker.start();
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
}
