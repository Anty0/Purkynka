package cz.anty.utils.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import cz.anty.utils.R;
import cz.anty.utils.listItem.MultilineAdapter;
import cz.anty.utils.listItem.TextMultilineItem;

public class SettingsActivity extends AppCompatActivity {

    private MultilineAdapter<TextMultilineItem> adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new MultilineAdapter<>(this);
        listView.setAdapter(adapter);

        initialize();
    }

    private void initialize() {
        TextMultilineItem[] data = new TextMultilineItem[]{
                new TextMultilineItem(getString(R.string.activity_title_sas_settings), null),
                new TextMultilineItem(getString(R.string.activity_title_wifi_settings), null),
                new TextMultilineItem(getString(R.string.activity_title_timetable_settings), null),
                new TextMultilineItem(getString(R.string.activity_title_attendance_settings), null),
                new TextMultilineItem(getString(R.string.activity_title_about), null)};

        adapter.clear();
        for (TextMultilineItem item : data) {
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
                        startActivity(new Intent(SettingsActivity.this, SASManagerSettingsActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(SettingsActivity.this, WifiSettingsActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(SettingsActivity.this, TimetableSettingsActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(SettingsActivity.this, AttendanceSettingsActivity.class));
                        break;
                    case 4:
                        startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                        break;
                }
            }

        });
    }

}
