package cz.anty.purkynkamanager.utils.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Utils;

/**
 * Created by anty on 9.10.15.
 *
 * @author anty
 */
public class MainSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_main);

        final SharedPreferences preferences = getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE);
        CheckBox debug = (CheckBox) findViewById(R.id.check_box_debug);
        debug.setChecked(AppDataManager.isDebugMode());
        debug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDataManager.setDebugMode(((CheckBox)
                        findViewById(R.id.check_box_debug)).isChecked());
            }
        });

        final CheckBox useOnlyWifi = (CheckBox) findViewById(R.id.check_box_use_only_wifi);
        useOnlyWifi.setChecked(preferences.getBoolean(Constants.SETTING_NAME_USE_ONLY_WIFI, false));
        useOnlyWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences.edit().putBoolean(Constants.SETTING_NAME_USE_ONLY_WIFI,
                        useOnlyWifi.isChecked()).apply();
            }
        });

        final CheckBox showDescription = (CheckBox) findViewById(R.id.check_box_show_description);
        showDescription.setChecked(preferences.getBoolean(Constants.SETTING_NAME_SHOW_DESCRIPTION, true));
        showDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences.edit().putBoolean(Constants.SETTING_NAME_SHOW_DESCRIPTION,
                        showDescription.isChecked()).apply();
            }
        });

        final CheckBox firstStart = (CheckBox) findViewById(R.id.check_box_first_start);
        firstStart.setChecked(preferences.getInt(Constants.SETTING_NAME_FIRST_START, -1) == -1);
        firstStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstStart.isChecked()) {
                    preferences.edit().putInt(Constants
                            .SETTING_NAME_FIRST_START, -1).apply();
                    return;
                }
                firstStart.setChecked(true);
                startActivity(getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName())
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        findViewById(R.id.button_language).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.generateLanguageChangeDialog(MainSettingsActivity.this, new Runnable() {
                    @Override
                    public void run() {
                        //finish();
                        //startActivity(new Intent(MainSettingsActivity.this, MainSettingsActivity.class));
                /*Toast.makeText(MainSettingsActivity.this, R.string
                        .toast_text_restart_app_to_change_language, Toast.LENGTH_LONG).show();*/
                        startActivity(getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName())
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    }
                });
            }
        });
    }

}
