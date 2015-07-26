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
import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.thread.OnceRunThread;

public class SASSplashActivity extends AppCompatActivity {

    private final OnceRunThread worker = new OnceRunThread(null);
    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, final IBinder binder) {
            worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    SASManagerService.MyBinder myBinder = (SASManagerService.MyBinder) binder;
                    try {
                        Thread.sleep(Constants.WAIT_TIME_SAS_SPLASH_ON_BIND);
                    } catch (InterruptedException e) {
                        if (AppDataManager.isDebugMode(SASSplashActivity.this))
                            Log.d(null, null, e);
                    }
                    myBinder.waitToWorkerStop();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startDefaultActivity();
                            finish();
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
        if (AppDataManager.isLoggedIn(AppDataManager.Type.SAS, this)) {
            activity = new Intent(this, SASManageActivity.class);
        } else {
            activity = new Intent(this, SASLoginActivity.class);
        }
        if (Build.VERSION.SDK_INT > 10)
            activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        else activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        worker.setPowerManager(this);
        sendBroadcast(new Intent(this, ScheduleReceiver.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (exit) {
            finish();
            return;
        }*/
        /*if (getSharedPreferences("MainData", Context.MODE_PRIVATE).getBoolean("CANT_START", false)) {
            new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                    .setTitle(R.string.notification_update_title)
                    .setMessage(R.string.notification_update_text_sas)
                    .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(R.mipmap.ic_launcher_sas)
                    .setCancelable(false)
                    .show();
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
