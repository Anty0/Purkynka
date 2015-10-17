package cz.anty.purkynkamanager.icanteen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.settings.ICSettingsActivity;
import cz.anty.purkynkamanager.utils.AppDataManager;
import cz.anty.purkynkamanager.utils.Constants;
import cz.anty.purkynkamanager.utils.list.listView.TextMultilineItem;
import cz.anty.purkynkamanager.utils.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.purkynkamanager.utils.list.recyclerView.RecyclerInflater;
import cz.anty.purkynkamanager.utils.list.recyclerView.RecyclerItemClickListener;

public class ICSelectServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean showDescription = getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_SHOW_DESCRIPTION, true);
        MultilineRecyclerAdapter<TextMultilineItem> adapter = new MultilineRecyclerAdapter<>(this);
        adapter.clearItems();
        adapter.addAllItems(
                new TextMultilineItem(getText(R.string.app_name_icanteen_lunch_order), showDescription
                        ? getText(R.string.app_description_icanteen_lunch_order) : null),
                new TextMultilineItem(getText(R.string.app_name_icanteen_burza_watcher), showDescription
                        ? getText(R.string.app_description_icanteen_burza_watcher) : null),
                new TextMultilineItem(getText(R.string.app_name_icanteen_burza), showDescription
                        ? getText(R.string.app_description_icanteen_burza) : null));

        RecyclerInflater.inflateToActivity(this, adapter, new RecyclerItemClickListener.ClickListener() {
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

            @Override
            public void onLongClick(View view, int position) {

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
