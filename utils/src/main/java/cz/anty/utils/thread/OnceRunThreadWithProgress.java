package cz.anty.utils.thread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.widget.Toast;

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
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
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

    @Override
    void start(final Thread thread, final String message) throws InterruptedException {
        if (message != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (progressDialog) {
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setProgress(0);
                        progressDialog.setMax(100);
                        /*progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                                activity.getString(R.string.but_cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        thread.interrupt();
                                    }
                                });
                        if (Build.VERSION.SDK_INT >= 21)
                            progressDialog.create();
                        else {
                            progressDialog.show();
                            progressDialog.hide();
                        }
                        Button but = progressDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        if (but != null)
                            but.setVisibility(View.GONE);*/
                        progressDialog.setCancelable(false);
                        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                thread.interrupt();
                            }
                        });
                    }
                }
            });
        }

        try {
            super.start(thread, message);
        } finally {
            if (message != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (progressDialog) {
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        }
                    }
                });
            }
        }

    }

    @Override
    public void startShowingProgress() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (progressDialog) {
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setCancelable(true);
                    /*Button but = progressDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    if (but != null)
                        but.setVisibility(View.VISIBLE);*/
                    if (progressDialog.isShowing())
                        progressDialog.show();
                }
            }
        });
    }

    @Override
    public void stopShowingProgress() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (progressDialog) {
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    /*Button but = progressDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    if (but != null)
                        but.setVisibility(View.GONE);*/
                    if (progressDialog.isShowing())
                        progressDialog.show();
                }
            }
        });
    }

    @Override
    public void setMaxProgress(final int max) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (progressDialog) {
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
                synchronized (progressDialog) {
                    progressDialog.setProgress(progress);
                }
            }
        });
    }
}