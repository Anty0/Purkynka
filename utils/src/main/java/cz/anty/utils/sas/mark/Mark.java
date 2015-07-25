package cz.anty.utils.sas.mark;

import java.util.Date;

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

    private Mark(Date date, String shortLesson, String longLesson, String valueToShow, double value, String type, int weight, String note, String teacher) {
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
    public String getTitle() {
        return toString();
    }

    @Override
    public String getText() {
        return getNote();
    }

    public static class Builder {

        private Date date;
        private String shortLesson, longLesson, valueToShow, type, note, teacher;
        private double value;
        private int weight;

        public Builder() {

        }

        public Builder(Date date, String shortLesson, String longLesson, String valueToShow, double value, String type, int weight, String note, String teacher) {
            this.date = date;
            this.shortLesson = shortLesson;
            this.longLesson = longLesson;
            this.valueToShow = valueToShow;
            this.value = value;
            this.type = type;
            this.weight = weight;
            this.note = note;
            this.teacher = teacher;
        }

        public Mark get() {
            return new Mark(date, shortLesson, longLesson, valueToShow, value, type, weight, note, teacher);
        }

        public Builder setDate(Date date) {
            this.date = date;
            return this;
        }

        public Builder setShortLesson(String shortLesson) {
            this.shortLesson = shortLesson;
            return this;
        }

        public Builder setLongLesson(String longLesson) {
            this.longLesson = longLesson;
            return this;
        }

        public Builder setValueToShow(String valueToShow) {
            this.valueToShow = valueToShow;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setNote(String note) {
            this.note = note;
            return this;
        }

        public Builder setTeacher(String teacher) {
            this.teacher = teacher;
            return this;
        }

        public Builder setValue(double value) {
            this.value = value;
            return this;
        }

        public Builder setWeight(int weight) {
            this.weight = weight;
            return this;
        }

    }
}
