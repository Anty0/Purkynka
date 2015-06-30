package cz.anty.utils.thread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.widget.Toast;

import cz.anty.utils.R;

/**
 * Created by anty on 29.6.15.
 *
 * @author anty
 */
public class OnceRunThreadWithProgress extends OnceRunThreadWithSpinner implements ProgressReporter {

    private final Activity activity;
    private final ProgressDialog progressDialog;

    public OnceRunThreadWithProgress(Activity activity) {
        super(activity);
        this.activity = activity;

        progressDialog = getProgressDialog();
        //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    public Thread startWorker(final RunnableWithProgress runnable, @Nullable String message) {
        return startWorker(new Thread(new Runnable() {
            @Override
            public void run() {
                final String result = runnable.run(OnceRunThreadWithProgress.this);
                if (result != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }, message + " Thread"), message);
    }

    protected void start(final Thread thread, final String message) throws InterruptedException {
        if (message != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (this) {
                        progressDialog.setProgress(0);
                        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                                activity.getString(R.string.but_cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        thread.interrupt();
                                    }
                                });
                    }
                }
            });
        }
        super.start(thread, message);
        if (message != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (this) {
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    }
                }
            });
        }
    }

    @Override
    public void startShowingProgress() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                }
            }
        });
    }

    @Override
    public void stopShowingProgress() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                }
            }
        });
    }

    @Override
    public void setMaxProgress(final int max) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    progressDialog.setMax(max);
                }
            }
        });
    }

    @Override
    public void reportProgress(final int progress) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    progressDialog.setProgress(progress);
                }
            }
        });
    }
}