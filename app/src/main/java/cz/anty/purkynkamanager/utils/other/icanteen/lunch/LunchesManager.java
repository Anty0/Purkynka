package cz.anty.purkynkamanager.utils.other.icanteen.lunch;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.anty.purkynkamanager.ApplicationBase;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.burza.BurzaLunch;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunch;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunchDay;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public class LunchesManager {

    private static final String LOG_TAG = "LunchesManager";
    private static final int LUNCHES_SAVE_VERSION = 3;

    private final Context context;
    private final List<MonthLunchDay> mMonthLunches = new ArrayList<>();
    private final List<BurzaLunch> mBurzaLunches = new ArrayList<>();
    private final List<BurzaLunchOrderRequest> mBurzaOrderRequests = new ArrayList<>();
    private final List<MonthLunchOrderRequest> mMonthOrderRequests = new ArrayList<>();
    private final List<MonthToBurzaLunchOrderRequest> mMonthToBurzaOrderRequests = new ArrayList<>();

    public LunchesManager(@NonNull Context context) {
        this.context = context;
        load();
    }

    private static String lunchesToString(MonthLunchDay... lunchDays) {
        try {
            return ApplicationBase.GSON.toJson(lunchDays);
        } catch (Throwable t) {
            Log.d(LOG_TAG, "lunchesToString", t);
            return "";
        }
    }

    public static MonthLunchDay[] parseLunches(String toParse) {
        if (toParse.equals("")) return new MonthLunchDay[0];
        try {
            return ApplicationBase.GSON.fromJson(toParse, MonthLunchDay[].class);
        } catch (Throwable t) {
            Log.d(LOG_TAG, "lunchesToString", t);
            return new MonthLunchDay[0];
        }
    }

    private static String ordersToString(MonthLunchOrderRequest... lunchOrders) {
        MonthLunch[] lunches = new MonthLunch[lunchOrders.length];
        for (int i = 0; i < lunchOrders.length; i++) {
            lunches[i] = lunchOrders[i].getMonthLunch();
        }
        try {
            return ApplicationBase.GSON.toJson(lunches);
        } catch (Throwable t) {
            Log.d(LOG_TAG, "ordersToString", t);
            return "";
        }
    }

    private static String ordersToString(BurzaLunchOrderRequest... lunchOrders) {
        BurzaLunch[] lunches = new BurzaLunch[lunchOrders.length];
        for (int i = 0; i < lunchOrders.length; i++) {
            lunches[i] = lunchOrders[i].getBurzaLunch();
        }

        try {
            return ApplicationBase.GSON.toJson(lunches);
        } catch (Throwable t) {
            Log.d(LOG_TAG, "ordersToString", t);
            return "";
        }
    }

    private static String ordersToString(MonthToBurzaLunchOrderRequest... lunchOrders) {
        MonthLunch[] lunches = new MonthLunch[lunchOrders.length];
        for (int i = 0; i < lunchOrders.length; i++) {
            lunches[i] = lunchOrders[i].getMonthLunch();
        }
        try {
            return ApplicationBase.GSON.toJson(lunches);
        } catch (Throwable t) {
            Log.d(LOG_TAG, "ordersToString", t);
            return "";
        }
    }

    public static MonthLunchOrderRequest[] parseMonthOrders(String toParse) {
        if (toParse.equals("")) return new MonthLunchOrderRequest[0];
        MonthLunch[] lunches;
        try {
            lunches = ApplicationBase.GSON.fromJson(toParse, MonthLunch[].class);
        } catch (Throwable t) {
            Log.d(LOG_TAG, "ordersToString", t);
            lunches = new MonthLunch[0];
        }
        MonthLunchOrderRequest[] lunchOrders = new MonthLunchOrderRequest[lunches.length];
        for (int i = 0; i < lunches.length; i++) {
            lunchOrders[i] = new MonthLunchOrderRequest(lunches[i]);
        }
        return lunchOrders;
    }

    public static BurzaLunchOrderRequest[] parseBurzaOrders(String toParse) {
        if (toParse.equals("")) return new BurzaLunchOrderRequest[0];
        BurzaLunch[] lunches;
        try {
            lunches = ApplicationBase.GSON.fromJson(toParse, BurzaLunch[].class);
        } catch (Throwable t) {
            Log.d(LOG_TAG, "ordersToString", t);
            lunches = new BurzaLunch[0];
        }
        BurzaLunchOrderRequest[] lunchOrders = new BurzaLunchOrderRequest[lunches.length];
        for (int i = 0; i < lunches.length; i++) {
            lunchOrders[i] = new BurzaLunchOrderRequest(lunches[i]);
        }
        return lunchOrders;
    }

    public static MonthToBurzaLunchOrderRequest[] parseMonthToBurzaOrders(String toParse) {
        if (toParse.equals("")) return new MonthToBurzaLunchOrderRequest[0];
        MonthLunch[] lunches;
        try {
            lunches = ApplicationBase.GSON.fromJson(toParse, MonthLunch[].class);
        } catch (Throwable t) {
            Log.d(LOG_TAG, "ordersToString", t);
            lunches = new MonthLunch[0];
        }
        MonthToBurzaLunchOrderRequest[] lunchOrders = new
                MonthToBurzaLunchOrderRequest[lunches.length];
        for (int i = 0; i < lunches.length; i++) {
            lunchOrders[i] = new MonthToBurzaLunchOrderRequest(lunches[i]);
        }
        return lunchOrders;
    }

    public synchronized void tryProcessOrders() {
        for (LunchOrderRequest request : getLunchOrderRequests()) {
            if (request.getState().equals(LunchOrderRequest.State.COMPLETED)
                    || request.tryOrder()) {
                if (request instanceof BurzaLunchOrderRequest
                        && mBurzaOrderRequests.contains(request))
                    removeLunchOrderRequest((BurzaLunchOrderRequest) request);
                else if (request instanceof MonthLunchOrderRequest
                        && mMonthOrderRequests.contains(request))
                    removeLunchOrderRequest((MonthLunchOrderRequest) request);
                else if (request instanceof MonthToBurzaLunchOrderRequest
                        && mMonthToBurzaOrderRequests.contains(request))
                    removeLunchOrderRequest((MonthToBurzaLunchOrderRequest) request);
                else Log.d(LOG_TAG, "tryProcessOrders: request is not in lists");
            }
        }
    }

    public synchronized boolean isPendingOrders() {
        return mBurzaOrderRequests.size() > 0 || mMonthOrderRequests.size() > 0
                || mMonthToBurzaOrderRequests.size() > 0;
    }

    public synchronized void addLunchOrderRequest(MonthLunchOrderRequest request) {
        mMonthOrderRequests.add(request);
        apply();
    }

    public synchronized void addLunchOrderRequest(BurzaLunchOrderRequest request) {
        mBurzaOrderRequests.add(request);
        apply();
    }

    public synchronized void addLunchOrderRequest(MonthToBurzaLunchOrderRequest request) {
        mMonthToBurzaOrderRequests.add(request);
        apply();
    }

    public synchronized void removeLunchOrderRequest(MonthLunchOrderRequest request) {
        mMonthOrderRequests.remove(request);
        apply();
    }

    public synchronized void removeLunchOrderRequest(BurzaLunchOrderRequest request) {
        mBurzaOrderRequests.remove(request);
        apply();
    }

    public synchronized void removeLunchOrderRequest(MonthToBurzaLunchOrderRequest request) {
        mMonthToBurzaOrderRequests.remove(request);
        apply();
    }

    public synchronized List<LunchOrderRequest> getLunchOrderRequests() {
        List<LunchOrderRequest> requests = new ArrayList<>();
        requests.addAll(mBurzaOrderRequests);
        requests.addAll(mMonthOrderRequests);
        requests.addAll(mMonthToBurzaOrderRequests);
        return requests;
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
        String toParseMonthOrders = preferences.getString(Constants.SETTING_NAME_LUNCHES_MONTH_ORDER_REQUESTS, "");
        String toParseBurzaOrders = preferences.getString(Constants.SETTING_NAME_LUNCHES_BURZA_ORDER_REQUESTS, "");
        String toParseMonthToBurzaOrders = preferences.getString(Constants.SETTING_NAME_LUNCHES_MONTH_TO_BURZA_ORDER_REQUESTS, "");

        setItems(parseLunches(toParse));
        mMonthOrderRequests.clear();
        Collections.addAll(mMonthOrderRequests, parseMonthOrders(toParseMonthOrders));
        mBurzaOrderRequests.clear();
        Collections.addAll(mBurzaOrderRequests, parseBurzaOrders(toParseBurzaOrders));
        mMonthToBurzaOrderRequests.clear();
        Collections.addAll(mMonthToBurzaOrderRequests, parseMonthToBurzaOrders(toParseMonthToBurzaOrders));
    }

    public synchronized void apply() {
        context.getSharedPreferences(Constants.SETTINGS_NAME_LUNCHES, Context.MODE_PRIVATE).edit()
                .putString(Constants.SETTING_NAME_MONTH_LUNCHES, lunchesToString(getAllMonthLunches()))
                .putString(Constants.SETTING_NAME_LUNCHES_MONTH_ORDER_REQUESTS,
                        ordersToString(mMonthOrderRequests.toArray(new MonthLunchOrderRequest[mMonthOrderRequests.size()])))
                .putString(Constants.SETTING_NAME_LUNCHES_BURZA_ORDER_REQUESTS,
                        ordersToString(mBurzaOrderRequests.toArray(new BurzaLunchOrderRequest[mBurzaOrderRequests.size()])))
                .putString(Constants.SETTING_NAME_LUNCHES_MONTH_TO_BURZA_ORDER_REQUESTS,
                        ordersToString(mMonthToBurzaOrderRequests.toArray(new MonthToBurzaLunchOrderRequest[mMonthToBurzaOrderRequests.size()])))
                .apply();
    }

    @Override
    public String toString() {
        return lunchesToString(getAllMonthLunches());
    }

}
