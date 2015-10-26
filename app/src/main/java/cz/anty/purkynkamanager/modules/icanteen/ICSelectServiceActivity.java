package cz.anty.purkynkamanager.modules.icanteen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.icanteen.widget.ICTodayLunchWidget;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.list.listView.TextMultilineItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.RecyclerInflater;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.RecyclerItemClickListener;
import cz.anty.purkynkamanager.utils.settings.ICSettingsActivity;

public class ICSelectServiceActivity extends AppCompatActivity {

    private static final String LOG_TAG = "ICSelectServiceActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean showDescription = getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_SHOW_DESCRIPTION, true);

        RecyclerInflater.inflateToActivity(this).useSwipeRefresh(false).inflate()
                .setAdapter(new TextMultilineItem(getText(R.string.app_name_icanteen_lunch_order), showDescription
                                ? getText(R.string.app_description_icanteen_lunch_order) : null),
                        new TextMultilineItem(getText(R.string.app_name_icanteen_burza_watcher), showDescription
                                ? getText(R.string.app_description_icanteen_burza_watcher) : null),
                        new TextMultilineItem(getText(R.string.app_name_icanteen_burza), showDescription
                                ? getText(R.string.app_description_icanteen_burza) : null))
                .setItemTouchListener(new RecyclerItemClickListener.SimpleClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        switch (position) {
                            case 0:
                                startActivity(new Intent(ICSelectServiceActivity.this, ICLunchOrderActivity.class));
                                break;
                            case 1:
                                startActivity(new Intent(ICSelectServiceActivity.this, ICBurzaCheckerActivity.class));
                                break;
                            case 2:
                                startActivity(new Intent(ICSelectServiceActivity.this, ICBurzaActivity.class));
                                break;
                        }
                    }
                });

        if (AppDataManager.isFirstStart(AppDataManager.Type.I_CANTEEN))
            Utils.generateFirstStartDialog(ICSelectServiceActivity.this, new Intent(),
                    ICTodayLunchWidget.class, R.style.AppTheme_Dialog_IC,
                    getText(R.string.dialog_title_icanteen_widget_alert),
                    getText(R.string.dialog_message_icanteen_widget_alert),
                    R.mipmap.ic_launcher_ic, new Runnable() {
                        @Override
                        public void run() {
                            AppDataManager.setFirstStart(AppDataManager.Type.I_CANTEEN, false);
                        }
                    });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select, menu);
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
            startActivity(new Intent(this, ICSettingsActivity.class));
            return true;
        }
        if (id == R.id.action_log_out) {
            logOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void logOut() {
        AppDataManager.logout(AppDataManager.Type.I_CANTEEN);
        startActivity(new Intent(this, ICSplashActivity.class));
        finish();
    }
}
