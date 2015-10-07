package cz.anty.utils.attendance.man;

import android.content.Context;

import java.util.Date;
import java.util.Locale;

import cz.anty.utils.R;
import cz.anty.utils.attendance.AttendanceConnector;
import cz.anty.utils.list.listView.MultilineItem;

/**
 * Created by anty on 13.6.15.
 *
 * @author anty
 */
public class Man implements MultilineItem {

    private final String name, classString;
    private final Date lastEnter;
    private final IsInSchoolState inSchool;

    Man(String name, String classString, Date lastEnter, IsInSchoolState inSchool) {
        this.name = name == null ? "" : name;
        this.classString = classString == null ? "" : classString;
        this.lastEnter = lastEnter == null ? new Date(System.currentTimeMillis()) : lastEnter;
        this.inSchool = inSchool;
    }

    public String getName() {
        return name;
    }

    public String getClassString() {
        return classString;
    }

    public Date getLastEnter() {
        return lastEnter;
    }

    public String getLastEnterAsString() {
        return AttendanceConnector.DATE_FORMAT.format(getLastEnter());
    }

    public IsInSchoolState isInSchool() {
        return inSchool;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) || o instanceof Man &&
                ((Man) o).getName().equals(getName()) &&
                ((Man) o).getClassString().equals(getClassString());
    }

    @Override
    public String toString() {
        return getName() + " | "
                + getClassString() + " | "
                + getLastEnterAsString() + " | "
                + isInSchool();
    }

    @Override
    public CharSequence getTitle(Context context, int position) {
        String classString = getClassString();
        IsInSchoolState schoolState = isInSchool();
        return String.format((IsInSchoolState.UNAVAILABLE.equals(schoolState) ? "%1$s" :
                (IsInSchoolState.IN_SCHOOL.equals(schoolState) ? context.getString(R.string.text_is_in_school)
                        : context.getString(R.string.text_isnt_in_school))), getName() +
                (classString.length() > 4 ? "" : " " + classString));
    }

    @Override
    public CharSequence getText(Context context, int position) {
        return getLastEnterAsString();
    }

    public enum IsInSchoolState {
        IN_SCHOOL, NOT_IN_SCHOOL, UNAVAILABLE;

        public static IsInSchoolState parseIsInSchoolState(String toParse) {
            toParse = toParse.toLowerCase(Locale.getDefault());
            if (toParse.equals("nezjištěn"))
                return UNAVAILABLE;
            if (toParse.contains("ne"))
                return NOT_IN_SCHOOL;
            return IN_SCHOOL;
        }

        @Override
        public String toString() {
            switch (this) {
                case IN_SCHOOL:
                    return "j";
                case NOT_IN_SCHOOL:
                    return "ne";
                case UNAVAILABLE:
                    return "nezjištěn";
                default:
                    return UNAVAILABLE.toString();
            }
        }
    }
}
