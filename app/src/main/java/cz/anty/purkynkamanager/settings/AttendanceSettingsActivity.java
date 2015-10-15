package cz.anty.purkynkamanager.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.attendance.receiver.TrackingScheduleReceiver;
import cz.anty.purkynkamanager.utils.Constants;

public class AttendanceSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_settings);

        CheckBox trackingWarningsCheckBox = (CheckBox) findViewById(R.id.check_box_display_tracking_warnings);
        trackingWarningsCheckBox.setChecked(getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, MODE_PRIVATE)
                        .getBoolean(Constants.SETTING_NAME_DISPLAY_TRACKING_ATTENDANCE_WARNINGS, true));
        trackingWarningsCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, MODE_PRIVATE).edit()
                        .putBoolean(Constants.SETTING_NAME_DISPLAY_TRACKING_ATTENDANCE_WARNINGS,
                                ((CheckBox) v).isChecked()).apply();
                sendBroadcast(new Intent(AttendanceSettingsActivity.this, TrackingScheduleReceiver.class));
            }
        });
    }
}
