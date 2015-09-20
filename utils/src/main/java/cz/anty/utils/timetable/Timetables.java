package cz.anty.utils.timetable;

import android.content.Context;
import android.content.SharedPreferences;

import cz.anty.utils.Arrays;
import cz.anty.utils.Constants;

/**
 * Created by anty on 13.6.15.
 *
 * @author anty
 */
public class Timetables {

    synchronized static String[] getTimetablesNames(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLES, Context.MODE_PRIVATE);
        int size = preferences.getInt(Constants.SETTING_NAME_SIZE, 0);
        String[] timetablesNames = new String[size];
        for (int i = 0; i < size; i++) {
            timetablesNames[i] = preferences.getString(Constants.SETTING_NAME_ADD_TIMETABLE_NAME + i, Constants.SETTING_NAME_ADD_TIMETABLE + i);
        }
        return timetablesNames;
    }

    private synchronized static void setTimetablesNames(Context context, String[] timetablesNames) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLES, Context.MODE_PRIVATE);
        int oldSize = preferences.getInt(Constants.SETTING_NAME_SIZE, 0);
        int size = timetablesNames.length;
        SharedPreferences.Editor editor = preferences
                .edit().putInt(Constants.SETTING_NAME_SIZE, size);
        for (int i = 0; i < oldSize; i++) {
            editor.remove(Constants.SETTING_NAME_ADD_TIMETABLE_NAME + i);
        }
        for (int i = 0; i < size; i++) {
            editor.putString(Constants.SETTING_NAME_ADD_TIMETABLE_NAME + i, timetablesNames[i]);
        }
        editor.apply();
    }

    static void addTimetablesName(Context context, String name) {
        String[] timetablesNames = getTimetablesNames(context);
        String[] newTimetablesNames;
        if (Arrays.contains(timetablesNames, name))
            throw new IllegalArgumentException("Timetable still exists");
        else newTimetablesNames = Arrays.add(timetablesNames, name);
        setTimetablesNames(context, newTimetablesNames);
    }

    static void removeTimetablesName(Context context, String name) {
        setTimetablesNames(context, cz.anty.utils.Arrays.remove(getTimetablesNames(context), name));
        context.getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLES, Context.MODE_PRIVATE)
                .edit().remove(Constants.SETTING_NAME_ADD_TIMETABLE + name)
                .remove(Constants.SETTING_NAME_ADD_TIMETABLE_SAVE_VERSION + name).apply();
    }
}
