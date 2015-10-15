package cz.anty.purkynkamanager.utils.icanteen.lunch.month;

import android.support.annotation.Nullable;

import java.util.Locale;

/**
 * Created by anty on 24.8.15.
 *
 * @author anty
 */
public class MonthLunch {

    private final String name, orderUrlAdd, toBurzaUrlAdd;
    private final BurzaState burzaState;
    private final State state;
    private boolean disabled = false;

    public MonthLunch(String name, @Nullable String orderUrlAdd, State state, @Nullable String toBurzaUrlAdd, @Nullable BurzaState burzaState) {
        this.name = name;
        this.orderUrlAdd = orderUrlAdd;
        this.state = state;
        this.toBurzaUrlAdd = toBurzaUrlAdd;
        this.burzaState = burzaState;
    }

    public String getName() {
        return name;
    }

    public String getOrderUrlAdd() {
        if (disabled) return null;
        return orderUrlAdd;
    }

    public State getState() {
        if (disabled) return state == State.ORDERED
                || state == State.DISABLED_ORDERED
                ? State.DISABLED_ORDERED : State.DISABLED;
        return state;
    }

    public String getToBurzaUrlAdd() {
        if (disabled) return null;
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
                && ((MonthLunch) o).state.equals(state);
    }

    public enum State {
        ENABLED, DISABLED, DISABLED_ORDERED, ORDERED, UNKNOWN;

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ENGLISH);
        }
    }

    public enum BurzaState {
        TO_BURZA, FROM_BURZA;

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
