package cz.anty.utils.timetable;

import android.content.Context;

/**
 * Created by anty on 13.6.15.
 *
 * @author anty
 */
public class Timetable {

    public static final String PARSE_CONST_DAY = ":;|TD|;:";
    public static final String PARSE_CONST_OBJECT = ":;|TO|;:";
    public static final String NAME_CONST = "TIMETABLE ";
    public static final String[] DAYS = new String[]{
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};
    public static final int MAX_LESSONS = 10;

    /*private static final int MONDAY = 0;
    private static final int TUESDAY = 1;
    private static final int WEDNESDAY = 2;
    private static final int THURSDAY = 3;
    private static final int FRIDAY = 4;*/
    private final Lesson[][] lessons;
    private String name;

    private Timetable(String name, Lesson[][] lessons) {
        this.name = name;
        this.lessons = lessons;
    }

    Timetable(Context context, String name) {
        this.name = name;
        lessons = new Lesson[DAYS.length][MAX_LESSONS];
        /*for (int i = 0; i < lessons.length; i++) {
            lessons[i] = new Lesson[MAX_LESSONS];
        }*/
        Timetables.addTimetablesName(context, name);
        apply(context);
    }

    static Timetable loadTimetable(Context context, String key) {
        String[] days = context.getSharedPreferences(NAME_CONST + key, Context.MODE_PRIVATE).getString(key, "").split(PARSE_CONST_DAY);
        Lesson[][] lessons = new Lesson[DAYS.length][MAX_LESSONS];
        for (int i = 0; i < days.length; i++) {
            lessons[i] = parseDay(days[i]);
        }
        return new Timetable(key, lessons);
    }

    private static Lesson[] parseDay(String day) {
        Lesson[] lessons = new Lesson[MAX_LESSONS];
        String[] dayData = day.split(PARSE_CONST_OBJECT);
        for (int i = 0; i < dayData.length; i++) {
            lessons[i] = Lesson.parse(dayData[i]);
        }
        return lessons;
    }

    public String getName() {
        return name;
    }

    public synchronized void setName(Context context, String className) {
        Timetables.removeTimetablesName(context, this.name);
        try {
            Timetables.addTimetablesName(context, className);
        } catch (Exception e) {
            Timetables.addTimetablesName(context, this.name);
        }
        this.name = className;
        apply(context);
    }

    public synchronized void setLesson(Context context, Lesson lesson, int day, int lessonIndex) {
        this.lessons[day][lessonIndex] = lesson;
        apply(context);
    }

    public synchronized Lesson getLesson(int day, int lessonIndex) {
        return lessons[day][lessonIndex];
    }

    public synchronized Lesson[] getDay(int day) {
        return lessons[day];
    }

    public synchronized Lesson[][] getFullWeek() {
        return lessons;
    }

    private synchronized void apply(Context context) {
        StringBuilder builder = new StringBuilder(dayToString(0));
        for (int i = 1; i < lessons.length; i++) {
            builder.append(PARSE_CONST_DAY).append(dayToString(i));
            //builder.append(Arrays.toString(lessons.get(i).toArray()));
        }

        context.getSharedPreferences(NAME_CONST + name, Context.MODE_PRIVATE).edit()
                .putString("TIMETABLE", builder.toString())
                .apply();
    }

    private synchronized String dayToString(int day) {
        Lesson[] lessonsDay = lessons[day];
        StringBuilder builder = new StringBuilder().append(lessonsDay[0] == null ? "null" : lessonsDay[0]);
        for (int i = 1; i < lessonsDay.length; i++) {
            builder.append(PARSE_CONST_OBJECT).append(lessonsDay[i] == null ? "null" : lessonsDay[i]);
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
