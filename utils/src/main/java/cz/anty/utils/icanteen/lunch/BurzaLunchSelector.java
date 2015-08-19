package cz.anty.utils.icanteen.lunch;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by anty on 19.8.15.
 *
 * @author anty
 */
public class BurzaLunchSelector {

    private final BurzaLunch.LunchNumber[] lunchNumber;
    private final Date date;

    public BurzaLunchSelector(@NonNull BurzaLunch.LunchNumber[] lunchNumber, @NonNull Date date) {
        this.lunchNumber = lunchNumber;
        this.date = date;
    }

    public boolean isSelected(BurzaLunch lunch) {
        return date.equals(lunch.getDate())
                && checkLunchNumber(lunch.getLunchNumber());
    }

    private boolean checkLunchNumber(BurzaLunch.LunchNumber lunchNumber) {
        for (BurzaLunch.LunchNumber lunchNumber1 : this.lunchNumber) {
            if (lunchNumber1.equals(lunchNumber)) return true;
        }
        return false;
    }
}
