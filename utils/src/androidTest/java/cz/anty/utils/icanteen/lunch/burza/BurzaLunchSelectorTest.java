package cz.anty.utils.icanteen.lunch.burza;

import android.util.Log;

import junit.framework.TestCase;

import java.util.Date;

/**
 * Created by anty on 8.9.15.
 *
 * @author anty
 */
public class BurzaLunchSelectorTest extends TestCase {

    public void testIsSelected() throws Exception {
        long time = System.currentTimeMillis();
        BurzaLunchSelector lunchSelector = new BurzaLunchSelector(
                new BurzaLunch.LunchNumber[]{BurzaLunch.LunchNumber.LUNCH_1, BurzaLunch.LunchNumber.LUNCH_2}, new Date(time));
        Log.d(getClass().getSimpleName(), "testIsSelected is selected? " + lunchSelector.isSelected(
                new BurzaLunch(BurzaLunch.LunchNumber.LUNCH_1, new Date(time), "TestName", "testCanteen", 1, "testURL")));
    }

    public void testToString() throws Exception {
        long time = System.currentTimeMillis();
        BurzaLunchSelector lunchSelector = new BurzaLunchSelector(
                BurzaLunch.LunchNumber.values(), new Date(time));
        String string = lunchSelector.toString();
        Log.d(getClass().getSimpleName(), "testToString string: " + string);
        Log.d(getClass().getSimpleName(), "testToString equals: " + lunchSelector
                .equals(BurzaLunchSelector.parseBurzaLunchSelector(string)));

    }
}