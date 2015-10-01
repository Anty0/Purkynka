package cz.anty.purkynkamanager.update;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import cz.anty.purkynkamanager.BuildConfig;
import cz.anty.purkynkamanager.MainActivity;
import cz.anty.purkynkamanager.R;
import cz.anty.utils.ApplicationBase;
import cz.anty.utils.thread.ProgressReporter;
import cz.anty.utils.update.UpdateConnector;

/**
 * Created by anty on 24.9.15.
 *
 * @author anty
 */
public class UpdateActivity extends AppCompatActivity implements ProgressReporter {

    public static final String EXTRA_SKIP_DIALOG = "SKIP_DIALOG";

    private ProgressBar progressBar;
    private TextView percentTextView, numberTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!UpdateReceiver.isUpdateAvailable(this)) {
            finish();
            return;
        }
        setContentView(R.layout.alert_dialog_progress);

        progressBar = (ProgressBar) findViewById(R.id.progress);
        percentTextView = (TextView) findViewById(R.id.progress_percent);
        numberTextView = (TextView) findViewById(R.id.progress_number);

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
                        startActivity(new Intent(UpdateActivity.this, MainActivity.class));
                    }
                })
                .show();
    }

    private void updateViews() {
        percentTextView.setText(String.format("%1$d%", (progressBar.getProgress() / progressBar.getMax()) * 100));
        numberTextView.setText(String.format("%1$d/%2$d", progressBar.getProgress(), progressBar.getMax()));
    }

    private void downloadInstallUpdate() {
        ApplicationBase.WORKER.startWorker(new Runnable() {
            @Override
            public void run() {
                String filename = "latest.apk";

                Intent intent;
                try {
                    String path = UpdateConnector.downloadUpdate(UpdateActivity.this, UpdateActivity.this, filename);

                    intent = new Intent(Intent.ACTION_VIEW)
                            .setDataAndType(Uri.fromFile(new File(path)), //TODO delete file after install
                                    "application/vnd.android.package-archive")
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                } catch (IOException e) {
                    intent = null;
                }

                final Intent finalIntent = intent;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        String toastText;
                        if (finalIntent != null) {
                            startActivity(finalIntent);
                            toastText = getString(R.string.toast_text_download_successful);
                        } else {
                            toastText = getString(R.string.toast_text_download_failed);
                        }
                        Toast.makeText(UpdateActivity.this, toastText,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public void setMaxProgress(int max) {
        progressBar.setMax(max);
        updateViews();
    }

    @Override
    public void reportProgress(int progress) {
        progressBar.setProgress(progress);
        updateViews();
    }
}
