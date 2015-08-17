package cz.anty.icanteenmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.WrongLoginDataException;
import cz.anty.utils.icanteen.ICanteenManager;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;

public class ICanteenLoginActivity extends AppCompatActivity {

    private OnceRunThreadWithSpinner saveThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ((EditText) findViewById(R.id.editText)).setText(AppDataManager.getUsername(AppDataManager.Type.I_CANTEEN, this));
        ((EditText) findViewById(R.id.editText2)).setText(AppDataManager.getPassword(AppDataManager.Type.I_CANTEEN, this));

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
            //TODO open settings
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickLogin(@SuppressWarnings("UnusedParameters") View view) {
        final String username = ((EditText) findViewById(R.id.editText)).getText().toString();
        final String password = ((EditText) findViewById(R.id.editText2)).getText().toString();
        if (username.equals("") || password.equals("")) {
            validateException(getString(R.string.exception_validate_wrong_login));
            return;
        }
        saveThread.startWorker(new Runnable() {
            @Override
            public void run() {
                try {
                    ICanteenManager.validate(username, password);
                } catch (WrongLoginDataException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            validateException(getString(R.string.exception_validate_wrong_login));
                        }
                    });
                    return;
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            validateException(getString(R.string.exception_validate_no_connection));
                        }
                    });
                    return;
                }

                AppDataManager.login(AppDataManager.Type.I_CANTEEN, ICanteenLoginActivity.this, username, password);
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

    private void validateException(String message) {
        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(R.string.exception_title_validate)
                .setMessage(message)
                .setPositiveButton(R.string.but_ok, null)
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(true)
                .show();
    }
}
