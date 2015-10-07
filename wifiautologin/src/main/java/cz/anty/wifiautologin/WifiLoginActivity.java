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
import android.widget.EditText;
import android.widget.Toast;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;
import cz.anty.utils.wifi.WifiLogin;

public class WifiLoginActivity extends AppCompatActivity {

    private OnceRunThreadWithSpinner worker;

    static void save(String username, String password) {
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
            WifiInfo wifiInfo = ((WifiManager) getSystemService(WIFI_SERVICE)).getConnectionInfo();
            if (!wifiInfo.getSSID().contains(WifiLogin.WIFI_NAME)) {
                new AlertDialog.Builder(WifiLoginActivity.this, R.style.AppTheme_Dialog)
                        .setTitle(R.string.exception_title_wifi_login)
                        .setMessage(R.string.exception_wifi_login_no_valid_wifi)
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
        final String username = ((EditText) findViewById(R.id.edit_username)).getText().toString();
        final String password = ((EditText) findViewById(R.id.edit_password)).getText().toString();
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                if (WifiLogin.tryLogin(username, password)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WifiLoginActivity.this, R.string.toast_text_wifi_login_successful, Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(WifiLoginActivity.this, R.style.AppTheme_Dialog)
                                .setTitle(R.string.exception_title_wifi_login)
                                .setMessage(R.string.exception_wifi_login_wrong_login_or_wifi)
                                .setPositiveButton(R.string.but_ok, null)
                                .setIcon(R.mipmap.ic_launcher_wifi)
                                .setCancelable(true)
                                .show();
                    }
                });
            }
        }, getText(R.string.wait_text_logging_in));

    }

}
