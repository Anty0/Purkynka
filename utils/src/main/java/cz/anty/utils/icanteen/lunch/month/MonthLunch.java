package cz.anty.utils.icanteen.lunch.month;

import java.util.Locale;

/**
 * Created by anty on 24.8.15.
 *
 * @author anty
 */
public class MonthLunch {

    private final String name, orderUrlAdd;
    private final State state;

    public MonthLunch(String name, String orderUrlAdd, State state) {//TODO add map alergeny
        this.name = name;
        this.orderUrlAdd = orderUrlAdd;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public State getState() {
        return state;
    }

    public String getOrderUrlAdd() {
        return orderUrlAdd;
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
            /*switch (this) {
                case UNKNOWN:
                    return "unknown";
                case ORDERED:
                    return "ordered";
                case DISABLED:
                    return "disabled";
                case ENABLED:
                default:
                    return "enabled";
            }*/
        }
    }
}
