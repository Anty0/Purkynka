package cz.anty.utils.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.R;

public class WifiSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_settings);

        ((CheckBox) findViewById(R.id.auto_login_checkbox)).setChecked(AppDataManager.isWifiAutoLogin());
        ((CheckBox) findViewById(R.id.wait_login_checkbox)).setChecked(AppDataManager.isWifiWaitLogin());
    }

    public void onAutoCheckBoxClick(@SuppressWarnings("UnusedParameters") View view) {
        AppDataManager.setWifiAutoLogin(((CheckBox) findViewById(R.id.auto_login_checkbox)).isChecked());
    }

    public void onWaitCheckBoxClick(@SuppressWarnings("UnusedParameters") View view) {
        AppDataManager.setWifiWaitLogin(((CheckBox) findViewById(R.id.wait_login_checkbox)).isChecked());
    }
}
