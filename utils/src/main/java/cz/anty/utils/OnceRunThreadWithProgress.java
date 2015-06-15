package cz.anty.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by anty on 10.6.15.
 *
 * @author anty
 */
public class OnceRunThreadWithProgress extends OnceRunThread {

    private final Activity activity;
    private final ProgressDialog progressDialog;
    private int depth = 0;

    public OnceRunThreadWithProgress(Activity activity) {
        this.activity = activity;

        if (Build.VERSION.SDK_INT > 13)
            progressDialog = new ProgressDialog(activity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        else progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    public void startWorker(final Runnable runnable, @Nullable String message) {
        startWorker(new Thread(runnable), message);
    }

    public void startWorker(@NonNull final Thread thread, @Nullable final String message) {
        if (thread.getState() != Thread.State.NEW)
            throw new IllegalStateException("Thread must by NEW and RUNNABLE");
        new Thread(new Runnable() {
            @Override
            public void run() {
                start(thread, message);
            }
        }, message + " Thread").start();
    }

    protected void start(Thread thread, final String message) {
        synchronized (this) {
            if (message != null) {
                if (depth == 0)
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.setMessage(message);
                            progressDialog.show();
                        }
                    });
                depth++;
            }
        }

        try {
            super.start(thread);
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            synchronized (this) {
                if (message != null) {
                    depth--;
                    if (depth == 0)
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.hide();
                            }
                        });
                }
            }
        }
    }
}
