package cz.anty.purkynkamanager.utils.other.icanteen.lunch.burza;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Date;

import cz.anty.purkynkamanager.ApplicationBase;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunchDay;
import proguard.annotation.Keep;
import proguard.annotation.KeepClassMemberNames;
import proguard.annotation.KeepClassMembers;
import proguard.annotation.KeepName;

/**
 * Created by anty on 19.8.15.
 *
 * @author anty
 */
@Keep
@KeepName
@KeepClassMembers
@KeepClassMemberNames
public class BurzaLunchSelector {

    private static final String LOG_TAG = "BurzaLunchSelector";
    //private static final String LUNCH_SPLIT_VALUE = "/";
    //private static final String DATA_SPLIT_VALUE = "\n";

    private final BurzaLunch.LunchNumber[] lunchNumbers;
    private final Date date;

    public BurzaLunchSelector(@NonNull BurzaLunch.LunchNumber[] lunchNumbers, @NonNull Date date) {
        this.lunchNumbers = lunchNumbers;
        this.date = date;
    }

    public static BurzaLunchSelector parseBurzaLunchSelector(String toParse) {
        if (toParse == null || "".equals(toParse)) return null;
        try {
            return ApplicationBase.GSON.fromJson(toParse, BurzaLunchSelector.class);
        } catch (Throwable t) {
            Log.d(LOG_TAG, "", t);
            return null;
        }
        /*String[] data = toParse.split(DATA_SPLIT_VALUE);

        BurzaLunch.LunchNumber[] lunchNumbers;
        String[] lunchNumbersData = data[0].split(LUNCH_SPLIT_VALUE);
        if (lunchNumbersData.length == 1 && lunchNumbersData[0].equals(""))
            lunchNumbers = new BurzaLunch.LunchNumber[0];
        else {
            List<BurzaLunch.LunchNumber> lunchNumberList = new ArrayList<>();
            for (String lunchNumberData : lunchNumbersData)
                lunchNumberList.add(BurzaLunch.LunchNumber
                        .parseLunchNumber(lunchNumberData));
            lunchNumbers = lunchNumberList.toArray(new BurzaLunch.LunchNumber[lunchNumberList.size()]);
        }

        return new BurzaLunchSelector(lunchNumbers, new Date(Long.parseLong(data[1])));*/
    }

    public boolean isSelected(MonthLunchDay lunch) {
        return date.equals(lunch.getDate());
    }

    public boolean isSelected(BurzaLunch lunch) {
        /*Log.d(getClass().getSimpleName(), "isSelected date1: " + date + " date2: "
                + lunch.getDate() + " equals: " + date.equals(lunch.getDate()));*/
        return date.equals(lunch.getDate())
                && checkLunchNumber(lunch.getLunchNumber());
    }

    private boolean checkLunchNumber(BurzaLunch.LunchNumber lunchNumber) {
        for (BurzaLunch.LunchNumber lunchNumber1 : this.lunchNumbers) {
            if (lunchNumber1.equals(lunchNumber)) return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) || o instanceof BurzaLunchSelector
                && date.equals(((BurzaLunchSelector) o).date)
                && Arrays.equals(lunchNumbers, ((BurzaLunchSelector) o).lunchNumbers);
    }

    @Override
    public String toString() {
        try {
            return ApplicationBase.GSON.toJson(this);
        } catch (Throwable t) {
            Log.d(LOG_TAG, "", t);
            return "";
        }
        /*StringBuilder builder = new StringBuilder();
        if (lunchNumbers.length > 0) {
            builder.append(lunchNumbers[0]);
            for (int i = 1; i < lunchNumbers.length; i++)
                builder.append(LUNCH_SPLIT_VALUE).append(lunchNumbers[i]);
        }
        builder.append(DATA_SPLIT_VALUE).append(date.getTime());
        return builder.toString();*/
    }
}
