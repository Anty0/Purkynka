package cz.anty.utils.timetable;

import android.content.Context;

import cz.anty.utils.Constants;
import cz.anty.utils.R;
import cz.anty.utils.listItem.MultilineItem;

/**
 * Created by anty on 13.6.15.
 *
 * @author anty
 */
public class Timetable implements MultilineItem {

    public static final int[] DAYS_STRINGS_IDS = new int[]{R.string.text_monday,
            R.string.text_tuesday, R.string.text_wednesday,
            R.string.text_thursday, R.string.text_friday};
    /*public static final String[] START_TIMES = new String[]{
            "7:10", "8:00", "8:55", "10:00", "10:55", "11:50",
            "12:45", "13:40", "14:35", "15:30", "16:25"};*/
    //public static final SimpleDateFormat START_TIMES_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    public static final int[] START_TIMES_HOURS = new int[]{
            7, 8, 8, 10, 10, 11, 12, 13, 14, 15, 16};
    public static final int[] START_TIMES_MINUTES = new int[]{
            10, 0, 55, 0, 55, 50, 45, 40, 35, 30, 25};
    public static final int MAX_LESSONS = 11;
    private static final String PARSE_CONST_DAY = ":;TD;:";
    private static final String PARSE_CONST_OBJECT = ":;TO;:";
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
        lessons = new Lesson[DAYS_STRINGS_IDS.length][MAX_LESSONS];
        /*for (int i = 0; i < lessons.length; i++) {
            lessons[i] = new Lesson[MAX_LESSONS];
        }*/
        Timetables.addTimetablesName(context, name);
        apply();
    }

    static Timetable loadTimetable(Context context, String key) {
        String[] days = context.getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLES, Context.MODE_PRIVATE)
                .getString(Constants.SETTING_NAME_TIMETABLE + key, "").split(PARSE_CONST_DAY);
        Lesson[][] lessons = new Lesson[DAYS_STRINGS_IDS.length][MAX_LESSONS];
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

    public synchronized Lesson getNextLesson(int day, int lessonIndex) {
        int index = lessonIndex + 1;
        Lesson[] day1 = getDay(day);
        if (index < day1.length) return day1[index];

        int newDay = day + 1;
        day1 = getDay(newDay);
        if (newDay < day1.length) return day1[0];
        return getLesson(0, 0);
    }

    public synchronized Lesson[] getDay(int day) {
        return lessons[day];
    }

    public synchronized Lesson[][] getFullWeek() {
        return lessons;
    }

    private synchronized void apply() {
        StringBuilder builder = new StringBuilder(dayToString(0).replace(PARSE_CONST_DAY, "??????"));
        for (int i = 1; i < lessons.length; i++) {
            builder.append(PARSE_CONST_DAY).append(dayToString(i).replace(PARSE_CONST_DAY, "??????"));
            //builder.append(Arrays.toString(lessons.get(i).toArray()));
        }

        context.getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLES, Context.MODE_PRIVATE).edit()
                .putString(Constants.SETTING_NAME_TIMETABLE + name, builder.toString())
                .apply();
    }

    private synchronized String dayToString(int day) {
        Lesson[] lessonsDay = lessons[day];
        StringBuilder builder = new StringBuilder().append(lessonsDay[0] == null ? "null" : lessonsDay[0].toString().replace(PARSE_CONST_OBJECT, "??????"));
        for (int i = 1; i < lessonsDay.length; i++) {
            builder.append(PARSE_CONST_OBJECT).append(lessonsDay[i] == null ? "null" : lessonsDay[i].toString().replace(PARSE_CONST_OBJECT, "??????"));
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getTitle(Context context, int position) {
        return toString();
    }

    @Override
    public String getText(Context context, int position) {
        return null;
    }

    @Override
    public Integer getLayoutResourceId(Context context, int position) {
        return null;
    }
}
