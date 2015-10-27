package cz.anty.purkynkamanager.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThreadWithSpinner;
import cz.anty.purkynkamanager.utils.other.update.UpdateConnector;

/**
 * Created by anty on 18.10.15.
 *
 * @author anty
 */
public class SendFeedbackActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SendFeedbackActivity";

    private OnceRunThreadWithSpinner worker;
    private EditText mTitle, mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        worker = new OnceRunThreadWithSpinner(this);
        mTitle = (EditText) findViewById(R.id.edit_text_title);
        mText = (EditText) findViewById(R.id.edit_text_text);
        findViewById(R.id.image_button_send)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        send();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_send_feedback) {
            send();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void send() {
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                try {
                    UpdateConnector.sendFeedback(mTitle.getText().toString(), mText.getText().toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE).edit()
                                    .putInt(Constants.SETTING_NAME_LATEST_EXCEPTION_CODE, -1).apply();
                            Toast.makeText(SendFeedbackActivity.this,
                                    R.string.toast_text_feedback_sent,
                                    Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                } catch (IOException e) {
                    Log.d(LOG_TAG, "onOptionsItemSelected", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SendFeedbackActivity.this,
                                    R.string.toast_text_feedback_not_sent,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }, getText(R.string.wait_text_please_wait));
    }
}
