package cz.anty.purkynkamanager.utils.other.sas.mark;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;
import proguard.annotation.Keep;
import proguard.annotation.KeepClassMemberNames;
import proguard.annotation.KeepClassMembers;
import proguard.annotation.KeepName;

/**
 * Created by anty on 8.6.15.
 *
 * @author anty
 */
@Keep
@KeepName
@KeepClassMembers
@KeepClassMemberNames
public class Mark implements MultilineItem {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    private final Date date;
    private final String shortLesson, longLesson, valueToShow, type, note, teacher;
    private final double value;
    private final int weight;
    private double valueModification = 0d;

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
        return valueModification == 0d ? valueToShow :
                String.valueOf((int) (value + valueModification));
    }

    public String getDefaultValueToShow() {
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
        return value + valueModification;
    }

    public double getDefaultValue() {
        return value;
    }

    public int getWeight() {
        return weight;
    }

    public double getValueModification() {
        return valueModification;
    }

    public void setValueModification(double valueModification) {
        this.valueModification = valueModification;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) || o instanceof Mark
                && ((Mark) o).getDate().equals(getDate())
                && ((Mark) o).getShortLesson().equals(getShortLesson())
                && ((Mark) o).getLongLesson().equals(getLongLesson())
                && ((Mark) o).getDefaultValueToShow().equals(getDefaultValueToShow())
                && ((Mark) o).getType().equals(getType())
                && ((Mark) o).getNote().equals(getNote())
                && ((Mark) o).getTeacher().equals(getTeacher())
                && ((Mark) o).getDefaultValue() == getDefaultValue()
                && ((Mark) o).getWeight() == getWeight();
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
        return Utils.getFormattedText(context, R.string.text_mark_with_weight,
                getShortLesson(), getValueToShow(), getWeight());
    }

    @Override
    public CharSequence getText(Context context, int position) {
        return getDateAsString() + " " + getNote();
    }
}
