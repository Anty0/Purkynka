package cz.anty.purkynkamanager.utils.other.timetable;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.timetable.widget.TimetableLessonWidget;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Utils;

/**
 * Created by anty on 13.6.15.
 *
 * @author anty
 */
public class TimetableManager {

    private static final String LOG_TAG = "TimetableManager";

    private final Context context;
    private final List<Timetable> timetables = new ArrayList<>();

    public TimetableManager(Context context) {
        this.context = context;
        String[] timetablesNames = Timetables.getTimetablesNames(context);
        for (String timetableName : timetablesNames) {
            timetables.add(Timetable.loadTimetable(context, timetableName));
        }
    }

    public synchronized Timetable addTimetable(final Context context, final String timetableName) {
        Timetable newTimetable = new Timetable(context, timetableName);
        timetables.add(newTimetable);
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences preferences = context.getSharedPreferences(
                        Constants.SETTINGS_NAME_TIMETABLES, Context.MODE_PRIVATE);
                if (preferences.getBoolean(Constants.SETTING_NAME_FIRST_START, true)) {
                    Utils.generateFirstStartDialog(context, new Intent().putExtra
                                    (TimetableLessonWidget.EXTRA_TIMETABLE_NAME, timetableName),
                            TimetableLessonWidget.class, R.style.AppTheme_Dialog_T,
                            context.getText(R.string.dialog_title_timetable_widget_alert),
                            context.getText(R.string.dialog_message_timetable_widget_alert),
                            R.mipmap.ic_launcher_t_no_border, new Runnable() {
                                @Override
                                public void run() {
                                    preferences.edit()
                                            .putBoolean(Constants.SETTING_NAME_FIRST_START, false)
                                            .apply();
                                }
                            });
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
