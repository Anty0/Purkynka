package cz.anty.attendancemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.anty.utils.OnceRunThreadWithProgress;
import cz.anty.utils.StableArrayAdapter;
import cz.anty.utils.attendance.AttendanceConnector;
import cz.anty.utils.attendance.man.Man;
import cz.anty.utils.attendance.man.Mans;

public class SearchActivity extends AppCompatActivity {

    private final AttendanceConnector connector = new AttendanceConnector();
    private OnceRunThreadWithProgress worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        if (worker == null)
            worker = new OnceRunThreadWithProgress(this);
        onUpdate(null);
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

    public void onCheckBoxClick(View view) {
        getSharedPreferences("AttendanceData", MODE_PRIVATE).edit()
                .putBoolean("DISPLAY_WARNING", ((CheckBox) view).isChecked())
                .apply();
        sendBroadcast(new Intent(this, ScheduleReceiver.class));
    }

    public void onUpdate(View view) {
        final String toSearch = ((EditText) findViewById(R.id.editText)).getText().toString();//TODO add auto complete using timetable and marks lessons
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                String[] values;
                try {
                    List<Man> mans = Mans.parseMans(connector.getSupElements(toSearch, 1));
                    values = new String[mans.size()];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = mans.get(i).toString();
                    }
                } catch (IOException e) {
                    values = new String[]{"Connection exception: " + e.getMessage() + "\n" + "Check your internet connection"};
                    e.printStackTrace();
                }

                final ArrayList<String> list = new ArrayList<>();
                Collections.addAll(list, values);
                final StableArrayAdapter adapter = new StableArrayAdapter(SearchActivity.this,
                        android.R.layout.simple_list_item_1, list);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListView listView = ((ListView) findViewById(R.id.listView));
                        listView.setAdapter(adapter);
                    }
                });
            }
        }, getString(R.string.searching));
    }
}
