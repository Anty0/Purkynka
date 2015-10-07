package cz.anty.purkynkamanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import java.io.IOException;

import cz.anty.attendancemanager.SearchActivity;
import cz.anty.attendancemanager.TrackingSpecialModule;
import cz.anty.icanteenmanager.ICSpecialModule;
import cz.anty.icanteenmanager.ICSplashActivity;
import cz.anty.purkynkamanager.firststart.FirstStartActivity;
import cz.anty.purkynkamanager.update.UpdateSpecialModule;
import cz.anty.sasmanager.SASSpecialModule;
import cz.anty.sasmanager.SASSplashActivity;
import cz.anty.timetablemanager.TimetableSelectActivity;
import cz.anty.timetablemanager.TimetableSpecialModule;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.list.listView.MultilineItem;
import cz.anty.utils.list.listView.TextMultilineItem;
import cz.anty.utils.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerItemClickListener;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialModuleManager;
import cz.anty.utils.list.toolbar.FragmentDrawer;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;
import cz.anty.utils.update.UpdateConnector;
import cz.anty.wifiautologin.WifiLoginActivity;
import cz.anty.wifiautologin.WifiSpecialModule;

public class MainActivity extends AppCompatActivity {

    private static SpecialModuleManager moduleManager;
    private boolean showOptionsMenu = false;
    private OnceRunThreadWithSpinner worker;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(getClass().getSimpleName(), "onCreate");
        showOptionsMenu = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        if (moduleManager == null)
            moduleManager = new SpecialModuleManager(recyclerView,
                    new UpdateSpecialModule(this), new ShareSpecialModule(this), new TrackingSpecialModule(this),
                    new SASSpecialModule(this), new ICSpecialModule(this), new TimetableSpecialModule(this),
                    new WifiSpecialModule(this));
        else moduleManager.reInit(recyclerView);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        worker = new OnceRunThreadWithSpinner(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (savedInstanceState == null) checkFirstStart();
        else init();
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
                        init();
                    }
                });
            }
        }, getText(R.string.wait_text_loading));
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

    private void init() {
        Log.d(getClass().getSimpleName(), "init");

        final FragmentDrawer drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout)
                        findViewById(R.id.drawer_layout), mToolbar,
                new TextMultilineItem(getText(R.string.app_name_sas), null),
                new TextMultilineItem(getText(R.string.app_name_wifi), null),
                new TextMultilineItem(getText(R.string.app_name_icanteen), null),
                new TextMultilineItem(getText(R.string.app_name_timetable), null),
                new TextMultilineItem(getText(R.string.app_name_attendance), null));
        drawerFragment.setDrawerListener(new RecyclerItemClickListener.ClickListener() {
            private final int[] descriptionIds = new int[]{
                    R.string.app_description_sas,
                    R.string.app_description_wifi,
                    R.string.app_description_icanteen,
                    R.string.app_description_timetable,
                    R.string.app_description_attendance
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
                }
            }

            @Override
            public void onLongClick(final View view, final int position) {
                final MultilineRecyclerAdapter<MultilineItem> adapter = drawerFragment.getAdapter();
                final MultilineItem item = adapter.getItem(position);
                boolean showDescription = item instanceof TextMultilineItem
                        && ((TextMultilineItem) item).getText() == null;
                clearTexts(adapter.getItems(new MultilineItem[adapter.getItemCount()]));
                adapter.notifyDataSetChanged();

                if (showDescription) {
                    ScaleAnimation animation = new ScaleAnimation(1, 1, 1, 0);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            view.clearAnimation();
                            ((TextMultilineItem) item).setText(getText(descriptionIds[position]));
                            adapter.notifyDataSetChanged();

                            ScaleAnimation animation2 = new ScaleAnimation(1, 1, 0, 1);
                            animation2.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    view.clearAnimation();
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            animation2.setDuration(100);
                            view.setAnimation(animation2);
                            animation2.startNow();
                            view.invalidate();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    animation.setDuration(100);
                    view.setAnimation(animation);
                    animation.startNow();
                    view.invalidate();
                }
            }

            private void clearTexts(MultilineItem[] items) {
                for (MultilineItem item : items) {
                    if (item instanceof TextMultilineItem) {
                        ((TextMultilineItem) item).setText(null);
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
    protected void onDestroy() {
        do {
            try {
                worker.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d(getClass().getSimpleName(), "onDestroy", e);
            }
        } while (worker.getWaitingThreadsLength() > 0);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (showOptionsMenu) {
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
