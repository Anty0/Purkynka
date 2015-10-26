package cz.anty.purkynkamanager.modules.icanteen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.icanteen.receiver.StartServiceScheduleReceiver;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.WrongLoginDataException;
import cz.anty.purkynkamanager.utils.other.icanteen.ICManager;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThreadWithSpinner;
import cz.anty.purkynkamanager.utils.settings.ICSettingsActivity;

public class ICLoginActivity extends AppCompatActivity {

    private OnceRunThreadWithSpinner saveThread;

    public static boolean login(final Activity activity, String username, String password) {
        try {
            if (username.equals("") || password.equals(""))
                throw new WrongLoginDataException();

            ICManager.validate(username, password);
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

        AppDataManager.login(AppDataManager.Type.I_CANTEEN, username, password);
        activity.sendBroadcast(new Intent(activity, StartServiceScheduleReceiver.class));
        return true;
    }

    private static void validateException(Context context, CharSequence message) {
        new AlertDialog.Builder(context, R.style.AppTheme_Dialog_IC)
                .setTitle(R.string.exception_title_validate)
                .setMessage(message)
                .setPositiveButton(R.string.but_ok, null)
                .setIcon(R.mipmap.ic_launcher_ic)
                .setCancelable(true)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icanteen_login);

        String username, password;
        if (AppDataManager.isLoggedIn(AppDataManager.Type.WIFI)
                && !AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN)) {
            username = AppDataManager.getUsername(AppDataManager.Type.WIFI);
            password = AppDataManager.getPassword(AppDataManager.Type.WIFI);
        } else {
            username = AppDataManager.getUsername(AppDataManager.Type.I_CANTEEN);
            password = AppDataManager.getPassword(AppDataManager.Type.I_CANTEEN);
        }
        ((EditText) findViewById(R.id.edit_username)).setText(username);
        ((EditText) findViewById(R.id.edit_password)).setText(password);

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, ICSettingsActivity.class));
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
                if (login(ICLoginActivity.this, username, password))
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //sendBroadcast(new Intent(SASLoginActivity.this, StartActivityReceiver.class));
                            startActivity(new Intent(ICLoginActivity.this, ICSplashActivity.class));
                            finish();
                        }
                    });

            }
        }, getText(R.string.wait_text_logging_in));
    }
}
