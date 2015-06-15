package cz.anty.sasmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import cz.anty.sasmanager.receiver.ScheduleReceiver;
import cz.anty.utils.LoginDataManager;

public class SASSplashActivity extends AppCompatActivity {

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, final IBinder binder) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long time = System.currentTimeMillis();
                    SASManagerService.MyBinder myBinder = (SASManagerService.MyBinder) binder;
                    myBinder.refresh();
                    myBinder.waitToWorkerStop();
                    long timeNew = System.currentTimeMillis();
                    if (timeNew - time < 1500)
                        try {
                            Thread.sleep(time - timeNew + 1500);
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //sendBroadcast(new Intent(SASSplashActivity.this, StartActivityReceiver.class));
                            //new StartActivityReceiver().onReceive(SASSplashActivity.this, null);
                            startDefaultActivity(SASSplashActivity.this);
                        }
                    });
                }
            }).start();
        }

        public void onServiceDisconnected(ComponentName className) {

        }

    };

    private static void startDefaultActivity(Context context) {
        Intent activity;
        if (LoginDataManager.isLoggedIn(LoginDataManager.Type.SAS, context)) {
            activity = new Intent(context.getApplicationContext(), SASManageActivity.class);
        } else {
            activity = new Intent(context.getApplicationContext(), SASLoginActivity.class);
        }
        if (Build.VERSION.SDK_INT > 10)
            activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        else activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activity);
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
        Intent intent = new Intent(this, SASManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }
}
