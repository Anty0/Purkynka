package cz.anty.purkynkamanager.modules.wifi;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThreadWithSpinner;
import cz.anty.purkynkamanager.utils.other.wifi.WifiLogin;
import cz.anty.purkynkamanager.utils.settings.WifiSettingsActivity;

public class WifiLoginActivity extends AppCompatActivity {

    private static final String LOG_TAG = "WifiLoginActivity";

    private OnceRunThreadWithSpinner worker;

    public static void save(String username, String password) {
        AppDataManager.login(AppDataManager.Type.WIFI, username, password);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (worker == null) worker = new OnceRunThreadWithSpinner(this);
        setContentView(R.layout.activity_wifi_login);

        ((EditText) findViewById(R.id.edit_username))
                .setText(AppDataManager.getUsername(AppDataManager.Type.WIFI));
        ((EditText) findViewById(R.id.edit_password))
                .setText(AppDataManager.getPassword(AppDataManager.Type.WIFI));
        ((TextView) findViewById(R.id.text_view_saved_login_attempts))
                .setText(Utils.getFormattedText(this, R.string.text_successful_login_attempts,
                        AppDataManager.getWifiSuccessfulLoginAttempts()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_default, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, WifiSettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickSave(@SuppressWarnings("UnusedParameters") View view) {
        save(((EditText) findViewById(R.id.edit_username)).getText().toString(),
                ((EditText) findViewById(R.id.edit_password)).getText().toString());
        Toast.makeText(this, R.string.text_login_data_successfully_saved, Toast.LENGTH_LONG).show();
    }

    public void onClickLogin(@SuppressWarnings("UnusedParameters") View view) {
        login(false);
    }

    private void login(boolean force) {
        if (!force) {
            WifiInfo wifiInfo = ((WifiManager) getApplicationContext()
                    .getSystemService(WIFI_SERVICE)).getConnectionInfo();
            String wifiName = wifiInfo.getSSID();
            if (wifiName == null || !wifiName.contains(WifiLogin.WIFI_NAME)) {
                new AlertDialog.Builder(WifiLoginActivity.this, R.style.AppTheme_Dialog_W)
                        .setTitle(R.string.exception_title_wifi_login)
                        .setMessage(R.string.exception_wifi_login_no_valid_wifi)
                        .setPositiveButton(R.string.but_continue, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                login(true);
                            }
                        })
                        .setNegativeButton(R.string.but_cancel, null)
                        .setIcon(R.mipmap.ic_launcher_wifi_no_border)
                        .setCancelable(true)
                        .show();
                return;
            }
        }
        final String username = ((EditText) findViewById(R.id.edit_username)).getText().toString();
        final String password = ((EditText) findViewById(R.id.edit_password)).getText().toString();
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                boolean result;
                try {
                    result = WifiLogin.tryLogin(WifiLoginActivity.this, username, password,
                            new Handler(getMainLooper()), null, false);
                } catch (IOException e) {
                    Log.d(LOG_TAG, "login", e);
                    result = false;
                }
                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WifiLoginActivity.this, R.string
                                    .toast_text_wifi_login_successful, Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(WifiLoginActivity.this, R.style.AppTheme_Dialog_W)
                                .setTitle(R.string.exception_title_wifi_login)
                                .setMessage(R.string.exception_wifi_login_wrong_login_or_wifi)
                                .setPositiveButton(R.string.but_ok, null)
                                .setIcon(R.mipmap.ic_launcher_wifi_no_border)
                                .setCancelable(true)
                                .show();
                    }
                });
            }
        }, getText(R.string.wait_text_logging_in));

    }

}
