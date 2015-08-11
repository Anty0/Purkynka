package cz.anty.utils.attendance.man;

import android.content.Context;

import java.util.Date;

import cz.anty.utils.Constants;
import cz.anty.utils.R;
import cz.anty.utils.attendance.AttendanceConnector;
import cz.anty.utils.listItem.MultilineItem;

/**
 * Created by anty on 13.6.15.
 *
 * @author anty
 */
public class Man implements MultilineItem {

    private final String name, classString;
    private final Date lastEnter;
    private final boolean inSchool;

    private Man(String name, String classString, Date lastEnter, boolean inSchool) {
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

    public boolean isInSchool() {
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
    public String getTitle(Context context) {
        String classString = getClassString();
        return (isInSchool() ? context.getString(R.string.text_is_in_school)
                : context.getString(R.string.text_isnt_in_school))
                .replace(Constants.STRINGS_CONST_NAME, getName() +
                        (classString.length() > 4 ? "" : " " + classString));
    }

    @Override
    public String getText(Context context) {
        return getLastEnterAsString();
    }

    @Override
    public Integer getLayoutResourceId(Context context) {
        return null;
    }

    public static class Builder {

        private String name, classString;
        private Date lastEnter;
        private boolean inSchool;

        public Builder(String name, String classString, Date lastEnter, boolean inSchool) {
            this.name = name;
            this.classString = classString;
            this.lastEnter = lastEnter;
            this.inSchool = inSchool;
        }

        public Builder() {

        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setClassString(String classString) {
            this.classString = classString;
            return this;
        }

        public Builder setLastEnter(Date lastEnter) {
            this.lastEnter = lastEnter;
            return this;
        }

        public Builder setInSchool(boolean inSchool) {
            this.inSchool = inSchool;
            return this;
        }

        public Man get() {
            return new Man(name, classString, lastEnter, inSchool);
        }
    }

}
