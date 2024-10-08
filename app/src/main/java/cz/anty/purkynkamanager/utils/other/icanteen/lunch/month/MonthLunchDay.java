package cz.anty.purkynkamanager.utils.other.icanteen.lunch.month;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;
import proguard.annotation.Keep;
import proguard.annotation.KeepClassMemberNames;
import proguard.annotation.KeepClassMembers;
import proguard.annotation.KeepName;

/**
 * Created by anty on 17.8.15.
 *
 * @author anty
 */
@Keep
@KeepName
@KeepClassMembers
@KeepClassMemberNames
public class MonthLunchDay implements MultilineItem {

    public static final SimpleDateFormat DATE_PARSE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public static final SimpleDateFormat DATE_SHOW_FORMAT = new SimpleDateFormat("dd. MM. yyyy", Locale.getDefault());

    private final Date date;
    private final MonthLunch[] lunches;
    private boolean disabled = false;

    public MonthLunchDay(Date date, MonthLunch[] monthLunches) {
        this.date = date;
        lunches = monthLunches;
    }

    public Date getDate() {
        return date;
    }

    public MonthLunch[] getLunches() {
        return lunches;
    }

    public int getOrderedLunchIndex() {
        MonthLunch[] lunches1 = getLunches();
        for (int i = 0, lunches1Length = lunches1.length; i < lunches1Length; i++) {
            MonthLunch.State state = lunches1[i].getState();
            if (state.equals(MonthLunch.State.ORDERED) ||
                    state.equals(MonthLunch.State.DISABLED_ORDERED)) {
                return i;
            }
        }
        return -1;
    }

    public MonthLunch getOrderedLunch() {
        for (MonthLunch lunch : getLunches()) {
            MonthLunch.State state = lunch.getState();
            if (state.equals(MonthLunch.State.ORDERED) ||
                    state.equals(MonthLunch.State.DISABLED_ORDERED)) {
                return lunch;
            }
        }
        return null;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void disable() {
        disabled = true;
        for (MonthLunch lunch : getLunches())
            lunch.disable();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) || o instanceof MonthLunchDay
                && ((MonthLunchDay) o).getDate().equals(getDate())
                && Arrays.equals(((MonthLunchDay) o).getLunches(), getLunches());
    }

    @Override
    public CharSequence getTitle(Context context, int position) {
        int orderedLunchIndex = getOrderedLunchIndex();
        return DATE_SHOW_FORMAT.format(getDate()) + (orderedLunchIndex == -1 ? "" :
                " - " + context.getText(R.string.text_lunch) + " " + (orderedLunchIndex + 1));
    }

    @Override
    public CharSequence getText(Context context, int position) {
        MonthLunch orderedLunch = getOrderedLunch();
        return orderedLunch == null ? context.getText(R.string
                .text_nothing_ordered) : orderedLunch.getName();
    }
}
