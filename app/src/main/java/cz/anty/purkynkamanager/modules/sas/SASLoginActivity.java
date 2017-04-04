package cz.anty.purkynkamanager.modules.sas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import java.io.IOException;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.WrongLoginDataException;
import cz.anty.purkynkamanager.utils.other.sas.SASManager;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThreadWithSpinner;
import cz.anty.purkynkamanager.utils.settings.SASManagerSettingsActivity;

public class SASLoginActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SASLoginActivity";

    //public static SASManager sasManager = null;
    private OnceRunThreadWithSpinner saveThread;

    public static boolean login(final Activity activity, String username, String password) {
        try {
            if (username.equals("") || password.equals(""))
                throw new WrongLoginDataException();

            SASManager.validate(username, password);
        } catch (final IOException e) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    validateException(activity, e instanceof WrongLoginDataException
                            ? activity.getText(R.string.exception_validate_wrong_login)
                            : activity.getText(R.string.exception_validate_no_connection));
                }
            });
            return false;
        }

        AppDataManager.login(AppDataManager.Type.SAS, username, password);
        return true;
    }

    private static void validateException(Context context, CharSequence message) {
        try {
            new AlertDialog.Builder(context, R.style.AppTheme_Dialog_S)
                    .setTitle(R.string.exception_title_validate)
                    .setMessage(message)
                    .setPositiveButton(R.string.but_ok, null)
                    .setIcon(R.mipmap.ic_launcher_sas_no_border)
                    .setCancelable(true)
                    .show();
        } catch (WindowManager.BadTokenException e) {
            Log.d(LOG_TAG, "validateException", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*SharedPreferences preferences = getSharedPreferences("LoginData", MODE_PRIVATE);
        if (!preferences.getString("LOGIN", "").equals("") && !preferences.getString("PASSWORD", "").equals("")) {
            this.finish();
            startActivity(new Intent(this, SASManageActivity.class));
            return;
        }*/
        setContentView(R.layout.activity_sas_login);

        ((EditText) findViewById(R.id.edit_username)).setText(AppDataManager.getUsername(AppDataManager.Type.SAS));
        ((EditText) findViewById(R.id.edit_password)).setText(AppDataManager.getPassword(AppDataManager.Type.SAS));

        if (saveThread == null)
            saveThread = new OnceRunThreadWithSpinner(this);
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

    public void onClickLogin(@SuppressWarnings("UnusedParameters") View view) {
        final String username = ((EditText) findViewById(R.id.edit_username)).getText().toString();
        final String password = ((EditText) findViewById(R.id.edit_password)).getText().toString();
        saveThread.startWorker(new Runnable() {
            @Override
            public void run() {
                if (login(SASLoginActivity.this, username, password))
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //sendBroadcast(new Intent(SASLoginActivity.this, StartActivityReceiver.class));
                            startActivity(new Intent(SASLoginActivity.this, SASSplashActivity.class));
                            finish();
                        }
                    });

            }
        }, getText(R.string.wait_text_logging_in));
    }
}
