package cz.anty.utils.timetable;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by anty on 13.6.15.
 *
 * @author anty
 */
public class Timetables {

    private static final String SETTINGS_NAME_CONST = "Timetables";

    synchronized static String[] getTimetablesNames(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SETTINGS_NAME_CONST, Context.MODE_PRIVATE);
        int size = preferences.getInt("SIZE", 0);
        String[] timetablesNames = new String[size];
        for (Integer i = 0; i < size; i++) {
            timetablesNames[i] = preferences.getString(i.toString(), Timetable.NAME_CONST + i);
        }
        return timetablesNames;
    }

    private synchronized static void setTimetablesNames(Context context, String[] timetablesNames) {
        int size = timetablesNames.length;
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTINGS_NAME_CONST, Context.MODE_PRIVATE)
                .edit().clear().putInt("SIZE", size);
        for (Integer i = 0; i < size; i++) {
            editor.putString(i.toString(), timetablesNames[i]);
        }
        editor.apply();
    }

    static void addTimetablesName(Context context, String name) {
        String[] timetablesNames = getTimetablesNames(context);
        String[] newTimetablesNames;
        if (cz.anty.utils.Arrays.contains(timetablesNames, name))
            throw new IllegalArgumentException("Timetable still exists");
        else newTimetablesNames = cz.anty.utils.Arrays.add(timetablesNames, name);
        setTimetablesNames(context, newTimetablesNames);
    }

    static void removeTimetablesName(Context context, String name) {
        setTimetablesNames(context, cz.anty.utils.Arrays.remove(getTimetablesNames(context), name));
        context.getSharedPreferences(Timetable.SETTINGS_NAME_CONST, Context.MODE_PRIVATE)
                .edit().remove(Timetable.NAME_CONST + name).apply();
    }
}
