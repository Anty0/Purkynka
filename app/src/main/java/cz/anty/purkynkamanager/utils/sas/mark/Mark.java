package cz.anty.purkynkamanager.utils.sas.mark;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.list.listView.MultilineItem;

/**
 * Created by anty on 8.6.15.
 *
 * @author anty
 */
public class Mark implements MultilineItem {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    private final Date date;
    private final String shortLesson, longLesson, valueToShow, type, note, teacher;
    private final double value;
    private final int weight;

    Mark(Date date, String shortLesson, String longLesson, String valueToShow, double value, String type, int weight, String note, String teacher) {
        this.date = date == null ? new Date(System.currentTimeMillis()) : date;
        this.shortLesson = shortLesson == null ? "NONE" : shortLesson;
        this.longLesson = longLesson == null ? "" : longLesson;
        this.valueToShow = valueToShow == null ? "-" : valueToShow;
        this.value = value;
        this.type = type == null ? "" : type;
        this.weight = weight;
        this.note = note == null ? "" : note;
        this.teacher = teacher == null ? "" : teacher;
    }

    public Date getDate() {
        return date;
    }

    public String getDateAsString() {
        return DATE_FORMAT.format(getDate());
    }

    public String getShortLesson() {
        return shortLesson;
    }

    public String getLongLesson() {
        return longLesson;
    }

    public String getValueToShow() {
        return valueToShow;
    }

    public String getType() {
        return type;
    }

    public String getNote() {
        return note;
    }

    public String getTeacher() {
        return teacher;
    }

    public double getValue() {
        return value;
    }

    public int getWeight() {
        return weight;
    }

    /*@Override
    public boolean equals(Object o) {
        return super.equals(o) || o instanceof Mark && getDate().equals(((Mark) o).getDate())
                && getShortLesson().equals(((Mark) o).getShortLesson());
    }*/

    @Override
    public String toString() {
        return getDateAsString()
                + " " + getShortLesson()
                + " " + getValueToShow()
                + " x " + getWeight();
    }

    @Override
    public CharSequence getTitle(Context context, int position) {
        return String.format(context.getString(R.string.text_mark_with_weight),
                getShortLesson(), getValueToShow(), getWeight());
    }

    @Override
    public CharSequence getText(Context context, int position) {
        return getDateAsString() + " " + getNote();
    }
}
