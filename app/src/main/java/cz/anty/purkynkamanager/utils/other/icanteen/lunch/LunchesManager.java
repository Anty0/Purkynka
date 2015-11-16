package cz.anty.purkynkamanager.utils.other.icanteen.lunch;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.anty.purkynkamanager.ApplicationBase;
import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.icanteen.ICService;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.burza.BurzaLunch;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunch;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunchDay;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;

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
    private final List<ICService.BurzaLunchOrderRequest> mBurzaOrderRequests = new ArrayList<>();
    private final List<ICService.MonthLunchOrderRequest> mMonthOrderRequests = new ArrayList<>();
    private final List<ICService.MonthToBurzaLunchOrderRequest> mMonthToBurzaOrderRequests = new ArrayList<>();

    public LunchesManager(@NonNull Context context) {
        this.context = context;
        load();
    }

    private static String lunchesToString(MonthLunchDay... lunchDays) {
        return ApplicationBase.GSON.toJson(lunchDays);
    }

    public static MonthLunchDay[] parseLunches(String toParse) {
        if (toParse.equals("")) return new MonthLunchDay[0];
        return ApplicationBase.GSON.fromJson(toParse, MonthLunchDay[].class);
    }

    private static String ordersToString(ICService.MonthLunchOrderRequest... lunchOrders) {
        MonthLunch[] lunches = new MonthLunch[lunchOrders.length];
        for (int i = 0; i < lunchOrders.length; i++) {
            lunches[i] = lunchOrders[i].getMonthLunch();
        }
        return ApplicationBase.GSON.toJson(lunches);
    }

    private static String ordersToString(ICService.BurzaLunchOrderRequest... lunchOrders) {
        BurzaLunch[] lunches = new BurzaLunch[lunchOrders.length];
        for (int i = 0; i < lunchOrders.length; i++) {
            lunches[i] = lunchOrders[i].getBurzaLunch();
        }
        return ApplicationBase.GSON.toJson(lunches);
    }

    private static String ordersToString(ICService.MonthToBurzaLunchOrderRequest... lunchOrders) {
        MonthLunch[] lunches = new MonthLunch[lunchOrders.length];
        for (int i = 0; i < lunchOrders.length; i++) {
            lunches[i] = lunchOrders[i].getMonthLunch();
        }
        return ApplicationBase.GSON.toJson(lunches);
    }

    public static ICService.MonthLunchOrderRequest[] parseMonthOrders(String toParse) {
        if (toParse.equals("")) return new ICService.MonthLunchOrderRequest[0];
        MonthLunch[] lunches = ApplicationBase.GSON.fromJson(toParse, MonthLunch[].class);
        ICService.MonthLunchOrderRequest[] lunchOrders = new ICService.MonthLunchOrderRequest[lunches.length];
        for (int i = 0; i < lunches.length; i++) {
            lunchOrders[i] = new ICService.MonthLunchOrderRequest(lunches[i]);
        }
        return lunchOrders;
    }

    public static ICService.BurzaLunchOrderRequest[] parseBurzaOrders(String toParse) {
        if (toParse.equals("")) return new ICService.BurzaLunchOrderRequest[0];
        BurzaLunch[] lunches = ApplicationBase.GSON.fromJson(toParse, BurzaLunch[].class);
        ICService.BurzaLunchOrderRequest[] lunchOrders = new ICService.BurzaLunchOrderRequest[lunches.length];
        for (int i = 0; i < lunches.length; i++) {
            lunchOrders[i] = new ICService.BurzaLunchOrderRequest(lunches[i]);
        }
        return lunchOrders;
    }

    public static ICService.MonthToBurzaLunchOrderRequest[] parseMonthToBurzaOrders(String toParse) {
        if (toParse.equals("")) return new ICService.MonthToBurzaLunchOrderRequest[0];
        MonthLunch[] lunches = ApplicationBase.GSON.fromJson(toParse, MonthLunch[].class);
        ICService.MonthToBurzaLunchOrderRequest[] lunchOrders =
                new ICService.MonthToBurzaLunchOrderRequest[lunches.length];
        for (int i = 0; i < lunches.length; i++) {
            lunchOrders[i] = new ICService.MonthToBurzaLunchOrderRequest(lunches[i]);
        }
        return lunchOrders;
    }

    public synchronized void tryProcessOrders() {
        for (LunchOrderRequest request : getLunchOrderRequests()) {
            if (request.getState().equals(LunchOrderRequest.State.COMPLETED)
                    || request.tryOrder()) {
                if (request instanceof ICService.BurzaLunchOrderRequest
                        && mBurzaOrderRequests.contains(request))
                    removeLunchOrderRequest((ICService.BurzaLunchOrderRequest) request);
                else if (request instanceof ICService.MonthLunchOrderRequest
                        && mMonthOrderRequests.contains(request))
                    removeLunchOrderRequest((ICService.MonthLunchOrderRequest) request);
                else if (request instanceof ICService.MonthToBurzaLunchOrderRequest
                        && mMonthToBurzaOrderRequests.contains(request))
                    removeLunchOrderRequest((ICService.MonthToBurzaLunchOrderRequest) request);
                else Log.d(LOG_TAG, "tryProcessOrders: request is not in lists");
            }
        }
    }

    public synchronized boolean isPendingOrders() {
        return mBurzaOrderRequests.size() > 0 || mMonthOrderRequests.size() > 0
                || mMonthToBurzaOrderRequests.size() > 0;
    }

    public synchronized void addLunchOrderRequest(ICService.MonthLunchOrderRequest request) {
        mMonthOrderRequests.add(request);
        apply();
    }

    public synchronized void addLunchOrderRequest(ICService.BurzaLunchOrderRequest request) {
        mBurzaOrderRequests.add(request);
        apply();
    }

    public synchronized void addLunchOrderRequest(ICService.MonthToBurzaLunchOrderRequest request) {
        mMonthToBurzaOrderRequests.add(request);
        apply();
    }

    public synchronized void removeLunchOrderRequest(ICService.MonthLunchOrderRequest request) {
        mMonthOrderRequests.remove(request);
        apply();
    }

    public synchronized void removeLunchOrderRequest(ICService.BurzaLunchOrderRequest request) {
        mBurzaOrderRequests.remove(request);
        apply();
    }

    public synchronized void removeLunchOrderRequest(ICService.MonthToBurzaLunchOrderRequest request) {
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
                        ordersToString(mMonthOrderRequests.toArray(new ICService
                                .MonthLunchOrderRequest[mMonthOrderRequests.size()])))
                .putString(Constants.SETTING_NAME_LUNCHES_BURZA_ORDER_REQUESTS,
                        ordersToString(mBurzaOrderRequests.toArray(new ICService
                                .BurzaLunchOrderRequest[mBurzaOrderRequests.size()])))
                .putString(Constants.SETTING_NAME_LUNCHES_MONTH_TO_BURZA_ORDER_REQUESTS,
                        ordersToString(mMonthToBurzaOrderRequests.toArray(new ICService
                                .MonthToBurzaLunchOrderRequest[mMonthToBurzaOrderRequests.size()])))
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
