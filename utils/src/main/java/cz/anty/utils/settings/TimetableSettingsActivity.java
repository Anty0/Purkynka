package cz.anty.utils.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;

import cz.anty.utils.Constants;
import cz.anty.utils.R;

public class TimetableSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_settings);
        ((CheckBox) findViewById(R.id.checkBox))
                .setChecked(getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, MODE_PRIVATE)
                        .getBoolean(Constants.SETTING_NAME_DISPLAY_TEACHERS_ATTENDANCE_WARNINGS, false));
    }

    public void onCheckBoxClick(View view) {
        getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, MODE_PRIVATE).edit()
                .putBoolean(Constants.SETTING_NAME_DISPLAY_TEACHERS_ATTENDANCE_WARNINGS, ((CheckBox) view).isChecked())
                .apply();
    }
}
