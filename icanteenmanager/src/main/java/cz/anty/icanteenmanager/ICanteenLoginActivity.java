package cz.anty.icanteenmanager;

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
import android.widget.Toast;

import java.io.IOException;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.WrongLoginDataException;
import cz.anty.utils.icanteen.ICanteenManager;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;

public class ICanteenLoginActivity extends AppCompatActivity {

    private OnceRunThreadWithSpinner saveThread;

    static boolean login(final Activity activity, String username, String password) {
        try {
            if (username.equals("") || password.equals(""))
                throw new WrongLoginDataException();

            ICanteenManager.validate(username, password);
        } catch (final IOException e) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    validateException(activity, e instanceof WrongLoginDataException
                            ? activity.getString(R.string.exception_validate_wrong_login)
                            : activity.getString(R.string.exception_validate_no_connection));
                }
            });
            return false;
        }

        AppDataManager.login(AppDataManager.Type.I_CANTEEN, username, password);
        return true;
    }

    private static void validateException(Context context, String message) {
        new AlertDialog.Builder(context, R.style.AppTheme_Dialog)
                .setTitle(R.string.exception_title_validate)
                .setMessage(message)
                .setPositiveButton(R.string.but_ok, null)
                        //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon iC
                .setCancelable(true)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icanteen_login);

        ((EditText) findViewById(R.id.edit_username)).setText(AppDataManager.getUsername(AppDataManager.Type.I_CANTEEN));
        ((EditText) findViewById(R.id.edit_password)).setText(AppDataManager.getPassword(AppDataManager.Type.I_CANTEEN));

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
            Toast.makeText(this, R.string.toast_text_coming_soon,
                    Toast.LENGTH_LONG).show();
            //TODO open settings
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
                if (login(ICanteenLoginActivity.this, username, password))
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //sendBroadcast(new Intent(SASLoginActivity.this, StartActivityReceiver.class));
                            startActivity(new Intent(ICanteenLoginActivity.this, ICanteenSplashActivity.class));
                            finish();
                        }
                    });

            }
        }, getString(R.string.wait_text_logging_in));
    }
}
