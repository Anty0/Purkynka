package cz.anty.utils.icanteen.lunch;

import android.content.Context;

import cz.anty.utils.listItem.MultilineItem;

/**
 * Created by anty on 17.8.15.
 *
 * @author anty
 */
public class BurzaLunch implements MultilineItem {

    LunchNumber lunchNumber;
    String date, name, canteen;
    int pieces;
    String orderUrlAdd;

    BurzaLunch(LunchNumber lunchNumber, String date, String name, String canteen, int pieces, String orderUrlAdd) {
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

    public String getDate() {
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
    public String getTitle(Context context) {
        return getPieces() + " x " + getLunchNumber().toString();
    }

    @Override
    public String getText(Context context) {
        return getName();
    }

    @Override
    public Integer getLayoutResourceId(Context context) {
        return null;
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
