package cz.anty.sasmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import cz.anty.sasmanager.receiver.StartServiceScheduleReceiver;
import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.service.ServiceManager;
import cz.anty.utils.thread.OnceRunThread;

public class SASSplashActivity extends AppCompatActivity {

    static ServiceManager<SASManagerService.SASBinder> serviceManager;
    private final OnceRunThread worker = new OnceRunThread();

    private void startDefaultActivity() {
        Intent activity;
        if (AppDataManager.isLoggedIn(AppDataManager.Type.SAS)) {
            activity = new Intent(this, SASManageActivity.class);
        } else {
            activity = new Intent(this, SASLoginActivity.class);
        }
        /*if (Build.VERSION.SDK_INT > 10)
            activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        else */
        //activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(activity);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_dialog);
        ((TextView) findViewById(R.id.message)).setText(R.string.wait_text_loading);

        worker.setPowerManager(this);
        sendBroadcast(new Intent(this, StartServiceScheduleReceiver.class));

        if (serviceManager == null || !serviceManager.isConnected()) {
            serviceManager = new ServiceManager<>(this, SASManagerService.class);
            serviceManager.addBinderConnection(
                    new ServiceManager.BinderConnection<SASManagerService.SASBinder>() {
                        @Override
                        public void onBinderConnected(final SASManagerService.SASBinder binder) {
                            worker.startWorker(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(Constants.WAIT_TIME_ON_BIND);
                                        binder.waitToWorkerStop();
                                    } catch (InterruptedException e) {
                                        Log.d("SASSplashActivity", "onBinderConnected", e);
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            startDefaultActivity();
                                        }
                                    });
                                }
                            });
                            serviceManager.removeBinderConnection(this);
                        }

                        @Override
                        public void onBinderDisconnected() {

                        }
                    });
            serviceManager.connect();
        } else startDefaultActivity();
    }
}
