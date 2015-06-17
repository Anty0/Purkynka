package cz.anty.utils.sas.mark;

import android.support.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by anty on 8.6.15.
 *
 * @author anty
 */
public class Lesson {

    private final String fullName, shortName;
    private final List<Mark> marks = new CopyOnWriteArrayList<>();
    private final DecimalFormat format = new DecimalFormat("#.###");

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
        return getShortName() + ": " + getMarks().length + " | " + format.format(getDiameter());
    }
}
