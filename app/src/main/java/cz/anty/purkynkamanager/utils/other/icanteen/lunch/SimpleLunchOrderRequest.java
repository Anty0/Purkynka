package cz.anty.purkynkamanager.utils.other.icanteen.lunch;

import android.content.Context;

import cz.anty.purkynkamanager.utils.other.Log;

/**
 * Created by anty on 22.11.2015.
 *
 * @author anty
 */
public abstract class SimpleLunchOrderRequest implements LunchOrderRequest {

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
