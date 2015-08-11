package cz.anty.utils.sas.mark;

import android.content.Context;
import android.support.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cz.anty.utils.Constants;
import cz.anty.utils.R;
import cz.anty.utils.listItem.MultilineItem;

/**
 * Created by anty on 8.6.15.
 *
 * @author anty
 */
public class Lesson implements MultilineItem {

    private static final DecimalFormat FORMAT = new DecimalFormat("#.###");
    private final String fullName, shortName;
    private final List<Mark> marks = new CopyOnWriteArrayList<>();

    public Lesson(@NonNull String fullName, @NonNull String shortName) {
        this.fullName = fullName;
        this.shortName = shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public boolean addMark(Mark mark) {
        if (getShortName().equals(mark.getShortLesson())) {
            marks.add(mark);
            return true;
        }
        return false;
    }

    public void removeMark(Mark mark) {
        marks.remove(mark);
    }

    public Mark[] getMarks() {
        return marks.toArray(new Mark[marks.size()]);
    }

    public double getDiameter() {
        Mark[] marks = getMarks();
        double tempMark = 0d;
        int tempWeight = 0;
        for (Mark mark : marks) {
            if (mark.getValue() == 0d) continue;
            tempMark += mark.getValue() * (double) mark.getWeight();
            tempWeight += mark.getWeight();
        }
        return tempMark / (double) tempWeight;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) || o instanceof Lesson &&
                getFullName().equals(((Lesson) o).getFullName()) &&
                getShortName().equals(((Lesson) o).getShortName());
    }

    @Override
    public String toString() {
        return getShortName() + ": " + getMarks().length
                + " | " + FORMAT.format(getDiameter());
    }

    @Override
    public String getTitle(Context context) {
        return getShortName() + ": " + FORMAT.format(getDiameter());
    }

    @Override
    public String getText(Context context) {
        return context.getString(R.string.text_marks)
                .replace(Constants.STRINGS_CONST_NUMBER
                        , Integer.toString(getMarks().length));
    }

    @Override
    public Integer getLayoutResourceId(Context context) {
        return null;
    }
}
