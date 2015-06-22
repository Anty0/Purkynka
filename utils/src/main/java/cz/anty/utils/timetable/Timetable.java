package cz.anty.utils.timetable;

import android.content.Context;

/**
 * Created by anty on 13.6.15.
 *
 * @author anty
 */
public class Timetable {

    public static final String SETTINGS_NAME_CONST = "TimetablesData";
    public static final String PARSE_CONST_DAY = ":;TD;:";
    public static final String PARSE_CONST_OBJECT = ":;TO;:";
    public static final String NAME_CONST = "TIMETABLE ";
    public static final String[] DAYS = new String[]{
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};
    /*public static final String[] START_TIMES = new String[]{
            "7:10", "8:00", "8:55", "10:00", "10:55", "11:50",
            "12:45", "13:40", "14:35", "15:30", "16:25"};*/
    //public static final SimpleDateFormat START_TIMES_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    public static final int[] START_TIMES_HOURS = new int[]{
            7, 8, 8, 10, 10, 11, 12, 13, 14, 15, 16};
    public static final int[] START_TIMES_MINUTES = new int[]{
            10, 0, 55, 0, 55, 50, 45, 40, 35, 30, 25};
    public static final int MAX_LESSONS = 11;

    /*private static final int MONDAY = 0;
    private static final int TUESDAY = 1;
    private static final int WEDNESDAY = 2;
    private static final int THURSDAY = 3;
    private static final int FRIDAY = 4;*/
    private final Lesson[][] lessons;
    private final Context context;
    private String name;

    private Timetable(Context context, String name, Lesson[][] lessons) {
        this.context = context;
        this.name = name;
        this.lessons = lessons;
    }

    Timetable(Context context, String name) {
        this.context = context;
        this.name = name;
        lessons = new Lesson[DAYS.length][MAX_LESSONS];
        /*for (int i = 0; i < lessons.length; i++) {
            lessons[i] = new Lesson[MAX_LESSONS];
        }*/
        Timetables.addTimetablesName(context, name);
        apply();
    }

    static Timetable loadTimetable(Context context, String key) {
        String[] days = context.getSharedPreferences(SETTINGS_NAME_CONST, Context.MODE_PRIVATE).getString(NAME_CONST + key, "").split(PARSE_CONST_DAY);
        Lesson[][] lessons = new Lesson[DAYS.length][MAX_LESSONS];
        for (int i = 0; i < days.length; i++) {
            lessons[i] = parseDay(days[i]);
        }
        return new Timetable(context, key, lessons);
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

    public synchronized void setName(String className) {
        Timetables.removeTimetablesName(context, this.name);
        try {
            Timetables.addTimetablesName(context, className);
        } catch (Exception e) {
            Timetables.addTimetablesName(context, this.name);
        }
        this.name = className;
        apply();
    }

    public synchronized void setLesson(Lesson lesson, int day, int lessonIndex) {
        this.lessons[day][lessonIndex] = lesson;
        apply();
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

    private synchronized void apply() {
        StringBuilder builder = new StringBuilder(dayToString(0).replaceAll(PARSE_CONST_DAY, "??????"));
        for (int i = 1; i < lessons.length; i++) {
            builder.append(PARSE_CONST_DAY).append(dayToString(i).replaceAll(PARSE_CONST_DAY, "??????"));
            //builder.append(Arrays.toString(lessons.get(i).toArray()));
        }

        context.getSharedPreferences(SETTINGS_NAME_CONST, Context.MODE_PRIVATE).edit()
                .putString(NAME_CONST + name, builder.toString())
                .apply();
    }

    private synchronized String dayToString(int day) {
        Lesson[] lessonsDay = lessons[day];
        StringBuilder builder = new StringBuilder().append(lessonsDay[0] == null ? "null" : lessonsDay[0].toString().replaceAll(PARSE_CONST_OBJECT, "??????"));
        for (int i = 1; i < lessonsDay.length; i++) {
            builder.append(PARSE_CONST_OBJECT).append(lessonsDay[i] == null ? "null" : lessonsDay[i].toString().replaceAll(PARSE_CONST_OBJECT, "??????"));
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
