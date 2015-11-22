package cz.anty.purkynkamanager.utils.other.icanteen.lunch;

import android.content.Context;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;

/**
 * Created by anty on 22.11.2015.
 *
 * @author anty
 */
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
