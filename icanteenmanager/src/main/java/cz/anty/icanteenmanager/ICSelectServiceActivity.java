package cz.anty.icanteenmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.list.listView.TextMultilineItem;
import cz.anty.utils.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerItemClickListener;

public class ICSelectServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean showDescription = getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_SHOW_DESCRIPTION, true);
        MultilineRecyclerAdapter<TextMultilineItem> adapter = new MultilineRecyclerAdapter<>();
        adapter.clearItems();
        adapter.addAllItems(new TextMultilineItem(getString(R.string.app_name_icanteen_burza),
                        showDescription ? getString(R.string.app_description_icanteen_burza) : null),
                new TextMultilineItem(getString(R.string.app_name_icanteen_lunch_order),
                        showDescription ? getString(R.string.app_description_icanteen_lunch_order) : null));

        RecyclerAdapter.inflateToActivity(this, null, adapter, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                switch (position) {
                    case 0:
                        startActivity(new Intent(ICSelectServiceActivity.this, ICBurzaActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(ICSelectServiceActivity.this, ICLunchOrderActivity.class));
                        break;
                }
            }
        });
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, R.string.toast_text_coming_soon,
                    Toast.LENGTH_LONG).show();
            //TODO open settings
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
