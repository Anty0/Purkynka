package cz.anty.purkynkamanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import cz.anty.attendancemanager.AttendanceSettingsActivity;
import cz.anty.icanteenmanager.ICSettingsActivity;
import cz.anty.sasmanager.SASManagerSettingsActivity;
import cz.anty.timetablemanager.TimetableSettingsActivity;
import cz.anty.utils.list.listView.TextMultilineItem;
import cz.anty.utils.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerItemClickListener;
import cz.anty.utils.settings.AboutActivity;
import cz.anty.wifiautologin.WifiSettingsActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MultilineRecyclerAdapter<TextMultilineItem> adapter = new MultilineRecyclerAdapter<>();
        adapter.addAllItems(
                new TextMultilineItem(getText(R.string.activity_title_sas_settings), null),
                new TextMultilineItem(getText(R.string.activity_title_wifi_settings), null),
                new TextMultilineItem(getText(R.string.activity_title_icanteen_settings), null),
                new TextMultilineItem(getText(R.string.activity_title_timetable_settings), null),
                new TextMultilineItem(getText(R.string.activity_title_attendance_settings), null),
                new TextMultilineItem(getText(R.string.activity_title_about), null));

        RecyclerAdapter.inflateToActivity(this, null, adapter, new RecyclerItemClickListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                switch (position) {
                    case 0:
                        startActivity(new Intent(SettingsActivity.this, SASManagerSettingsActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(SettingsActivity.this, WifiSettingsActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(SettingsActivity.this, ICSettingsActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(SettingsActivity.this, TimetableSettingsActivity.class));
                        break;
                    case 4:
                        startActivity(new Intent(SettingsActivity.this, AttendanceSettingsActivity.class));
                        break;
                    case 5:
                        startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                        break;
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        });
    }

}
