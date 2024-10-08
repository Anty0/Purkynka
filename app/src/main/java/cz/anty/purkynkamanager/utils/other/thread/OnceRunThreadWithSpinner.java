package cz.anty.purkynkamanager.utils.other.thread;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import cz.anty.purkynkamanager.utils.other.AppCompatProgressDialog;
import cz.anty.purkynkamanager.utils.other.Log;

/**
 * Created by anty on 10.6.15.
 *
 * @author anty
 */
public class OnceRunThreadWithSpinner extends OnceRunThread {

    private static final String LOG_TAG = "OnceRunThreadWithSpinner";

    private final Activity activity;
    private final AppCompatProgressDialog progressDialog;
    private final ArrayList<CharSequence> messages = new ArrayList<>();
    private final Object depthLock = new Object();
    private int depth = 0;

    public OnceRunThreadWithSpinner(Activity activity) {
        super(activity);
        this.activity = activity;

        /*if (Build.VERSION.SDK_INT > 13)
            progressDialog = new AppCompatProgressDialog(activity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        else progressDialog = new AppCompatProgressDialog(activity);*/
        progressDialog = new AppCompatProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(AppCompatProgressDialog.STYLE_SPINNER);
    }

    AppCompatProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public Thread startWorker(Runnable runnable, @Nullable CharSequence message) {
        return startWorker(new Thread(runnable, message + " Thread"), message);
    }

    public Thread startWorker(@NonNull final Thread thread, @Nullable final CharSequence message) {
        if (thread.getState() != Thread.State.NEW)
            throw new IllegalStateException("Thread must by NEW and RUNNABLE");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    start(thread, message);
                } catch (InterruptedException e) {
                    Log.d(LOG_TAG, "startWorker", e);
                }
            }
        }, message + " Thread").start();
        return thread;
    }

    void start(final Thread thread, final CharSequence message) throws InterruptedException {
        if (message != null) {
            synchronized (messages) {
                messages.add(message);
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (progressDialog) {
                        progressDialog.setMessage(getMessage());
                        synchronized (depthLock) {
                            if (depth == 0 && (Build.VERSION.SDK_INT < 17
                                    || !activity.isDestroyed()))
                                progressDialog.show();

                            depth++;
                        }
                    }
                }
            });
        }

        try {
            super.start(thread, null);
        } finally {
            if (message != null) {
                synchronized (messages) {
                    messages.remove(message);
                }

                int localDepth;
                synchronized (depthLock) {
                    depth--;
                    localDepth = depth;
                }

                if (localDepth == 0)
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (progressDialog) {
                                progressDialog.hide();
                            }
                        }
                    });
            }
        }
    }

    private CharSequence getMessage() {
        synchronized (messages) {
            if (messages.size() <= 0) return "";
            if (messages.size() == 1) return messages.get(0);
            StringBuilder builder = new StringBuilder(messages.get(0));
            for (int i = 1; i < messages.size(); i++) {
                builder.append("\n").append(messages.get(i));
            }
            return builder;
        }
    }
}
