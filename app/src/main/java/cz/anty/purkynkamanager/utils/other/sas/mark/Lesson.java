package cz.anty.purkynkamanager.utils.other.sas.mark;

import android.content.Context;
import android.support.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
public class Lesson implements MultilineItem {

    public static final DecimalFormat FORMAT = new DecimalFormat("#.###");

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
    public CharSequence getTitle(Context context, int position) {
        return Utils.getFormattedText(getShortName() + ": <b>"
                + FORMAT.format(getDiameter()) + "</b>");
    }

    @Override
    public CharSequence getText(Context context, int position) {
        int len = getMarks().length;
        return context.getResources().getQuantityString(R.plurals
                .text_marks, len, len);
    }
}
