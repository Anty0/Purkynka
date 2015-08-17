package cz.anty.utils.sas.mark;

import android.content.Context;

import java.util.Date;

import cz.anty.utils.Constants;
import cz.anty.utils.R;
import cz.anty.utils.listItem.MultilineItem;
import cz.anty.utils.sas.SASConnector;

/**
 * Created by anty on 8.6.15.
 *
 * @author anty
 */
public class Mark implements MultilineItem {

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
        return SASConnector.DATE_FORMAT.format(getDate());
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

    @Override
    public String toString() {
        return getDateAsString()
                + " " + getShortLesson()
                + " " + getValueToShow()
                + " x " + getWeight();
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.text_mark_with_weight)
                .replace(Constants.STRINGS_CONST_NAME, getShortLesson())
                .replace(Constants.STRINGS_CONST_NUMBER, getValueToShow())
                .replace(Constants.STRINGS_CONST_WEIGHT, Integer.toString(getWeight()));
    }

    @Override
    public String getText(Context context) {
        return getDateAsString() + " " + getNote();
    }

    @Override
    public Integer getLayoutResourceId(Context context) {
        return null;
    }
}
