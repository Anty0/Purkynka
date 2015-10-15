package cz.anty.purkynkamanager.utils.timetable;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.Constants;

/**
 * Created by anty on 13.6.15.
 *
 * @author anty
 */
public class TimetableManager {

    private final Context context;
    private final List<Timetable> timetables = new ArrayList<>();

    public TimetableManager(Context context) {
        this.context = context;
        String[] timetablesNames = Timetables.getTimetablesNames(context);
        for (String timetableName : timetablesNames) {
            timetables.add(Timetable.loadTimetable(context, timetableName));
        }
    }

    public synchronized Timetable addTimetable(final Context context, String timetableName) {
        Timetable newTimetable = new Timetable(context, timetableName);
        timetables.add(newTimetable);
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences preferences = context.getSharedPreferences(
                        Constants.SETTINGS_NAME_TIMETABLES, Context.MODE_PRIVATE);
                if (preferences.getBoolean(Constants.SETTING_NAME_FIRST_START, true)) {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.dialog_title_timetable_widget_alert)
                                    //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon T
                            .setMessage(R.string.dialog_message_timetable_widget_alert)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    preferences.edit()
                                            .putBoolean(Constants.SETTING_NAME_FIRST_START, false)
                                            .apply();
                                }
                            })
                            .setNegativeButton(R.string.but_later, null)
                            .setCancelable(false)
                            .show();
                }
            }
        });
        return newTimetable;
    }

    public synchronized void removeTimetable(Timetable timetable) {
        timetables.remove(timetable);
        Timetables.removeTimetablesName(context, timetable.getName());
    }

    public synchronized Timetable[] getTimetables() {
        return timetables.toArray(new Timetable[timetables.size()]);
    }

    public synchronized Timetable getTimetableByName(String name) {
        for (Timetable timetable : timetables) {
            if (timetable.getName().equals(name))
                return timetable;
        }
        return null;
    }

    public synchronized int getIndexOf(Timetable timetable) {
        return timetables.indexOf(timetable);
    }
}
