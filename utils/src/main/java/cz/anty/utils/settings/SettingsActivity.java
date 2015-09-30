package cz.anty.utils.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import cz.anty.utils.R;
import cz.anty.utils.list.listView.TextMultilineItem;
import cz.anty.utils.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerItemClickListener;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MultilineRecyclerAdapter<TextMultilineItem> adapter = new MultilineRecyclerAdapter<>();
        adapter.addAllItems(new TextMultilineItem(getString(R.string.activity_title_sas_settings), null),
                new TextMultilineItem(getString(R.string.activity_title_wifi_settings), null),
                new TextMultilineItem(getString(R.string.activity_title_timetable_settings), null),
                new TextMultilineItem(getString(R.string.activity_title_attendance_settings), null),
                new TextMultilineItem(getString(R.string.activity_title_about), null));

        RecyclerAdapter.inflateToActivity(this, null, adapter, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
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
