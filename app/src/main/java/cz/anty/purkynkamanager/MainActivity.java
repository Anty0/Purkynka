package cz.anty.purkynkamanager;

import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;
import java.util.Calendar;

import cz.anty.attendancemanager.SearchActivity;
import cz.anty.sasmanager.SASSplashActivity;
import cz.anty.timetablemanager.TimetableSelectActivity;
import cz.anty.utils.listItem.MultilineAdapter;
import cz.anty.utils.listItem.MultilineItem;
import cz.anty.utils.listItem.TextMultilineItem;
import cz.anty.utils.settings.SettingsActivity;
import cz.anty.utils.thread.OnceRunThreadWithProgress;
import cz.anty.utils.thread.ProgressReporter;
import cz.anty.utils.thread.RunnableWithProgress;
import cz.anty.utils.update.UpdateConnector;
import cz.anty.wifiautologin.WifiLoginActivity;

public class MainActivity extends AppCompatActivity {

    private OnceRunThreadWithProgress worker;
    private MultilineAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        worker = new OnceRunThreadWithProgress(this);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new MultilineAdapter(this, R.layout.text_multi_line_list_item);
        listView.setAdapter(adapter);

        checkUpdate();
    }

    private void checkUpdate() {
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                try {
                    UpdateReceiver.checkUpdate(MainActivity.this);
                } catch (IOException ignored) {
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (UpdateReceiver.isUpdateAvailable(MainActivity.this)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    MainActivity.this, R.style.AppTheme_Dialog)
                                    .setTitle(R.string.notification_update_title)
                                    .setMessage(getString(R.string.notification_update_text_old) +
                                            " " + BuildConfig.VERSION_NAME
                                            + "\n" + getString(R.string.notification_update_text_new) + " " +
                                            UpdateReceiver.getLatestName(MainActivity.this))
                                    .setPositiveButton(R.string.but_update, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            downloadInstallUpdate();
                                        }
                                    }).setNegativeButton(R.string.but_exit, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    }).setIcon(R.mipmap.ic_launcher)
                                    .setCancelable(false);

                            long deferTime = UpdateReceiver.getDeferTime(MainActivity.this);
                            if (deferTime > 0) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(deferTime);
                                builder.setNeutralButton(getString(R.string.notify_but_defer_for) +
                                        " " + calendar.get(Calendar.HOUR_OF_DAY) +
                                        getString(R.string.notify_but_hours), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        checkTerms();
                                    }
                                });
                            }

                            builder.show();
                            return;
                        }
                        checkTerms();
                    }
                });
            }
        }, getString(R.string.loading));
    }

    private void downloadInstallUpdate() {
        worker.startWorker(new RunnableWithProgress() {
            @Override
            public String run(ProgressReporter reporter) {
                String filename = getString(R.string.latest) + " " + getString(R.string.app_name) + ".apk";
                Cursor cursor = null;
                try {
                    long id = UpdateConnector.downloadUpdate(MainActivity.this, filename);
                    DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(id);

                    cursor = manager.query(q);
                    cursor.moveToFirst();

                    while (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) ==
                            DownloadManager.STATUS_PENDING) {
                        if (Thread.interrupted()) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }

                    reporter.startShowingProgress();

                    while (true) {
                        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL ||
                                status == DownloadManager.STATUS_FAILED ||
                                Thread.interrupted()) break;

                        reporter.setMaxProgress(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)));
                        reporter.reportProgress(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)));

                        Log.d("STATUS", Integer.toString(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))));
                        Log.d("MAX", Integer.toString(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))));
                        Log.d("COMPLETED", Integer.toString(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))));

                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) ==
                            DownloadManager.STATUS_SUCCESSFUL) {
                        //manager.openDownloadedFile(id);
                        final Intent target = new Intent(Intent.ACTION_VIEW);
                        target.setDataAndType(Uri.parse(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                + "/" + filename), "application/vnd.android.package-archive");
                        target.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        Log.v("OPEN_FILE_PATH", getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + filename);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(target);//TODO delete target
                            }
                        });

                        return getString(R.string.download_successful);
                    }
                    return getString(R.string.download_failed);
                } finally {
                    if (cursor != null)
                        cursor.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }
            }
        }, getString(R.string.downloading_update) + "…");
    }

    private void checkTerms() {
        final SharedPreferences preferences = getSharedPreferences("MainData", MODE_PRIVATE);
        if (preferences.getBoolean("FIRST_START", true)) {
            new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                    .setTitle(R.string.title_terms)
                    .setMessage(getTerms())
                    .setPositiveButton(R.string.but_accept, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            preferences.edit().putBoolean("FIRST_START", false).apply();
                            initialize();
                        }
                    })
                    .setNegativeButton(R.string.but_exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(R.mipmap.ic_launcher)
                    .setCancelable(false)
                    .show();
            return;
        }
        initialize();
    }

    private String getTerms() {
        final StringBuilder builder = new StringBuilder();
        worker.waitToWorkerStop(worker.startWorker(new Runnable() {
            @Override
            public void run() {
                try {
                    builder.append(UpdateConnector.getLatestTerms(getString(R.string.language)));
                } catch (IOException e) {
                    builder.append("null");
                }
            }
        }, getString(R.string.loading)));

        String result = builder.toString();
        return result.equals("null") ? getString(R.string.text_terms) : result;
    }

    private void initialize() {
        MultilineItem[] data = new MultilineItem[]{
                new TextMultilineItem(getString(R.string.sas_app_name), getString(R.string.sas_app_description)),
                new TextMultilineItem(getString(R.string.wifi_app_name), getString(R.string.wifi_app_description)),
                new TextMultilineItem(getString(R.string.timetable_app_name), getString(R.string.timetable_app_description)),
                new TextMultilineItem(getString(R.string.attendance_app_name), getString(R.string.attendance_app_description))};

        adapter.clear();
        for (MultilineItem item : data) {
            adapter.add(item);
        }
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                //final String item = (String) parent.getItemAtPosition(position);
                switch (position) {
                    case 0:
                        startActivity(new Intent(MainActivity.this, SASSplashActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(MainActivity.this, WifiLoginActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(MainActivity.this, TimetableSelectActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(MainActivity.this, SearchActivity.class));
                        break;
                }
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_default, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
