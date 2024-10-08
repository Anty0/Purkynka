package cz.anty.purkynkamanager.modules.attendance.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.util.List;

import cz.anty.purkynkamanager.ApplicationBase;
import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.attendance.TrackingActivity;
import cz.anty.purkynkamanager.modules.attendance.widget.TrackingWidget;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.attendance.AttendanceConnector;
import cz.anty.purkynkamanager.utils.other.attendance.man.Man;
import cz.anty.purkynkamanager.utils.other.attendance.man.Mans;
import cz.anty.purkynkamanager.utils.other.attendance.man.TrackingMansManager;

public class TrackingReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "TrackingReceiver";

    public static synchronized TrackingMansManager refreshTrackingMans(Context context, @Nullable TrackingMansManager mansManager, boolean updateWidget) {
        if (mansManager == null) mansManager = new TrackingMansManager(context);
        AttendanceConnector connector = new AttendanceConnector();
        Man[] mans = mansManager.get();

        for (Man man : mans) {
            try {
                String search = man.getName().split(" ")[0];
                List<Man> manList = Mans.parseMans(connector
                        .getSupElements(search, 1));
                Man findMan = null;
                for (int index = 0; index < manList.size(); index++) {
                    findMan = manList.get(index);
                    if (findMan.equals(man)) break;
                    else findMan = null;
                }

                if (findMan != null) {
                    int index = mansManager.indexOf(man);
                    mansManager.remove(man)
                            .add(index, findMan);

                    if (man.isInSchool() != findMan.isInSchool()) {
                        Notification n = new NotificationCompat.Builder(context)
                                .setContentTitle(findMan.getName() + " " + findMan.getClassString())
                                .setContentText(Utils.getFormattedText(context,
                                        Man.IsInSchoolState.IN_SCHOOL.equals(findMan.isInSchool()) ?
                                                R.string.notify_text_tracked_is_in_school :
                                                R.string.notify_text_tracked_is_in_not_school,
                                        findMan.getName()) + " (" + findMan.getLastEnterAsString() + ")")
                                .setSmallIcon(R.mipmap.ic_launcher_a_no_border)
                                .setContentIntent(PendingIntent.getActivity(context, 0,
                                        new Intent(context, TrackingActivity.class), 0))
                                .setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                        //.addAction(R.mipmap.ic_launcher, "And more", pIntent)
                                .build();

                        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                                .notify(Constants.NOTIFICATION_ID_TRACKING + (index < 10 ? index : 9), n);
                    }
                    context.getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE).edit()
                            .putLong(man.getName() + Constants.SETTING_NAME_ADD_LAST_UPDATE, System.currentTimeMillis()).apply();
                }
            } catch (IOException e) {
                Log.d(LOG_TAG, "onReceive", e);
            }

        }

        mansManager.apply();
        if (updateWidget)
            TrackingWidget.callUpdate(context, false);
        return mansManager;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final TrackingMansManager mansManager = new TrackingMansManager(context);
        if (!context.getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_DISPLAY_TRACKING_ATTENDANCE_WARNINGS, true)
                || mansManager.get().length == 0) {
            context.sendBroadcast(new Intent(context, TrackingScheduleReceiver.class));
            return;
        }

        ApplicationBase.WORKER.startWorker(new Runnable() {
            @Override
            public void run() {
                TrackingActivity.mansManager = refreshTrackingMans(context,
                        TrackingActivity.mansManager, true);
            }
        }, Build.VERSION.SDK_INT >= 11 ? goAsync() : null);
    }
}
