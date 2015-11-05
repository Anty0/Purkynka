package cz.anty.purkynkamanager.utils.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.timetable.receiver.TimetableScheduleReceiver;
import cz.anty.purkynkamanager.utils.other.Constants;

public class TimetableSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_settings);

        CheckBox teachersWarningsCheckBox = (CheckBox) findViewById(R.id.check_box_display_teachers_warnings);
        teachersWarningsCheckBox.setChecked(getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_DISPLAY_TEACHERS_ATTENDANCE_WARNINGS, true));
        teachersWarningsCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, MODE_PRIVATE).edit()
                        .putBoolean(Constants.SETTING_NAME_DISPLAY_TEACHERS_ATTENDANCE_WARNINGS,
                                ((CheckBox) v).isChecked()).apply();
                sendBroadcast(new Intent(TimetableSettingsActivity.this, TimetableScheduleReceiver.class));
            }
        });

        CheckBox lessonWarningsCheckBox = (CheckBox) findViewById(R.id.check_box_display_lesson_warnings);
        lessonWarningsCheckBox.setChecked(getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLES, MODE_PRIVATE)
                        .getBoolean(Constants.SETTING_NAME_DISPLAY_LESSON_WARNINGS, false));
        lessonWarningsCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLES, MODE_PRIVATE).edit()
                        .putBoolean(Constants.SETTING_NAME_DISPLAY_LESSON_WARNINGS,
                                ((CheckBox) v).isChecked()).apply();
                sendBroadcast(new Intent(TimetableSettingsActivity.this, TimetableScheduleReceiver.class));
            }
        });
    }
}
