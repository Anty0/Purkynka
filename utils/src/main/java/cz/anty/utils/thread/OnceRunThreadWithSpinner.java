package cz.anty.utils.thread;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by anty on 10.6.15.
 *
 * @author anty
 */
public class OnceRunThreadWithSpinner extends OnceRunThread {

    private final Activity activity;
    private final ProgressDialog progressDialog;
    private final ArrayList<String> messages = new ArrayList<>();
    private int depth = 0;

    public OnceRunThreadWithSpinner(Activity activity) {
        super((PowerManager) activity.getSystemService(Context.POWER_SERVICE));
        this.activity = activity;

        if (Build.VERSION.SDK_INT > 13)
            progressDialog = new ProgressDialog(activity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        else progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    protected ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public Thread startWorker(Runnable runnable, @Nullable String message) {
        return startWorker(new Thread(runnable, message + " Thread"), message);
    }

    public Thread startWorker(@NonNull final Thread thread, @Nullable final String message) {
        if (thread.getState() != Thread.State.NEW)
            throw new IllegalStateException("Thread must by NEW and RUNNABLE");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    start(thread, message);
                } catch (InterruptedException ignored) {
                }
            }
        }, message + " Thread").start();
        return thread;
    }

    protected void start(final Thread thread, final String message) throws InterruptedException {
        if (message != null) {
            synchronized (messages) {
                messages.add(message);
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (this) {
                        progressDialog.setMessage(getMessage());
                        if (depth == 0)
                            progressDialog.show();

                        depth++;
                    }
                }
            });
        }

        try {
            super.start(thread);
        } finally {
            if (message != null) {
                synchronized (messages) {
                    messages.remove(message);
                }
                synchronized (this) {
                    depth--;

                    if (depth == 0)
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (this) {
                                    progressDialog.hide();
                                }
                            }
                        });
                }
            }
        }
    }

    private String getMessage() {
        synchronized (messages) {
            if (messages.size() <= 0) return "";
            StringBuilder builder = new StringBuilder(messages.get(0));
            for (int i = 1; i < messages.size(); i++) {
                builder.append("\n").append(messages.get(i));
            }
            return builder.toString();
        }
    }
}
