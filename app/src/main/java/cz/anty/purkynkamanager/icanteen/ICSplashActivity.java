package cz.anty.purkynkamanager.icanteen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.icanteen.receiver.StartServiceScheduleReceiver;
import cz.anty.purkynkamanager.utils.AppDataManager;
import cz.anty.purkynkamanager.utils.Constants;
import cz.anty.purkynkamanager.utils.Log;
import cz.anty.purkynkamanager.utils.service.ServiceManager;
import cz.anty.purkynkamanager.utils.thread.OnceRunThread;

public class ICSplashActivity extends AppCompatActivity {

    static ServiceManager<ICService.ICBinder> serviceManager;
    private final OnceRunThread worker = new OnceRunThread();

    public static void initService(Context context, final OnceRunThread worker, final Runnable onComplete) {
        if (serviceManager == null || !serviceManager.isConnected()) {
            serviceManager = new ServiceManager<>(context, ICService.class);
            serviceManager.addBinderConnection(
                    new ServiceManager.BinderConnection<ICService.ICBinder>() {
                        @Override
                        public void onBinderConnected(final ICService.ICBinder binder) {
                            serviceManager.removeBinderConnection(this);
                            worker.startWorker(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(Constants.WAIT_TIME_ON_BIND);
                                        binder.waitToWorkerStop();
                                    } catch (InterruptedException e) {
                                        Log.d("ICSplashActivity", "onBinderConnected", e);
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
        ((TextView) findViewById(R.id.message)).setText(R.string.wait_text_loading);

        worker.setPowerManager(this);
        sendBroadcast(new Intent(this, StartServiceScheduleReceiver.class));

        initService(this, worker, new Runnable() {
            @Override
            public void run() {
                startDefaultActivity();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        serviceManager.connect();
    }
}
