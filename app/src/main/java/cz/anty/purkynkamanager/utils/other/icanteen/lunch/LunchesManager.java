package cz.anty.purkynkamanager.utils.other.icanteen.lunch;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.burza.BurzaLunch;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunchDay;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public class LunchesManager {

    private static final int LUNCHES_SAVE_VERSION = 2;

    private final Context context;
    private final List<MonthLunchDay> mMonthLunches = new ArrayList<>();
    private final List<BurzaLunch> mBurzaLunches = new ArrayList<>();
    private final List<LunchOrderRequest> mOrderRequests = new ArrayList<>();

    public LunchesManager(@NonNull Context context) {
        this.context = context;
        load();
    }

    private static String lunchesToString(MonthLunchDay... lunchDays) {
        return new Gson().toJson(lunchDays);
    }

    public static MonthLunchDay[] parseLunches(String toParse) {
        if (toParse.equals("")) return new MonthLunchDay[0];
        return new Gson().fromJson(toParse, MonthLunchDay[].class);
    }

    private static String ordersToString(LunchOrderRequest... lunchOrders) {
        return new Gson().toJson(lunchOrders);
    }

    public static LunchOrderRequest[] parseOrders(String toParse) {
        if (toParse.equals("")) return new LunchOrderRequest[0];
        return new Gson().fromJson(toParse, LunchOrderRequest[].class);
    }

    public synchronized void tryProcessOrders() {
        LunchOrderRequest[] orderRequests = mOrderRequests
                .toArray(new LunchOrderRequest[mOrderRequests.size()]);
        for (LunchOrderRequest request : orderRequests) {
            if (request.getState().equals(LunchOrderRequest.State.COMPLETED)
                    || request.tryOrder()) mOrderRequests.remove(request);
        }
    }

    public synchronized boolean isPendingOrders() {
        return mOrderRequests.size() > 0;
    }

    public synchronized void addLunchOrderRequest(LunchOrderRequest request) {
        mOrderRequests.add(request);
    }

    public synchronized void removeLunchOrderRequest(LunchOrderRequest request) {
        mOrderRequests.remove(request);
    }

    public synchronized LunchOrderRequest[] getLunchOrderRequests() {
        return mOrderRequests.toArray(new LunchOrderRequest[mOrderRequests.size()]);
    }

    private synchronized void setLunchOrderRequests(LunchOrderRequest[] requests) {
        mOrderRequests.clear();
        Collections.addAll(mOrderRequests, requests);
    }

    public synchronized boolean setItems(MonthLunchDay[] lunchDays) {
        //mMonthLunches.clear();
        //Collections.addAll(mMonthLunches, lunchDays);
        for (MonthLunchDay lunchDay : mMonthLunches) {
            lunchDay.disable();
        }

        boolean newLunches = false;
        MonthLunchDay[] listLunchDays = mMonthLunches
                .toArray(new MonthLunchDay[mMonthLunches.size()]);
        for (MonthLunchDay lunchDay : lunchDays) {
            boolean inserted = false;
            for (MonthLunchDay listLunchDay : listLunchDays) {
                if (lunchDay.getDate().equals(listLunchDay.getDate())) {
                    int index = mMonthLunches.indexOf(listLunchDay);
                    mMonthLunches.remove(index);
                    mMonthLunches.add(index, lunchDay);
                    inserted = true;
                    break;
                }
            }
            if (!inserted) {
                mMonthLunches.add(lunchDay);
                newLunches = true;
            }
        }
        return newLunches;
    }

    public synchronized void setItems(BurzaLunch[] burzaLunches) {
        mBurzaLunches.clear();
        Collections.addAll(mBurzaLunches, burzaLunches);
    }

    public synchronized void removeDisabledLunch(MonthLunchDay lunchDay) {
        if (!lunchDay.isDisabled())
            throw new IllegalArgumentException("MonthLunchDay is not disabled");
        mMonthLunches.remove(lunchDay);
    }

    public synchronized void removeAllDisabledLunches() {
        for (MonthLunchDay lunchDay : mMonthLunches
                .toArray(new MonthLunchDay[mMonthLunches.size()])) {
            if (lunchDay.isDisabled()) mMonthLunches.remove(lunchDay);
        }
    }

    public synchronized MonthLunchDay[] getAllMonthLunches() {
        return mMonthLunches.toArray(new MonthLunchDay[mMonthLunches.size()]);
    }

    public synchronized MonthLunchDay[] getNewMonthLunches() {
        ArrayList<MonthLunchDay> monthLunches = new ArrayList<>();
        for (MonthLunchDay lunchDay : mMonthLunches)
            if (!lunchDay.isDisabled())
                monthLunches.add(lunchDay);
        return monthLunches.toArray(new MonthLunchDay[monthLunches.size()]);
    }

    public synchronized MonthLunchDay[] getOldMonthLunches() {
        ArrayList<MonthLunchDay> monthLunches = new ArrayList<>();
        for (MonthLunchDay lunchDay : mMonthLunches)
            if (lunchDay.isDisabled())
                monthLunches.add(lunchDay);
        return monthLunches.toArray(new MonthLunchDay[monthLunches.size()]);
    }

    public synchronized BurzaLunch[] getBurzaLunches() {
        return mBurzaLunches.toArray(new BurzaLunch[mBurzaLunches.size()]);
    }

    private synchronized void load() {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SETTINGS_NAME_LUNCHES, Context.MODE_PRIVATE);
        if (preferences.getInt(Constants.SETTING_NAME_LUNCHES_SAVE_VERSION, -1) != LUNCHES_SAVE_VERSION) {
            apply();
            preferences.edit()
                    .putInt(Constants.SETTING_NAME_LUNCHES_SAVE_VERSION, LUNCHES_SAVE_VERSION)
                    .apply();
            return;
        }

        String toParse = preferences.getString(Constants.SETTING_NAME_MONTH_LUNCHES, "");
        String toParseOrders = preferences.getString(Constants.SETTING_NAME_LUNCHES_ORDER_REQUESTS, "");

        setItems(parseLunches(toParse));
        setLunchOrderRequests(parseOrders(toParseOrders));
    }

    public synchronized void apply() {
        context.getSharedPreferences(Constants.SETTINGS_NAME_LUNCHES, Context.MODE_PRIVATE).edit()
                .putString(Constants.SETTING_NAME_MONTH_LUNCHES, lunchesToString(getAllMonthLunches()))
                .putString(Constants.SETTING_NAME_LUNCHES_ORDER_REQUESTS, ordersToString(getLunchOrderRequests()))
                .apply();
    }

    @Override
    public String toString() {
        return lunchesToString(getAllMonthLunches());
    }

    public interface LunchOrderRequest extends MultilineItem {
        boolean tryOrder();

        State getState();

        enum State {
            WAITING, ERROR, COMPLETED;

            public CharSequence toCharSequence(Context context) {
                switch (this) {
                    case COMPLETED:
                        return context.getText(R.string.text_completed);
                    case ERROR:
                        return context.getText(R.string.text_error);
                    case WAITING:
                        return context.getText(R.string.text_waiting);
                }
                return super.toString();
            }
        }
    }

    public static abstract class SimpleLunchOrderRequest implements LunchOrderRequest {

        private static final String LOG_TAG = "SimpleLunchOrderRequest";

        private State state = State.WAITING;

        @Override
        public boolean tryOrder() {
            try {
                if (state == State.COMPLETED) return true;
                if (doOrder()) {
                    state = State.COMPLETED;
                    return true;
                }
            } catch (Throwable throwable) {
                Log.d(LOG_TAG, "tryOrder", throwable);
                state = State.ERROR;
            }
            return false;
        }

        public abstract boolean doOrder() throws Throwable;

        @Override
        public State getState() {
            return state;
        }

        @Override
        public CharSequence getText(Context context, int position) {
            return state.toCharSequence(context);
        }
    }

}
