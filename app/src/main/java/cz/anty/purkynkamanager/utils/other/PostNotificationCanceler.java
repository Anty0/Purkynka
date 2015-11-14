package cz.anty.purkynkamanager.utils.other;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by anty on 14.11.2015.
 *
 * @author anty
 */
public class PostNotificationCanceler extends BroadcastReceiver {

    private static final String EXTRA_NOTIFICATION_ID = "NOTIFICATION_ID";

    public static void postNotificationCancel(Context context, int notificationId, long waitTime) {
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                .set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + waitTime, PendingIntent
                        .getBroadcast(context, 0, new Intent(context, PostNotificationCanceler.class)
                                .putExtra(EXTRA_NOTIFICATION_ID, notificationId), 0));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
        if (notificationId >= 0)
            ((NotificationManager) context.getSystemService(Context
                    .NOTIFICATION_SERVICE)).cancel(notificationId);
    }
}
