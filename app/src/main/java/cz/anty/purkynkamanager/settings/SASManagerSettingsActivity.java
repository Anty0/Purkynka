package cz.anty.purkynkamanager.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.sas.receiver.StartServiceScheduleReceiver;
import cz.anty.purkynkamanager.utils.AppDataManager;

public class SASManagerSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sasmanager_settings);

        final CheckBox marksUpdateCheckBox = (CheckBox) findViewById(R.id.check_box_sas_marks_update);
        marksUpdateCheckBox.setChecked(AppDataManager.isSASMarksAutoUpdate());
        marksUpdateCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDataManager.setSASMarksAutoUpdate(marksUpdateCheckBox.isChecked());
                sendBroadcast(new Intent(SASManagerSettingsActivity.this, StartServiceScheduleReceiver.class));
            }
        });
    }
}
