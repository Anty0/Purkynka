package cz.anty.purkynkamanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;

import cz.anty.attendancemanager.SearchActivity;
import cz.anty.sasmanager.SASSplashActivity;
import cz.anty.timetablemanager.TimetableSelectActivity;
import cz.anty.utils.listItem.StableArrayAdapter;
import cz.anty.wifiautologin.WifiLoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getPreferences(MODE_PRIVATE).getBoolean("FIRST_START", true)) {
            new AlertDialog.Builder(this, cz.anty.sasmanager.R.style.AppTheme_Dialog)
                    .setTitle(R.string.title_terms)
                    .setMessage(R.string.text_terms)
                    .setPositiveButton(R.string.but_accept, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getPreferences(MODE_PRIVATE).edit()
                                    .putBoolean("FIRST_START", false).apply();
                            initialize();
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
            return;
        }
        initialize();
    }

    private void initialize() {
        ListView listView = ((ListView) findViewById(R.id.listView));
        String[] values = new String[]{getString(R.string.sas_app_name), getString(R.string.wifi_app_name), getString(R.string.timetable_app_name), getString(R.string.attendance_app_name)};
        final ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, values);

        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

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
                        startActivity(new Intent(MainActivity.this, TimetableSelectActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(MainActivity.this, SearchActivity.class));
                }
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_default, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
