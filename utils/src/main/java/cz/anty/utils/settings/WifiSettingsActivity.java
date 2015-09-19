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

        final CheckBox autoLoginCheckBox = (CheckBox) findViewById(R.id.auto_login_checkbox);
        autoLoginCheckBox.setChecked(AppDataManager.isWifiAutoLogin());
        autoLoginCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDataManager.setWifiAutoLogin(autoLoginCheckBox.isChecked());
            }
        });

        final CheckBox waitLoginCheckBox = (CheckBox) findViewById(R.id.wait_login_checkbox);
        waitLoginCheckBox.setChecked(AppDataManager.isWifiWaitLogin());
        waitLoginCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDataManager.setWifiWaitLogin(waitLoginCheckBox.isChecked());
            }
        });
    }
}
