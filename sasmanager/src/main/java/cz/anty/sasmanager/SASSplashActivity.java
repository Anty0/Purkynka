package cz.anty.sasmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import cz.anty.sasmanager.receiver.ScheduleReceiver;
import cz.anty.utils.LoginDataManager;
import cz.anty.utils.OnceRunThread;

public class SASSplashActivity extends AppCompatActivity {

    private static final int WAIT_TIME = 100;
    private static final int MIN_LENGTH_TIME = 500;

    //private boolean exit = false;
    private OnceRunThread worker = new OnceRunThread();
    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, final IBinder binder) {
            worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(WAIT_TIME);
                    } catch (InterruptedException e) {
                        Log.d(null, null, e);
                    }
                    long time = System.currentTimeMillis();
                    SASManagerService.MyBinder myBinder = (SASManagerService.MyBinder) binder;
                    //myBinder.refresh();
                    myBinder.waitToWorkerStop();
                    long timeNew = System.currentTimeMillis();
                    if (timeNew - time < MIN_LENGTH_TIME)
                        try {
                            Thread.sleep(time - timeNew + MIN_LENGTH_TIME);
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //sendBroadcast(new Intent(SASSplashActivity.this, StartActivityReceiver.class));
                            //new StartActivityReceiver().onReceive(SASSplashActivity.this, null);
                            finish();
                            startDefaultActivity();
                            //exit = true;
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
        if (LoginDataManager.isLoggedIn(LoginDataManager.Type.SAS, this)) {
            activity = new Intent(this, SASManageActivity.class);
        } else {
            activity = new Intent(this, SASLoginActivity.class);
        }
        if (Build.VERSION.SDK_INT > 10)
            activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        else activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(activity);
        //this.startActivityFromChild(getParent(), activity, -1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sendBroadcast(new Intent(this, ScheduleReceiver.class));
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (exit) {
            finish();
            return;
        }*/
        Intent intent = new Intent(this, SASManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        worker.waitToWorkerStop();
        super.onPause();
        unbindService(mConnection);
    }
}
