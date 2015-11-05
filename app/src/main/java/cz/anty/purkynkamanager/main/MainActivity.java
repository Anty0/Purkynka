package cz.anty.purkynkamanager.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.attendance.SearchActivity;
import cz.anty.purkynkamanager.modules.icanteen.ICSplashActivity;
import cz.anty.purkynkamanager.modules.sas.SASSplashActivity;
import cz.anty.purkynkamanager.modules.timetable.TimetableSelectActivity;
import cz.anty.purkynkamanager.modules.wifi.WifiLoginActivity;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;
import cz.anty.purkynkamanager.utils.other.list.items.TextMultilineImageItem;
import cz.anty.purkynkamanager.utils.other.list.items.TextMultilineItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.RecyclerItemClickListener;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.base.RecyclerInflater;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.SpecialModuleManager;
import cz.anty.purkynkamanager.utils.other.list.toolbar.FragmentDrawer;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThread;
import cz.anty.purkynkamanager.utils.other.update.UpdateConnector;
import cz.anty.purkynkamanager.utils.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";

    private static SpecialModuleManager moduleManager;
    private OnceRunThread worker;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        worker = new OnceRunThread(this);

        if (moduleManager == null)
            moduleManager = SpecialModuleManager.getInstance(this, true);
        moduleManager.bindRecyclerManager(RecyclerInflater
                .inflateToActivity(this).setLayoutResourceId
                        (R.layout.activity_main).inflate()
                .setRefreshing(true));

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (savedInstanceState == null) {
            final SharedPreferences preferences = getSharedPreferences
                    (Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE);
            final int actualCode = preferences.getInt(Constants.SETTING_NAME_LATEST_TERMS_CODE, -1);
            worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    Integer latestCode;
                    if (actualCode == -1)
                        latestCode = null;
                    else try {
                        latestCode = UpdateConnector.getLatestTermsVersionCode();
                    } catch (IOException | NumberFormatException e) {
                        Log.d(LOG_TAG, "onCreate", e);
                        latestCode = null;
                    }
                    if (actualCode == -1 || (latestCode != null && actualCode != latestCode)) {
                        CharSequence terms;
                        try {
                            terms = UpdateConnector.getLatestTerms
                                    (getString(R.string.language));
                        } catch (IOException e) {
                            Log.d(LOG_TAG, "onCreate", e);
                            terms = getText(R.string.text_terms);
                        }
                        final CharSequence finalTerms = terms;
                        final Integer finalLatestCode = latestCode;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(MainActivity.this, R.style.AppTheme_Dialog)
                                        .setTitle(R.string.activity_title_first_start_terms)
                                        .setMessage(finalTerms)
                                        .setPositiveButton(R.string.but_accept,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        worker.startWorker(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Integer latestCode2;
                                                                if (finalLatestCode != null)
                                                                    latestCode2 = finalLatestCode;
                                                                else {
                                                                    try {
                                                                        latestCode2 = UpdateConnector.getLatestTermsVersionCode();
                                                                    } catch (IOException | NumberFormatException e) {
                                                                        Log.d(LOG_TAG, "onCreate", e);
                                                                        latestCode2 = null;
                                                                    }
                                                                }
                                                                if (latestCode2 == null) {
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            new AlertDialog.Builder(MainActivity.this, R.style.AppTheme_Dialog)
                                                                                    .setTitle(R.string.activity_title_first_start_terms)
                                                                                    .setMessage(R.string.dialog_message_can_not_accept_terms)
                                                                                    .setPositiveButton(R.string.but_exit,
                                                                                            new DialogInterface.OnClickListener() {
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
                                                                preferences.edit()
                                                                        .putInt(Constants.SETTING_NAME_LATEST_TERMS_CODE,
                                                                                latestCode2).apply();
                                                            }
                                                        });
                                                    }
                                                })
                                        .setNegativeButton(R.string.but_exit,
                                                new DialogInterface.OnClickListener() {
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
                    }
                }
            });
        }

        init();
    }

    /*private void checkFirstStart() {
        Log.d(LOG_TAG, "checkFirstStart");
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
                        init();
                    }
                });
            }
        });
    }

    private boolean isNewTerms() {
        Log.d(LOG_TAG, "isNewTerms");
    }*/

    private void init() {
        Log.d(LOG_TAG, "init");

        final FragmentDrawer drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout)
                        findViewById(R.id.drawer_layout), mToolbar,
                new TextMultilineImageItem(getText(R.string.app_name_sas), null, R.mipmap.ic_launcher_sas, false),
                new TextMultilineImageItem(getText(R.string.app_name_wifi), null, R.mipmap.ic_launcher_wifi, false),
                new TextMultilineImageItem(getText(R.string.app_name_icanteen), null, R.mipmap.ic_launcher_ic, false),
                new TextMultilineImageItem(getText(R.string.app_name_timetable), null, R.mipmap.ic_launcher_t, false),
                new TextMultilineImageItem(getText(R.string.app_name_attendance), null, R.mipmap.ic_launcher_a, false),
                new TextMultilineItem("", null, false),
                new TextMultilineImageItem(getText(R.string.activity_title_settings), null, R.drawable.ic_action_settings, false));
        drawerFragment.setDrawerListener(new RecyclerItemClickListener.ClickListener() {
            private final int[] descriptionIds = new int[]{
                    R.string.app_description_sas,
                    R.string.app_description_wifi,
                    R.string.app_description_icanteen,
                    R.string.app_description_timetable,
                    R.string.app_description_attendance,
                    -1, -1
            };

            @Override
            public void onClick(View view, int position) {
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
                    case 6:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;
                }
            }

            @Override
            public void onLongClick(final View view, final int position) {
                final MultilineRecyclerAdapter<MultilineItem> adapter = drawerFragment.getAdapter();
                final MultilineItem item = adapter.getItem(position);
                boolean showDescription = item instanceof TextMultilineItem
                        && ((TextMultilineItem) item).getText() == null
                        && descriptionIds[position] != -1;

                clearTexts(adapter);
                adapter.notifyItemRangeChanged(0, adapter.getItemCount());

                if (showDescription) {
                    ((TextMultilineItem) item).setText(getText(descriptionIds[position]));
                    adapter.notifyItemChanged(position);
                }
            }

            private void clearTexts(MultilineRecyclerAdapter<MultilineItem> adapter) {
                ArrayList<MultilineItem> items = adapter.getItems();
                for (int i = 0, itemsLength = items.size(); i < itemsLength; i++) {
                    MultilineItem item = items.get(i);
                    if (item instanceof TextMultilineItem
                            && ((TextMultilineItem) item).getText() != null) {
                        ((TextMultilineItem) item).setText(null);
                        adapter.notifyItemChanged(i);
                    }
                }
            }
        });

        if (!moduleManager.isInitialized())
            moduleManager.init();
    }

    @Override
    protected void onResume() {
        if (moduleManager.isInitialized())
            moduleManager.update();
        super.onResume();
    }

    @Override
    protected void onPause() {
        moduleManager.saveState();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        do {
            try {
                worker.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d(LOG_TAG, "onDestroy", e);
            }
        } while (worker.getWaitingThreadsLength() > 0);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_removed_items) {
            startActivity(new Intent(MainActivity.this, RemovedItemsActivity.class));
            return true;
        }
        if (id == R.id.action_share) {
            startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                            .putExtra(Intent.EXTRA_TEXT, getText(R.string.text_extra_text_share)),
                    null));

            getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE).edit()
                    .putBoolean(Constants.SETTING_NAME_SHOW_SHARE, false).apply();

            if (moduleManager.isInitialized())
                moduleManager.update();
            return true;
        }
        if (id == R.id.action_send_feedback) {
            startActivity(new Intent(MainActivity.this, SendFeedbackActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
