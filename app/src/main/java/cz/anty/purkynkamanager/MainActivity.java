package cz.anty.purkynkamanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;

import cz.anty.attendancemanager.SearchActivity;
import cz.anty.icanteenmanager.ICSplashActivity;
import cz.anty.purkynkamanager.firststart.FirstStartActivity;
import cz.anty.sasmanager.SASSplashActivity;
import cz.anty.timetablemanager.TimetableSelectActivity;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.list.listView.TextMultilineItem;
import cz.anty.utils.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerItemClickListener;
import cz.anty.utils.settings.SettingsActivity;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;
import cz.anty.utils.update.UpdateConnector;
import cz.anty.wifiautologin.WifiLoginActivity;

public class MainActivity extends AppCompatActivity {

    public static final String SKIP_UPDATE_CHECK_KEY = "SKIP_UPDATE";

    private OnceRunThreadWithSpinner worker;
    private MultilineRecyclerAdapter<TextMultilineItem> adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getClass().getSimpleName(), "onCreate");

        worker = new OnceRunThreadWithSpinner(this);
        adapter = new MultilineRecyclerAdapter<>();
        recyclerView = RecyclerAdapter.inflateToActivity(this, null, adapter, null);

        checkUpdate();
    }

    private void checkUpdate() {
        Log.d(getClass().getSimpleName(), "checkUpdate");
        if (getIntent().getBooleanExtra(SKIP_UPDATE_CHECK_KEY, false)) {
            checkFirstStart();
            return;
        }

        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                try {
                    UpdateReceiver.checkUpdate(MainActivity.this);
                } catch (IOException | NumberFormatException e) {
                    Log.d(MainActivity.this.getClass().getSimpleName(), "checkUpdate", e);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (UpdateReceiver.isUpdateAvailable(MainActivity.this)) {
                            startActivity(new Intent(MainActivity.this, UpdateActivity.class));
                            finish();
                            return;
                        }
                        checkFirstStart();
                    }
                });
            }
        }, getString(R.string.wait_text_loading));
    }

    private void checkFirstStart() {
        Log.d(getClass().getSimpleName(), "checkFirstStart");
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
        Log.d(getClass().getSimpleName(), "isNewTerms");
        try {
            return getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                    .getInt(Constants.SETTING_NAME_LATEST_TERMS_CODE, -1)
                    != UpdateConnector.getLatestTermsVersionCode();
        } catch (IOException | NumberFormatException e) {
            Log.d(getClass().getSimpleName(), "isNewTerms", e);
            return false;
        }
    }

    private void checkShare() {
        Log.d(getClass().getSimpleName(), "checkShare");
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
        Log.d(getClass().getSimpleName(), "init");
        boolean showDescription = getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_SHOW_DESCRIPTION, true);

        adapter.clearItems();
        adapter.addAllItems(new TextMultilineItem(getString(R.string.app_name_sas), showDescription ? getString(R.string.app_description_sas) : null),
                new TextMultilineItem(getString(R.string.app_name_wifi), showDescription ? getString(R.string.app_description_wifi) : null),
                new TextMultilineItem(getString(R.string.app_name_icanteen), showDescription ? getString(R.string.app_description_icanteen) : null),
                new TextMultilineItem(getString(R.string.app_name_timetable), showDescription ? getString(R.string.app_description_timetable) : null),
                new TextMultilineItem(getString(R.string.app_name_attendance), showDescription ? getString(R.string.app_description_attendance) : null));

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
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
                }));

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
