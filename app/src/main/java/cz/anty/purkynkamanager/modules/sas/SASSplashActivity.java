package cz.anty.purkynkamanager.modules.sas;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.sas.receiver.StartServiceScheduleReceiver;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.service.ServiceManager;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThread;

public class SASSplashActivity extends AppCompatActivity {

    public static ServiceManager<SASManagerService.SASBinder> serviceManager;
    private final OnceRunThread worker = new OnceRunThread();

    private boolean mRunning = true;

    public static void initService(Context context, final OnceRunThread worker, final Runnable onComplete) {
        if (serviceManager == null || !serviceManager.isConnected()) {
            serviceManager = new ServiceManager<>(context, SASManagerService.class);
            serviceManager.addBinderConnection(
                    new ServiceManager.BinderConnection<SASManagerService.SASBinder>() {
                        @Override
                        public void onBinderConnected(final SASManagerService.SASBinder binder) {
                            serviceManager.removeBinderConnection(this);
                            worker.startWorker(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(Constants.WAIT_TIME_ON_BIND);
                                        //binder.waitToWorkerStop();
                                    } catch (InterruptedException e) {
                                        Log.d("SASSplashActivity", "onBinderConnected", e);
                                    }

                                    onComplete.run();
                                }
                            });
                        }

                        @Override
                        public void onBinderDisconnected() {

                        }
                    });
            serviceManager.connect();
        } else onComplete.run();
    }

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
        setContentView(R.layout.activity_splash);
        ((TextView) findViewById(R.id.message)).setText(R.string.wait_text_loading);

        worker.setPowerManager(this);
        sendBroadcast(new Intent(this, StartServiceScheduleReceiver.class));

        initService(this, worker, new Runnable() {
            @Override
            public void run() {
                if (mRunning)
                    startDefaultActivity();
            }
        });
    }

    @Override
    protected void onDestroy() {
        mRunning = false;
        super.onDestroy();
    }
}
