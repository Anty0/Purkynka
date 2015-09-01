package cz.anty.utils.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ((CheckBox) findViewById(R.id.check_box_debug)).setChecked(AppDataManager.isDebugMode(this));
        ((CheckBox) findViewById(R.id.check_box_first_start)).setChecked(
                getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                        .getInt(Constants.SETTING_NAME_FIRST_START, -1) == -1);
    }

    public void onCheckBoxDebugClick(View view) {
        AppDataManager.setDebugMode(this, ((CheckBox) findViewById(R.id.check_box_debug)).isChecked());
    }

    public void onCheckBoxFirstStartClick(View view) {
        if (((CheckBox) findViewById(R.id.check_box_first_start)).isChecked()) {
            getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE).edit()
                    .putInt(Constants.SETTING_NAME_FIRST_START, -1).apply();
            return;
        }
        ((CheckBox) findViewById(R.id.check_box_first_start)).setChecked(true);
    }
}
