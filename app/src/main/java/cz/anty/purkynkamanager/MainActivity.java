package cz.anty.purkynkamanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;

import cz.anty.attendancemanager.SearchActivity;
import cz.anty.icanteenmanager.ICSplashActivity;
import cz.anty.purkynkamanager.firststart.FirstStartActivity;
import cz.anty.sasmanager.SASSplashActivity;
import cz.anty.timetablemanager.TimetableSelectActivity;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.listItem.MultilineAdapter;
import cz.anty.utils.listItem.TextMultilineItem;
import cz.anty.utils.settings.SettingsActivity;
import cz.anty.utils.thread.OnceRunThreadWithProgress;
import cz.anty.utils.thread.ProgressReporter;
import cz.anty.utils.thread.RunnableWithProgress;
import cz.anty.utils.update.UpdateConnector;
import cz.anty.wifiautologin.WifiLoginActivity;

public class MainActivity extends AppCompatActivity {

    private OnceRunThreadWithProgress worker;
    private MultilineAdapter<TextMultilineItem> adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        worker = new OnceRunThreadWithProgress(this);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new MultilineAdapter<>(this);
        listView.setAdapter(adapter);

        checkUpdate();
    }

    private void checkUpdate() {
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                try {
                    UpdateReceiver.checkUpdate(MainActivity.this);
                } catch (IOException | NumberFormatException e) {
                    Log.d(getClass().getSimpleName(), "checkUpdate", e);
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
                                            checkFirstStart();
                                        }
                                    })
                                    .show();
                            return;
                        }
                        checkFirstStart();
                    }
                });
            }
        }, getString(R.string.wait_text_loading));
    }

    private void downloadInstallUpdate() {
        worker.startWorker(new RunnableWithProgress() {
            @Override
            public String run(ProgressReporter reporter) {
                String filename = "latest.apk";

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
            }
        }, getString(R.string.wait_text_downloading_update) + "…");
    }

    private void checkFirstStart() {
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                if (getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                        .getInt(Constants.SETTING_NAME_FIRST_START, -1)
                        != BuildConfig.VERSION_CODE || isNewTerms()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(MainActivity.this, FirstStartActivity.class));
                            finish();
                        }
                    });
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkShare();
                    }
                });
            }
        }, getString(R.string.wait_text_loading));
    }

    private boolean isNewTerms() {
        try {
            return getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                    .getInt(Constants.SETTING_NAME_LATEST_TERMS_CODE, -1)
                    != UpdateConnector.getLatestTermsVersionCode();
        } catch (IOException | NumberFormatException e) {
            Log.d(getClass().getSimpleName(), "showThisPage", e);
            return false;
        }
    }

    private void checkShare() {
        final SharedPreferences preferences = getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE);
        if (preferences.getBoolean(Constants.SETTING_NAME_SHOW_SHARE, true)) {
            new AlertDialog.Builder(MainActivity.this, R.style.AppTheme_Dialog)
                    .setTitle(R.string.dialog_title_share)
                    .setMessage(R.string.dialog_message_share)
                    .setPositiveButton(R.string.but_share, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND)
                                            .setType("text/plain")
                                            .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                                            .putExtra(Intent.EXTRA_TEXT, getString(R.string.text_extra_text_share)),// TODO: 2.9.15 better share text
                                    null));

                            preferences.edit().putBoolean(Constants.SETTING_NAME_SHOW_SHARE, false).apply();
                            init();
                        }
                    })
                    .setIcon(R.mipmap.ic_launcher)
                    .setCancelable(false)
                    .setNeutralButton(R.string.but_skip, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            init();
                        }
                    })
                    .show();
            return;
        }
        init();
    }

    private void init() {
        boolean showDescription = getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_SHOW_DESCRIPTION, true);
        TextMultilineItem[] data = new TextMultilineItem[]{
                new TextMultilineItem(getString(R.string.app_name_sas), showDescription ? getString(R.string.app_description_sas) : null),
                new TextMultilineItem(getString(R.string.app_name_wifi), showDescription ? getString(R.string.app_description_wifi) : null),
                new TextMultilineItem(getString(R.string.app_name_icanteen), showDescription ? getString(R.string.app_description_icanteen) : null),
                new TextMultilineItem(getString(R.string.app_name_timetable), showDescription ? getString(R.string.app_description_timetable) : null),
                new TextMultilineItem(getString(R.string.app_name_attendance), showDescription ? getString(R.string.app_description_attendance) : null)};

        adapter.setNotifyOnChange(false);
        adapter.clear();
        for (TextMultilineItem item : data) {
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
                        startActivity(new Intent(MainActivity.this, ICSplashActivity.class));
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
