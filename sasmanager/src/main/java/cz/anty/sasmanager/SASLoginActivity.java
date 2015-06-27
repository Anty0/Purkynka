package cz.anty.sasmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

import cz.anty.utils.LoginDataManager;
import cz.anty.utils.OnceRunThreadWithProgress;
import cz.anty.utils.WrongLoginDataException;
import cz.anty.utils.sas.SASManager;
import cz.anty.utils.settings.SASManagerSettingsActivity;

public class SASLoginActivity extends AppCompatActivity {

    //public static SASManager sasManager = null;
    private OnceRunThreadWithProgress saveThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*SharedPreferences preferences = getSharedPreferences("LoginData", MODE_PRIVATE);
        if (!preferences.getString("LOGIN", "").equals("") && !preferences.getString("PASSWORD", "").equals("")) {
            this.finish();
            startActivity(new Intent(this, SASManageActivity.class));
            return;
        }*/
        setContentView(R.layout.activity_login);

        ((EditText) findViewById(R.id.editText)).setText(LoginDataManager.getUsername(LoginDataManager.Type.SAS, this));
        ((EditText) findViewById(R.id.editText2)).setText(LoginDataManager.getPassword(LoginDataManager.Type.SAS, this));

        if (saveThread == null)
            saveThread = new OnceRunThreadWithProgress(this);
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
            startActivity(new Intent(this, SASManagerSettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickLogin(View view) {
        final String username = ((EditText) findViewById(R.id.editText)).getText().toString();
        final String password = ((EditText) findViewById(R.id.editText2)).getText().toString();
        if (username.equals("") || password.equals("")) {
            validateException(getString(R.string.validate_exception_wrong_login));
            return;
        }
        saveThread.startWorker(new Runnable() {
            @Override
            public void run() {
                try {
                    SASManager.validate(username, password);
                } catch (WrongLoginDataException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            validateException(getString(R.string.validate_exception_wrong_login));
                        }
                    });
                    return;
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            validateException(getString(R.string.validate_exception_no_connection));
                        }
                    });
                    return;
                }

                LoginDataManager.login(LoginDataManager.Type.SAS, SASLoginActivity.this, username, password);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //sendBroadcast(new Intent(SASLoginActivity.this, StartActivityReceiver.class));
                        startActivity(new Intent(SASLoginActivity.this, SASSplashActivity.class));
                        SASLoginActivity.this.finish();
                    }
                });

            }
        }, getString(R.string.logging_in));
    }

    private void validateException(String message) {
        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(R.string.validate_exception_title)
                .setMessage(message)
                .setPositiveButton(R.string.but_ok, null)
                .setIcon(R.mipmap.ic_launcher_sas)
                .setCancelable(true)
                .show();
    }
}
