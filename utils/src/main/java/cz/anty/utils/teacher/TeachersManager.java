package cz.anty.utils.teacher;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.anty.utils.Constants;

/**
 * Created by anty on 22.6.15.
 *
 * @author anty
 */
public class TeachersManager {

    private static final int TEACHERS_SAVE_VERSION = 1;
    private static final String SPLIT_VALUE = ":;TE;:";

    private final Context context;
    private final List<Teacher> teachers = new ArrayList<>();
    private long lastRefresh;

    public TeachersManager(@NonNull Context context) {
        this.context = context;
        load();
    }

    public synchronized Teacher[] get() {
        return teachers.toArray(new Teacher[teachers.size()]);
    }

    private synchronized void load() {
        teachers.clear();

        SharedPreferences preferences = context.getSharedPreferences(Constants.SETTINGS_NAME_TEACHERS, Context.MODE_PRIVATE);
        if (preferences.getInt(Constants.SETTING_NAME_TEACHERS_SAVE_VERSION, 0) != TEACHERS_SAVE_VERSION) {
            context.getSharedPreferences(Constants.SETTINGS_NAME_TEACHERS, Context.MODE_PRIVATE).edit()
                    .putInt(Constants.SETTING_NAME_TEACHERS_SAVE_VERSION, TEACHERS_SAVE_VERSION)
                    .apply();
            refresh();
            return;
        }

        String[] teachersData;
        teachersData = preferences.getString(Constants.SETTING_NAME_TEACHERS, "").split("\n");
        if (teachersData[0].equals("")) return;
        for (String string : teachersData) {
            teachers.add(parseTeacher(string));
        }

        if (preferences.getLong(Constants.SETTING_NAME_LAST_REFRESH, lastRefresh) - lastRefresh > 1000 * 60 * 60 * 24) {
            refresh();
        }
    }

    public synchronized void refresh() {
        Teacher[] teachers;
        try {
            List<Teacher> teacherList = TeachersListConnector.getTeachers();
            teachers = teacherList.toArray(new Teacher[teacherList.size()]);
            lastRefresh = System.currentTimeMillis();
        } catch (IOException e) {
            teachers = get();
        }

        StringBuilder builder = new StringBuilder();
        if (teachers.length > 0) {
            builder.append(teacherToString(teachers[0]));
            for (int i = 1; i < teachers.length; i++) {
                builder.append("\n").append(teacherToString(teachers[i]).replaceAll("\n", "?"));
            }
        }

        context.getSharedPreferences(Constants.SETTINGS_NAME_TEACHERS, Context.MODE_PRIVATE).edit()
                .putString(Constants.SETTING_NAME_TEACHERS, builder.toString())
                .putLong(Constants.SETTING_NAME_LAST_REFRESH, lastRefresh)
                .apply();
        load();
    }

    private String teacherToString(Teacher teacher) {
        return teacher.getName().replaceAll(SPLIT_VALUE, "??????") + SPLIT_VALUE
                + teacher.getShortcut().replaceAll(SPLIT_VALUE, "??????") + SPLIT_VALUE
                + teacher.getPhoneNumber().replaceAll(SPLIT_VALUE, "??????") + SPLIT_VALUE
                + teacher.getEmail().replaceAll(SPLIT_VALUE, "??????");
    }

    private Teacher parseTeacher(String string) {
        String[] teacherData = string.split(SPLIT_VALUE);
        return new Teacher(teacherData[0], teacherData[1], teacherData[2], teacherData[3]);
    }
}
