package cz.anty.purkynkamanager.utils.other.sas.mark;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.anty.purkynkamanager.utils.other.Constants;

/**
 * Created by anty on 10.6.15.
 *
 * @author anty
 */
public class MarksManager {

    private static final int MARKS_SAVE_VERSION = 6;
    //private static final String SPLIT_VALUE = ":;M;:";

    private final Context context;
    private final List<List<Mark>> marks = new ArrayList<>(2);
    private final List<List<Lesson>> lessons = new ArrayList<>(2);

    {
        marks.add(new ArrayList<Mark>());
        marks.add(new ArrayList<Mark>());
    }

    {
        lessons.add(new ArrayList<Lesson>());
        lessons.add(new ArrayList<Lesson>());
    }

    public MarksManager(@NonNull Context context) {
        this.context = context;
        load();
    }

    private static String marksToString(Mark... marks) {
        return new Gson().toJson(marks);
        /*StringBuilder builder = new StringBuilder();
        if (marks.length > 0) {
            builder.append(markToString(marks[0]));
            for (int i = 1; i < marks.length; i++) {
                builder.append("\n").append(markToString(marks[i]).replace('\n', '?'));
            }
        }
        return builder.toString();*/
    }

    /*private static String markToString(Mark mark) {
        return mark.getDateAsString().replace(SPLIT_VALUE, "?????") + SPLIT_VALUE
                + mark.getShortLesson().replace(SPLIT_VALUE, "?????") + SPLIT_VALUE
                + mark.getLongLesson().replace(SPLIT_VALUE, "?????") + SPLIT_VALUE
                + mark.getValueToShow().replace(SPLIT_VALUE, "?????") + SPLIT_VALUE
                + mark.getValue() + SPLIT_VALUE
                + mark.getType().replace(SPLIT_VALUE, "?????") + SPLIT_VALUE
                + mark.getWeight() + SPLIT_VALUE
                + mark.getNote().replace(SPLIT_VALUE, "?????") + SPLIT_VALUE
                + mark.getTeacher().replace(SPLIT_VALUE, "?????");
    }*/

    public static List<Mark> parseMarks(String toParse) {
        if (toParse.equals("")) return new ArrayList<>();
        return Arrays.asList(new Gson().fromJson(toParse, Mark[].class));

        /*String[] marksData = toParse.split("\n");
        List<Mark> marks = new ArrayList<>();
        if (marksData[0].equals("")) return marks;
        for (String string : marksData) {
            marks.add(parseMark(string));
        }
        return marks;*/
    }

    /*private static Mark parseMark(String string) {
        String[] markData = string.split(SPLIT_VALUE);
        Date date;
        try {
            date = Mark.DATE_FORMAT.parse(markData[0]);
        } catch (ParseException e) {
            date = new Date(System.currentTimeMillis());
        }
        return new Mark(date,
                markData[1], markData[2], markData[3], Double.parseDouble(markData[4]),
                markData[5], Integer.parseInt(markData[6]), markData[7], markData[8]);
    }*/

    public synchronized void add(Mark mark, Semester semester) {
        marks.get(semester.getIndexValue()).add(mark);
    }

    public synchronized void addAll(List<Mark> marks, Semester semester) {
        this.marks.get(semester.getIndexValue()).addAll(marks);
    }

    public synchronized void remove(Mark mark, Semester semester) {
        marks.get(semester.getIndexValue()).remove(mark);
    }

    public synchronized void clear(Semester semester) {
        marks.get(semester.getIndexValue()).clear();
    }

    public synchronized Mark[] get(Semester semester) {
        List<Mark> marks = this.marks.get(semester.getIndexValue());
        return marks.toArray(new Mark[marks.size()]);
    }

    public synchronized Lesson[] getAsLessons(Semester semester) {
        List<Lesson> lessons = this.lessons.get(semester.getIndexValue());
        return lessons.toArray(new Lesson[lessons.size()]);
    }

    private synchronized void load() {
        clear(Semester.FIRST);
        clear(Semester.SECOND);

        SharedPreferences preferences = context.getSharedPreferences(Constants.SETTINGS_NAME_MARKS, Context.MODE_PRIVATE);
        if (preferences.getInt(Constants.SETTING_NAME_MARKS_SAVE_VERSION, -1) != MARKS_SAVE_VERSION) {
            apply(Semester.FIRST);
            apply(Semester.SECOND);
            preferences.edit()
                    .putInt(Constants.SETTING_NAME_MARKS_SAVE_VERSION, MARKS_SAVE_VERSION)
                    .apply();
            return;
        }

        load(Semester.FIRST);
        load(Semester.SECOND);
    }

    private synchronized void load(Semester semester) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SETTINGS_NAME_MARKS, Context.MODE_PRIVATE);
        /*if (Build.VERSION.SDK_INT >= 11) {
            Set<String> marks = preferences.getStringSet("MARKS" + semester.getValue(), new HashSet<String>());
            marksData = marks.toArray(new String[marks.size()]);
        } else {*/
        String toParse = preferences.getString(Constants.SETTING_NAME_ADD_MARKS + semester.getValue(), "");
        //}

        addAll(parseMarks(toParse), semester);
        lessons.set(semester.getIndexValue(), Marks.toLessons(this.marks.get(semester.getIndexValue())));
    }

    public synchronized void apply(Semester semester) {
        //Mark[] marks = get(semester);
        /*if (Build.VERSION.SDK_INT >= 11) {
            Set<String> masksSet = new HashSet<>();
            for (Mark mark : marks) {
                masksSet.add(markToString(mark));
            }
            context.getSharedPreferences("MarksData", Context.MODE_PRIVATE).edit()
                    .putStringSet("MARKS" + semester.getValue(), masksSet)
                    .apply();
        } else {*/
        /*StringBuilder builder = new StringBuilder();
        if (marks.length > 0) {
            builder.append(markToString(marks[0]));
            for (int i = 1; i < marks.length; i++) {
                builder.append("\n").append(markToString(marks[i]).replaceAll("\n", "?"));
            }
        }*/

        context.getSharedPreferences(Constants.SETTINGS_NAME_MARKS, Context.MODE_PRIVATE).edit()
                .putString(Constants.SETTING_NAME_ADD_MARKS + semester.getValue(), marksToString(get(semester)))
                .apply();
        //}
        lessons.set(semester.getIndexValue(), Marks.toLessons(this.marks.get(semester.getIndexValue())));
    }

    @Override
    public String toString() {
        return toString(Semester.AUTO);
    }

    public String toString(Semester semester) {
        return marksToString(get(semester));
    }

    public enum Semester {
        FIRST, SECOND, AUTO;

        public Semester getStableSemester() {
            switch (getValue()) {
                case 2:
                    return SECOND;
                default:
                    return FIRST;
            }
        }

        public Integer getValue() {
            switch (this) {
                case FIRST:
                    return 1;
                case SECOND:
                    return 2;
                default:
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date(System.currentTimeMillis()));
                    int month = calendar.get(Calendar.MONTH);
                    return month > Calendar.JANUARY && month < Calendar.JULY ? 2 : 1;
            }
        }

        public Integer getIndexValue() {
            return getValue() - 1;
        }

        public Semester reverse() {
            switch (getValue()) {
                case 2:
                    return FIRST;
                default:
                    return SECOND;
            }
        }
    }
}
