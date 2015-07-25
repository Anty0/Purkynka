package cz.anty.utils.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;

import cz.anty.utils.R;

public class TimetableSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_settings);
        ((CheckBox) findViewById(R.id.checkBox))
                .setChecked(getSharedPreferences("AttendanceData", MODE_PRIVATE)
                        .getBoolean("DISPLAY_WARNING", false));
    }

    public void onCheckBoxClick(View view) {
        getSharedPreferences("AttendanceData", MODE_PRIVATE).edit()
                .putBoolean("DISPLAY_WARNING", ((CheckBox) view).isChecked())
                .apply();
    }
}
