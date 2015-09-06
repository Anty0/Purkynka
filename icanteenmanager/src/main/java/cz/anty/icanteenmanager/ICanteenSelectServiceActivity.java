package cz.anty.icanteenmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.listItem.MultilineAdapter;
import cz.anty.utils.listItem.MultilineItem;
import cz.anty.utils.listItem.TextMultilineItem;

public class ICanteenSelectServiceActivity extends AppCompatActivity {

    private MultilineAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = (ListView) findViewById(R.id.listView);
        adapter = new MultilineAdapter(this, R.layout.text_multi_line_list_item);
        listView.setAdapter(adapter);

        init();
    }

    private void init() {
        boolean showDescription = getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_SHOW_DESCRIPTION, true);
        MultilineItem[] data = new MultilineItem[]{
                new TextMultilineItem(getString(R.string.app_name_icanteen_burza), showDescription ? getString(R.string.app_description_icanteen_burza) : null),
                new TextMultilineItem(getString(R.string.app_name_icanteen_lunch_order), showDescription ? getString(R.string.app_description_icanteen_lunch_order) : null)
        };

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
                        startActivity(new Intent(ICanteenSelectServiceActivity.this, ICanteenBurzaActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(ICanteenSelectServiceActivity.this, ICanteenLunchOrderActivity.class));
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
        AppDataManager.logout(AppDataManager.Type.I_CANTEEN, this);
        startActivity(new Intent(this, ICanteenSplashActivity.class));
        finish();
    }
}
