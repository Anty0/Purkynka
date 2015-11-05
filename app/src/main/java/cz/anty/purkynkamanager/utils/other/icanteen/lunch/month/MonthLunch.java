package cz.anty.purkynkamanager.utils.other.icanteen.lunch.month;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.Locale;

import cz.anty.purkynkamanager.R;
import proguard.annotation.Keep;
import proguard.annotation.KeepClassMemberNames;
import proguard.annotation.KeepClassMembers;
import proguard.annotation.KeepName;

/**
 * Created by anty on 24.8.15.
 *
 * @author anty
 */
@Keep
@KeepName
@KeepClassMembers
@KeepClassMemberNames
public class MonthLunch {

    private final String name, orderUrlAdd, toBurzaUrlAdd;
    private final Date date;
    private final BurzaState burzaState;
    private final State state;
    private boolean disabled = false;

    public MonthLunch(String name, Date date, @Nullable String orderUrlAdd, State state,
                      @Nullable String toBurzaUrlAdd, @Nullable BurzaState burzaState) {
        this.name = name;
        this.date = date;
        this.orderUrlAdd = orderUrlAdd;
        this.state = state;
        this.toBurzaUrlAdd = toBurzaUrlAdd;
        this.burzaState = burzaState;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public String getOrderUrlAdd() {
        //if (disabled) return null;
        return orderUrlAdd;
    }

    public State getState() {
        if (disabled) return state == State.ORDERED
                || state == State.DISABLED_ORDERED
                ? State.DISABLED_ORDERED : State.DISABLED;
        return state;
    }

    public String getToBurzaUrlAdd() {
        //if (disabled) return null;
        return toBurzaUrlAdd;
    }

    public BurzaState getBurzaState() {
        if (disabled) return null;
        return burzaState;
    }

    public boolean isDisabled() {
        return disabled;
    }

    void disable() {
        disabled = true;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) || o instanceof MonthLunch
                && ((MonthLunch) o).name.equals(name)
                && ((MonthLunch) o).date.equals(date);
        //&& ((MonthLunch) o).state.equals(state);
    }

    @Keep
    @KeepName
    @KeepClassMembers
    @KeepClassMemberNames
    public enum State {
        ENABLED, DISABLED, DISABLED_ORDERED, ORDERED, UNKNOWN;

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ENGLISH);
        }
    }

    @Keep
    @KeepName
    @KeepClassMembers
    @KeepClassMemberNames
    public enum BurzaState {
        TO_BURZA, FROM_BURZA;

        public CharSequence toCharSequence(Context context) {
            switch (this) {
                case TO_BURZA:
                    return context.getText(R.string.text_to_burza);
                case FROM_BURZA:
                    return context.getText(R.string.text_from_burza);
            }
            return toString();
        }

        @Override
        public String toString() {
            switch (this) {
                case TO_BURZA:
                    return "do burzy";
                case FROM_BURZA:
                    return "z burzy";
            }
            return super.toString().toLowerCase(Locale.ENGLISH);
        }
    }
}
