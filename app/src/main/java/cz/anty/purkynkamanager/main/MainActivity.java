package cz.anty.purkynkamanager.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

import cz.anty.purkynkamanager.BuildConfig;
import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.attendance.SearchActivity;
import cz.anty.purkynkamanager.firststart.FirstStartActivity;
import cz.anty.purkynkamanager.icanteen.ICSplashActivity;
import cz.anty.purkynkamanager.modules.ICSpecialModule;
import cz.anty.purkynkamanager.modules.SASSpecialModule;
import cz.anty.purkynkamanager.modules.ShareSpecialModule;
import cz.anty.purkynkamanager.modules.TimetableSpecialModule;
import cz.anty.purkynkamanager.modules.TrackingSpecialModule;
import cz.anty.purkynkamanager.modules.UpdateSpecialModule;
import cz.anty.purkynkamanager.modules.WifiSpecialModule;
import cz.anty.purkynkamanager.sas.SASSplashActivity;
import cz.anty.purkynkamanager.settings.SettingsActivity;
import cz.anty.purkynkamanager.timetable.TimetableSelectActivity;
import cz.anty.purkynkamanager.utils.Constants;
import cz.anty.purkynkamanager.utils.Log;
import cz.anty.purkynkamanager.utils.list.listView.MultilineItem;
import cz.anty.purkynkamanager.utils.list.listView.TextMultilineItem;
import cz.anty.purkynkamanager.utils.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.purkynkamanager.utils.list.recyclerView.RecyclerItemClickListener;
import cz.anty.purkynkamanager.utils.list.recyclerView.specialAdapter.SpecialModuleManager;
import cz.anty.purkynkamanager.utils.list.toolbar.FragmentDrawer;
import cz.anty.purkynkamanager.utils.thread.OnceRunThreadWithSpinner;
import cz.anty.purkynkamanager.utils.update.UpdateConnector;
import cz.anty.purkynkamanager.wifi.WifiLoginActivity;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";

    private static SpecialModuleManager moduleManager;
    private boolean showOptionsMenu = false;
    private OnceRunThreadWithSpinner worker;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        showOptionsMenu = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        View emptyView = findViewById(R.id.empty_view);
        if (moduleManager == null)
            moduleManager = new SpecialModuleManager(recyclerView, emptyView, true,
                    new UpdateSpecialModule(this), new ShareSpecialModule(this), new TrackingSpecialModule(this),
                    new SASSpecialModule(this), new ICSpecialModule(this), new TimetableSpecialModule(this),
                    new WifiSpecialModule(this));
        else moduleManager.reInit(recyclerView, emptyView);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        worker = new OnceRunThreadWithSpinner(this);

        setSupportActionBar(mToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (savedInstanceState == null) checkFirstStart();
        else init();
    }

    private void checkFirstStart() {
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
        }, getText(R.string.wait_text_loading));
    }

    private boolean isNewTerms() {
        Log.d(LOG_TAG, "isNewTerms");
        try {
            return getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                    .getInt(Constants.SETTING_NAME_LATEST_TERMS_CODE, -1)
                    != UpdateConnector.getLatestTermsVersionCode();
        } catch (IOException | NumberFormatException e) {
            Log.d(LOG_TAG, "isNewTerms", e);
            return false;
        }
    }

    private void init() {
        Log.d(LOG_TAG, "init");

        final FragmentDrawer drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout)
                        findViewById(R.id.drawer_layout), mToolbar,
                new TextMultilineItem(getText(R.string.app_name_sas), null, false),
                new TextMultilineItem(getText(R.string.app_name_wifi), null, false),
                new TextMultilineItem(getText(R.string.app_name_icanteen), null, false),
                new TextMultilineItem(getText(R.string.app_name_timetable), null, false),
                new TextMultilineItem(getText(R.string.app_name_attendance), null, false),
                new TextMultilineItem("", null, false),
                new TextMultilineItem(getText(R.string.activity_title_settings), null, false));
        drawerFragment.setDrawerListener(new RecyclerItemClickListener.ClickListener() {
            private final int[] descriptionIds = new int[]{
                    R.string.app_description_sas,
                    R.string.app_description_wifi,
                    R.string.app_description_icanteen,
                    R.string.app_description_timetable,
                    R.string.app_description_attendance, -1, -1
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

        showOptionsMenu = true;
        invalidateOptionsMenu();
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
        if (showOptionsMenu) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
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
        if (id == R.id.action_removed_items) {
            startActivity(new Intent(MainActivity.this, RemovedItemsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
