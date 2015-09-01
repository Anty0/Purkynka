package cz.anty.purkynkamanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;

import cz.anty.attendancemanager.SearchActivity;
import cz.anty.icanteenmanager.ICanteenSplashActivity;
import cz.anty.purkynkamanager.firststart.FirstStartActivity;
import cz.anty.sasmanager.SASSplashActivity;
import cz.anty.timetablemanager.TimetableSelectActivity;
import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
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
        Log.d("START", "DEBUG-MODE: " + AppDataManager.isDebugMode(this));
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
                            new AlertDialog.Builder(MainActivity.this, R.style.AppTheme_Dialog)
                                    .setTitle(R.string.notify_title_update)
                                    .setMessage(getString(R.string.notify_text_update_old)
                                            .replace(Constants.STRINGS_CONST_VERSION, BuildConfig.VERSION_NAME)
                                            + "\n" + getString(R.string.notify_text_update_new)
                                            .replace(Constants.STRINGS_CONST_VERSION, UpdateReceiver.getLatestName(MainActivity.this))
                                            + "\n\n" + getString(R.string.dialog_message_update_alert))
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
                                    .setCancelable(false)
                                    .setNeutralButton(R.string.but_skip, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            checkTerms();
                                        }
                                    })
                                    .show();

                            /*long deferTime = UpdateReceiver.getDeferTime(MainActivity.this);
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
                            }*/

                            //builder.show();
                            return;
                        }
                        checkTerms();
                    }
                });
            }
        }, getString(R.string.wait_text_loading));
    }

    private void downloadInstallUpdate() {
        worker.startWorker(new RunnableWithProgress() {
            @Override
            public String run(ProgressReporter reporter) {
                String filename = getString(R.string.text_app_apk_name)
                        .replace(Constants.STRINGS_CONST_NAME, getString(R.string.app_name))
                        .replace(Constants.STRINGS_CONST_NUMBER,
                                Integer.toString(UpdateReceiver.getLatestCode(MainActivity.this)));

                Intent intent = null;
                String toReturn;
                try {
                    String path = UpdateConnector.downloadUpdate(MainActivity.this, reporter, filename);

                    intent = new Intent(Intent.ACTION_VIEW)
                            .setDataAndType(Uri.fromFile(new File(path)), //TODO delete file after install
                                    "application/vnd.android.package-archive")
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    toReturn = getString(R.string.toast_text_download_successful);
                } catch (IOException e) {
                    toReturn = getString(R.string.toast_text_download_failed);
                }

                final Intent finalIntent = intent;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        if (finalIntent != null)
                            startActivity(finalIntent);
                    }
                });
                return toReturn;

                /*DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                int status;

                boolean pending = true;
                while (true) {
                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(id);

                    Cursor cursor = manager.query(q);
                    cursor.moveToFirst();

                    status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

                    if (pending) {
                        if (status != DownloadManager.STATUS_PENDING) {
                            reporter.startShowingProgress();
                            pending = false;
                        }
                    } else {
                        reporter.setMaxProgress(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)));
                        reporter.reportProgress(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)));
                    }

                    if (AppDataManager.isDebugMode(MainActivity.this)) {
                        Log.d("STATUS", Integer.toString(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))));
                        Log.d("MAX", Integer.toString(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))));
                        Log.d("COMPLETED", Integer.toString(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))));
                    }

                    cursor.close();
                    if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED || Thread.interrupted())
                        break;

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        break;
                    }
                }*/

                    /*DownloadManager.Query q = new DownloadManager.Query();
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

                        if (AppDataManager.isDebugMode(MainActivity.this)) {
                            Log.d("STATUS", Integer.toString(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))));
                            Log.d("MAX", Integer.toString(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))));
                            Log.d("COMPLETED", Integer.toString(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))));
                        }

                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }*/
                /*String toReturn;
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    //manager.openDownloadedFile(id);
                    final Intent target = new Intent(Intent.ACTION_VIEW);
                    target.setData(Uri.parse(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + File.separator + filename));
                    /*target.setDataAndType(Uri.parse(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/" + filename), "application/vnd.android.package-archive");//
                    target.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    if (AppDataManager.isDebugMode(MainActivity.this))
                        Log.v("OPEN_FILE_PATH", getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + filename);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(target);
                        }
                    });

                    toReturn = getString(R.string.download_successful);
                } else
                    toReturn = getString(R.string.download_failed);*/

                //return toReturn;
            }
        }, getString(R.string.wait_text_downloading_update) + "…");
    }

    private void checkTerms() {
        if (getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                .getInt(Constants.SETTING_NAME_FIRST_START, -1) != BuildConfig.VERSION_CODE) {
            startActivity(new Intent(this, FirstStartActivity.class));
            finish();
            return;
        }
        init();
        /*worker.startWorker(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences preferences = getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE);
                if (preferences.getInt(Constants.SETTING_NAME_FIRST_START, -1) != BuildConfig.VERSION_CODE) {
                    final String terms = getTerms();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(MainActivity.this, R.style.AppTheme_Dialog)
                                    .setTitle(R.string.dialog_title_terms)
                                    .setMessage(terms)
                                    .setPositiveButton(R.string.but_accept, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            preferences.edit().putInt(Constants.SETTING_NAME_FIRST_START,
                                                    BuildConfig.VERSION_CODE).apply();
                                            init();
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
                        }
                    });
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        init();
                    }
                });
            }
        }, getString(R.string.wait_text_loading));*/
    }

    private void init() {
        MultilineItem[] data = new MultilineItem[]{
                new TextMultilineItem(getString(R.string.app_name_sas), getString(R.string.app_description_sas)),
                new TextMultilineItem(getString(R.string.app_name_wifi), getString(R.string.app_description_wifi)),
                new TextMultilineItem(getString(R.string.app_name_icanteen), getString(R.string.app_description_icanteen)),
                new TextMultilineItem(getString(R.string.app_name_timetable), getString(R.string.app_description_timetable)),
                new TextMultilineItem(getString(R.string.app_name_attendance), getString(R.string.app_description_attendance))};

        adapter.setNotifyOnChange(false);
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
                        startActivity(new Intent(MainActivity.this, ICanteenSplashActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(MainActivity.this, TimetableSelectActivity.class));
                        break;
                    case 4:
                        startActivity(new Intent(MainActivity.this, SearchActivity.class));
                        break;
                }
            }

        });

        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!adapter.isEmpty()) {
            getMenuInflater().inflate(R.menu.menu_default, menu);
            return true;
        }
        return false;
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
