package cz.anty.utils.icanteen.lunch.month;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import cz.anty.utils.listItem.MultilineItem;

/**
 * Created by anty on 17.8.15.
 *
 * @author anty
 */
public class MonthLunchDay implements MultilineItem {

    public static final SimpleDateFormat DATE_PARSE_FORMAT = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private static final SimpleDateFormat DATE_SHOW_FORMAT = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    private final Date date;
    private final MonthLunch[] lunches;

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

    public MonthLunch getOrderedLunch() {
        MonthLunch orderedLunch = null;
        for (MonthLunch lunch : getLunches())
            if (lunch.getState().equals(MonthLunch.State.ORDERED)) {
                orderedLunch = lunch;
                break;
            }
        return orderedLunch;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) || o instanceof MonthLunchDay
                && ((MonthLunchDay) o).getDate().equals(getDate())
                && Arrays.equals(((MonthLunchDay) o).getLunches(), getLunches());
    }

    @Override
    public String getTitle(Context context) {
        return DATE_SHOW_FORMAT.format(getDate());
    }

    @Override
    public String getText(Context context) {
        MonthLunch orderedLunch = getOrderedLunch();
        return orderedLunch == null ? "Nothing ordered" : orderedLunch.getName();//TODO add to strings
    }

    @Override
    public Integer getLayoutResourceId(Context context) {
        return null;
    }
}
