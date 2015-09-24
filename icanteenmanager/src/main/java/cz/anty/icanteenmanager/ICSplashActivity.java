package cz.anty.icanteenmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.service.ServiceManager;
import cz.anty.utils.thread.OnceRunThread;

public class ICSplashActivity extends AppCompatActivity {

    static ServiceManager<ICService.ICBinder> serviceManager;
    private final OnceRunThread worker = new OnceRunThread();

    private void startDefaultActivity() {
        Intent activity;
        if (AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN)) {
            activity = new Intent(this, ICSelectServiceActivity.class);
        } else {
            activity = new Intent(this, ICLoginActivity.class);
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

        worker.setPowerManager(this);
        //sendBroadcast(new Intent(this, StartServiceScheduleReceiver.class));

        if (serviceManager == null || !serviceManager.isConnected()) {
            serviceManager = new ServiceManager<>(this, ICService.class);
            serviceManager.addBinderConnection(
                    new ServiceManager.BinderConnection<ICService.ICBinder>() {
                        @Override
                        public void onBinderConnected(final ICService.ICBinder binder) {
                            worker.startWorker(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(Constants.WAIT_TIME_ON_BIND);
                                        binder.waitToWorkerStop();
                                    } catch (InterruptedException e) {
                                        Log.d("ICSplashActivity", "onBinderConnected", e);
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

    @Override
    protected void onStart() {
        super.onStart();
        serviceManager.connect();
    }
}
