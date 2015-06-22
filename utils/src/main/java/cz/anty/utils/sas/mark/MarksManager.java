package cz.anty.utils.sas.mark;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cz.anty.utils.sas.SASConnector;

/**
 * Created by anty on 10.6.15.
 *
 * @author anty
 */
public class MarksManager {

    private static final int MARKS_SAVE_VERSION = 5;
    private static final String SPLIT_VALUE = ":;M;:";

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

    public synchronized void add(Mark mark, Semester semester) {
        marks.get(semester.getIndexValue()).add(mark);
    }

    public void addAll(List<Mark> marks, Semester semester) {
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

        SharedPreferences preferences = context.getSharedPreferences("MarksData", Context.MODE_PRIVATE);
        if (preferences.getInt("MARKS_SAVE_VERSION", 0) != MARKS_SAVE_VERSION) {
            apply(Semester.FIRST);
            apply(Semester.SECOND);
            context.getSharedPreferences("MarksData", Context.MODE_PRIVATE).edit()
                    .putInt("MARKS_SAVE_VERSION", MARKS_SAVE_VERSION)
                    .apply();
            return;
        }

        load(Semester.FIRST);
        load(Semester.SECOND);
    }

    private synchronized void load(Semester semester) {
        SharedPreferences preferences = context.getSharedPreferences("MarksData", Context.MODE_PRIVATE);
        String[] marksData;
        /*if (Build.VERSION.SDK_INT >= 11) {
            Set<String> marks = preferences.getStringSet("MARKS" + semester.getValue(), new HashSet<String>());
            marksData = marks.toArray(new String[marks.size()]);
        } else {*/
        marksData = preferences.getString("MARKS" + semester.getValue(), "").split("\n");
        if (marksData[0].equals("")) return;
        //}
        for (String string : marksData) {
            add(parseMark(string), semester);
        }
        lessons.set(semester.getIndexValue(), Marks.toLessons(this.marks.get(semester.getIndexValue())));
    }

    public synchronized void apply(Semester semester) {
        Mark[] marks = get(semester);
        /*if (Build.VERSION.SDK_INT >= 11) {
            Set<String> masksSet = new HashSet<>();
            for (Mark mark : marks) {
                masksSet.add(markToString(mark));
            }
            context.getSharedPreferences("MarksData", Context.MODE_PRIVATE).edit()
                    .putStringSet("MARKS" + semester.getValue(), masksSet)
                    .apply();
        } else {*/
        StringBuilder builder = new StringBuilder();
        if (marks.length > 0) {
            builder.append(markToString(marks[0]));
            for (int i = 1; i < marks.length; i++) {
                builder.append("\n").append(markToString(marks[i]).replaceAll("\n", "?"));
            }
        }

        context.getSharedPreferences("MarksData", Context.MODE_PRIVATE).edit()
                .putString("MARKS" + semester.getValue(), builder.toString())
                .apply();
        //}
        lessons.set(semester.getIndexValue(), Marks.toLessons(this.marks.get(semester.getIndexValue())));
    }

    private String markToString(Mark mark) {
        return mark.getDateAsString().replaceAll(SPLIT_VALUE, "?????") + SPLIT_VALUE
                + mark.getShortLesson().replaceAll(SPLIT_VALUE, "?????") + SPLIT_VALUE
                + mark.getLongLesson().replaceAll(SPLIT_VALUE, "?????") + SPLIT_VALUE
                + mark.getValueToShow().replaceAll(SPLIT_VALUE, "?????") + SPLIT_VALUE
                + mark.getValue() + SPLIT_VALUE
                + mark.getType().replaceAll(SPLIT_VALUE, "?????") + SPLIT_VALUE
                + mark.getWeight() + SPLIT_VALUE
                + mark.getNote().replaceAll(SPLIT_VALUE, "?????") + SPLIT_VALUE
                + mark.getTeacher().replaceAll(SPLIT_VALUE, "?????");
    }

    private Mark parseMark(String string) {
        String[] markData = string.split(SPLIT_VALUE);
        Mark.Builder builder = new Mark.Builder()
                .setShortLesson(markData[1])
                .setLongLesson(markData[2])
                .setValueToShow(markData[3])
                .setValue(Double.parseDouble(markData[4]))
                .setType(markData[5])
                .setWeight(Integer.parseInt(markData[6]))
                .setNote(markData[7])
                .setTeacher(markData[8]);
        try {
            builder.setDate(SASConnector.DATE_FORMAT.parse(markData[0]));
        } catch (ParseException e) {
            builder.setDate(new Date(System.currentTimeMillis()));
            //throw new IllegalArgumentException("Parameter error: invalid date " + markInfo, e);
        }

        return builder.get();
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
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTime(new Date(System.currentTimeMillis()));
                    int month = calendar.get(Calendar.MONTH);
                    return month > 1 && month < 9 ? 2 : 1;
            }
        }

        public Integer getIndexValue() {
            switch (this) {
                case FIRST:
                    return 0;
                case SECOND:
                    return 1;
                default:
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTime(new Date(System.currentTimeMillis()));
                    int month = calendar.get(Calendar.MONTH);
                    return month > 1 && month < 9 ? 1 : 0;
            }
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
