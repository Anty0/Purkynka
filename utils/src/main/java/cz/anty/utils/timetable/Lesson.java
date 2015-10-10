package cz.anty.utils.timetable;

import android.content.Context;

import cz.anty.utils.R;
import cz.anty.utils.list.listView.MultilineItem;

/**
 * Created by anty on 13.6.15.
 *
 * @author anty
 */
public class Lesson implements MultilineItem {

    //private static final String PARSE_CHARS = ":;L;:";
    private final String name, shortName, classString, teacher;

    public Lesson(String name, String shortName, String classString, String teacher) {
        this.name = name == null ? "" : name;
        this.shortName = shortName == null ? "" : shortName;
        this.classString = classString == null ? "" : classString;
        this.teacher = teacher == null ? "" : teacher;
    }

    /*public static Lesson parse(String toParse) {
        if ("".equals(toParse) || "null".equals(toParse)) return null;
        String[] toParseData = toParse.split(PARSE_CHARS);

        return new Lesson(toParseData[0], toParseData[1], toParseData[2], toParseData[3]);
    }*/

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getClassString() {
        return classString;
    }

    public String getTeacher() {
        return teacher;
    }

    /*@Override
    public String toString() {
        return getName().replace(PARSE_CHARS, "?????") + PARSE_CHARS
                + getShortName().replace(PARSE_CHARS, "?????") + PARSE_CHARS
                + getClassString().replace(PARSE_CHARS, "?????") + PARSE_CHARS
                + getTeacher().replace(PARSE_CHARS, "?????");
    }*/

    @Override
    public CharSequence getTitle(Context context, int position) {
        if (position == NO_POSITION)
            return String.format(context.getString(R.string.list_item_text_lesson),
                    0, getShortName(), getClassString()).substring(3);

        return String.format(context.getString(R.string.list_item_text_lesson),
                position, getShortName(), getClassString());
    }

    @Override
    public CharSequence getText(Context context, int position) {
        int minutes = Timetable.START_TIMES_MINUTES[position];
        return Timetable.START_TIMES_HOURS[position] +
                ":" + (minutes < 10 ? "0" + minutes : minutes)
                + " " + getTeacher() + " " + getName();
    }
}
