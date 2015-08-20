package cz.anty.icanteenmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.thread.OnceRunThread;

public class ICanteenSplashActivity extends AppCompatActivity {

    private final OnceRunThread worker = new OnceRunThread();
    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, final IBinder binder) {
            worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    ICanteenService.MyBinder myBinder = (ICanteenService.MyBinder) binder;
                    try {
                        Thread.sleep(Constants.WAIT_TIME_ON_BIND);
                    } catch (InterruptedException e) {
                        if (AppDataManager.isDebugMode(ICanteenSplashActivity.this))
                            Log.d("ICanteenSplashActivity", "onServiceConnected", e);
                    }
                    myBinder.waitToWorkerStop();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startDefaultActivity();
                        }
                    });
                }
            });
        }

        public void onServiceDisconnected(ComponentName className) {

        }

    };

    private void startDefaultActivity() {
        Intent activity;
        if (AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN, this)) {
            activity = new Intent(this, ICanteenSelectServiceActivity.class);
        } else {
            activity = new Intent(this, ICanteenLoginActivity.class);
        }
        /*if (Build.VERSION.SDK_INT > 10)
            activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        else */
        activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(activity);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        worker.setPowerManager(this);
        //sendBroadcast(new Intent(this, StartServiceScheduleReceiver.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, ICanteenService.class),
                mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        worker.waitToWorkerStop();
        unbindService(mConnection);
        super.onStop();
    }
}
