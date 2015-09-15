package cz.anty.utils.icanteen.lunch.month;

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
        return orderUrlAdd;
    }

    public State getState() {
        return state;
    }

    public String getToBurzaUrlAdd() {
        return toBurzaUrlAdd;
    }

    public BurzaState getBurzaState() {
        return burzaState;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) || o instanceof MonthLunch
                && ((MonthLunch) o).getName().equals(getName())
                && ((MonthLunch) o).getState().equals(getState())
                && ((MonthLunch) o).getOrderUrlAdd().equals(getOrderUrlAdd());
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
