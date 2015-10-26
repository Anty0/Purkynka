package cz.anty.purkynkamanager.utils.update;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import cz.anty.purkynkamanager.ApplicationBase;
import cz.anty.purkynkamanager.BuildConfig;
import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.main.MainActivity;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.thread.ProgressReporter;
import cz.anty.purkynkamanager.utils.other.update.UpdateConnector;

/**
 * Created by anty on 24.9.15.
 *
 * @author anty
 */
public class UpdateActivity extends AppCompatActivity {

    public static final String EXTRA_SKIP_DIALOG = "SKIP_DIALOG";

    private static Thread downloader;
    private static AppCompatActivity activity;
    private static ProgressBar progressBar;
    private static TextView percentTextView, numberTextView;
    private static int max = 100, progress = 0;
    private static ProgressReporter reporter = new ProgressReporter() {

        @Override
        public void setMaxProgress(final int max) {
            //Log.d(getClass().getSimpleName(), "setMaxProgress: " + max);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setMax(max);
                    UpdateActivity.max = max;
                    updateViews();
                }
            });
        }

        @Override
        public void reportProgress(final int progress) {
            //Log.d(getClass().getSimpleName(), "reportProgress: " + progress);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(progress);
                    UpdateActivity.progress = progress;
                    updateViews();
                }
            });
        }
    };

    private static void updateViews() {
        String percent = (int) (((float) progress / (float) max) * 100f) + "%";
        String number = progress + "/" + max;
        percentTextView.setText(percent);
        numberTextView.setText(number);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!UpdateReceiver.isUpdateAvailable(this)) {
            finish();
            return;
        }
        setContentView(R.layout.dialog_alert_progress);
        Utils.setPadding(findViewById(R.id
                .alert_dialog_body), 15, 2, 15, 2);

        activity = this;
        progressBar = (ProgressBar) findViewById(R.id.progress);
        percentTextView = (TextView) findViewById(R.id.progress_percent);
        numberTextView = (TextView) findViewById(R.id.progress_number);

        reporter.setMaxProgress(max);
        reporter.reportProgress(progress);
        if (downloader != null) return;

        if (getIntent().getBooleanExtra(EXTRA_SKIP_DIALOG, false)) {
            downloadInstallUpdate();
            return;
        }

        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(R.string.notify_title_update_available)
                .setMessage(String.format(getString(R.string.notify_text_update_old), BuildConfig.VERSION_NAME)
                        + "\n" + String.format(getString(R.string.notify_text_update_new),
                        UpdateReceiver.getLatestName(this))
                        + "\n\n" + getString(R.string.dialog_message_update_alert))
                .setPositiveButton(R.string.but_update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadInstallUpdate();
                    }
                }).setNegativeButton(R.string.but_exit,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(false)
                .setNeutralButton(R.string.but_skip, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        startActivity(new Intent(UpdateActivity.this, MainActivity.class));
                    }
                })
                .show();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (downloader != null) downloader.interrupt();

        return downloader == null
                && super.onKeyUp(keyCode, event);
    }

    private void downloadInstallUpdate() {
        downloader = ApplicationBase.WORKER.startWorker(new Runnable() {
            @Override
            public void run() {
                try {
                    String filename = "latest.apk";

                    Intent intent;
                    try {
                        String path = UpdateConnector.downloadUpdate(UpdateActivity.this, reporter, filename);

                        intent = new Intent(Intent.ACTION_VIEW)
                                .setDataAndType(Uri.fromFile(new File(path)), //TODO delete file after install
                                        "application/vnd.android.package-archive")
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    } catch (IOException | InterruptedException e) {
                        intent = null;
                    }

                    final Intent finalIntent = intent;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                            CharSequence toastText;
                            if (finalIntent != null) {
                                startActivity(finalIntent);
                                toastText = getText(R.string.toast_text_download_successful);
                            } else {
                                toastText = getText(R.string.toast_text_download_failed);
                            }
                            Toast.makeText(UpdateActivity.this, toastText,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    downloader = null;
                }
            }
        });
    }
}
