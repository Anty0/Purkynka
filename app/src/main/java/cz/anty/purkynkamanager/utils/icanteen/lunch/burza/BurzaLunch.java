package cz.anty.purkynkamanager.utils.icanteen.lunch.burza;

import android.content.Context;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cz.anty.purkynkamanager.utils.list.listView.MultilineItem;

/**
 * Created by anty on 17.8.15.
 *
 * @author anty
 */
public class BurzaLunch implements MultilineItem {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    private final LunchNumber lunchNumber;
    private final Date date;
    private final String name, canteen;
    private final int pieces;
    private final String orderUrlAdd;

    public BurzaLunch(@NonNull LunchNumber lunchNumber, @NonNull Date date,
                      @NonNull String name, @NonNull String canteen, int pieces,
                      @NonNull String orderUrlAdd) {
        this.lunchNumber = lunchNumber;
        this.date = date;
        this.name = name;
        this.canteen = canteen;
        this.pieces = pieces;
        this.orderUrlAdd = orderUrlAdd;
    }

    public LunchNumber getLunchNumber() {
        return lunchNumber;
    }

    public Date getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getCanteen() {
        return canteen;
    }

    public int getPieces() {
        return pieces;
    }

    public String getOrderUrlAdd() {
        return orderUrlAdd;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) || o instanceof BurzaLunch
                && ((BurzaLunch) o).getLunchNumber().equals(getLunchNumber())
                && ((BurzaLunch) o).getDate().equals(getDate())
                && ((BurzaLunch) o).getCanteen().equals(getCanteen())
                && ((BurzaLunch) o).getPieces() == getPieces()
                && ((BurzaLunch) o).getOrderUrlAdd().equals(getOrderUrlAdd());
    }

    @Override
    public CharSequence getTitle(Context context, int position) {
        return DATE_FORMAT.format(getDate()) + " - " + getPieces() + " x " + getLunchNumber().toString();
    }

    @Override
    public CharSequence getText(Context context, int position) {
        return getName();
    }

    public enum LunchNumber {
        LUNCH_1, LUNCH_2, LUNCH_3;

        public static LunchNumber parseLunchNumber(String toParse) {
            switch (toParse) {
                case "Oběd 3":
                    return LUNCH_3;
                case "Oběd 2":
                    return LUNCH_2;
                case "Oběd 1":
                default:
                    return LUNCH_1;
            }
        }

        @Override
        public String toString() {
            switch (this) {
                case LUNCH_3:
                    return "Oběd 3";
                case LUNCH_2:
                    return "Oběd 2";
                case LUNCH_1:
                default:
                    return "Oběd 1";
            }
        }
    }
}
