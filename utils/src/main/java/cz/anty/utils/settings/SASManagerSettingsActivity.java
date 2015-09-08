package cz.anty.utils.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.R;

public class SASManagerSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sasmanager_settings);
        ((CheckBox) findViewById(R.id.check_box_sas_marks_update)).setChecked(AppDataManager.isSASMarksAutoUpdate());
    }

    public void onCheckBoxSASMarksUpdateClick(View view) {
        AppDataManager.setSASMarksAutoUpdate(((CheckBox) findViewById(R.id.check_box_sas_marks_update)).isChecked());
    }
}
