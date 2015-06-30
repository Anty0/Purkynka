package cz.anty.wifiautologin;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import cz.anty.utils.LoginDataManager;
import cz.anty.utils.settings.WifiSettingsActivity;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;
import cz.anty.utils.wifi.WifiLogin;

public class WifiLoginActivity extends AppCompatActivity {

    private OnceRunThreadWithSpinner worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (worker == null) worker = new OnceRunThreadWithSpinner(this);
        setContentView(R.layout.activity_wifi_login);

        ((EditText) findViewById(R.id.editText)).setText(LoginDataManager.getUsername(LoginDataManager.Type.WIFI, this));
        ((EditText) findViewById(R.id.editText2)).setText(LoginDataManager.getPassword(LoginDataManager.Type.WIFI, this));
        ((CheckBox) findViewById(R.id.auto_login_checkbox)).setChecked(LoginDataManager.isWifiAutoLogin(this));
        ((CheckBox) findViewById(R.id.wait_login_checkbox)).setChecked(LoginDataManager.isWifiWaitLogin(this));
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

    public void onAutoCheckBoxClick(View view) {
        LoginDataManager.setWifiAutoLogin(this, ((CheckBox) findViewById(R.id.auto_login_checkbox)).isChecked());
    }

    public void onWaitCheckBoxClick(View view) {
        LoginDataManager.setWifiWaitLogin(this, ((CheckBox) findViewById(R.id.wait_login_checkbox)).isChecked());
    }

    public void onClickSave(View view) {
        LoginDataManager.login(LoginDataManager.Type.WIFI, this,
                ((EditText) findViewById(R.id.editText)).getText().toString(),
                ((EditText) findViewById(R.id.editText2)).getText().toString());
        Toast.makeText(this, R.string.successfully_saved, Toast.LENGTH_LONG).show();
    }

    public void onClickLogin(View view) {
        login(false);
    }

    private void login(boolean force) {
        if (!force) {
            WifiInfo wifiInfo = ((WifiManager) getSystemService(WIFI_SERVICE)).getConnectionInfo();
            if (!wifiInfo.getSSID().contains(WifiLogin.WIFI_NAME)) {
                new AlertDialog.Builder(WifiLoginActivity.this, R.style.AppTheme_Dialog)
                        .setTitle(R.string.wifi_login_exception_title)
                        .setMessage(R.string.wifi_login_exception_no_valid_wifi)
                        .setPositiveButton(R.string.but_continue, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                login(true);
                            }
                        })
                        .setNegativeButton(R.string.but_cancel, null)
                        .setIcon(R.mipmap.ic_launcher_wifi)
                        .setCancelable(true)
                        .show();
                return;
            }
        }
        final String username = ((EditText) findViewById(R.id.editText)).getText().toString();
        final String password = ((EditText) findViewById(R.id.editText2)).getText().toString();
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                if (WifiLogin.tryLogin(username, password)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WifiLoginActivity.this, R.string.wifi_login_successful, Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(WifiLoginActivity.this, R.style.AppTheme_Dialog)
                                .setTitle(R.string.wifi_login_exception_title)
                                .setMessage(R.string.wifi_login_exception_wrong_login_or_wifi)
                                .setPositiveButton(R.string.but_ok, null)
                                .setIcon(R.mipmap.ic_launcher_wifi)
                                .setCancelable(true)
                                .show();
                    }
                });
            }
        }, getString(R.string.logging_in));

    }

}
