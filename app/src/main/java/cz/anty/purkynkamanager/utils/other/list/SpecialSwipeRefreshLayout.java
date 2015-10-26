package cz.anty.purkynkamanager.utils.other.list;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

/**
 * Created by anty on 25.10.15.
 *
 * @author anty
 */
public class SpecialSwipeRefreshLayout extends SwipeRefreshLayout {

    private static final String LOG_TAG = "SpecialSwipeRefreshLayout";

    private final ArrayList<CanChildScrollUpListener> mListeners = new ArrayList<>();
    private int extraMotionState = MotionEvent.ACTION_UP;
    private float lastTouchEventY = Float.NaN;
    //private boolean lastTouchState = false;
    private boolean cantScroll = false;
    private boolean lastScrollState = false;

    public SpecialSwipeRefreshLayout(Context context) {
        super(context);
    }

    public SpecialSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addCanChildScrollUpListener(CanChildScrollUpListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    @Override
    public boolean canChildScrollUp() {
        if (cantScroll) return false;
        synchronized (mListeners) {
            if (mListeners.size() == 0)
                return super.canChildScrollUp();

            for (CanChildScrollUpListener listener : mListeners) {
                if (!listener.canChildScrollUp()) return false;
            }
        }
        return true;
    }

    private void sendExtraTouchEvent(MotionEvent ev) {
        synchronized (mListeners) {
            for (CanChildScrollUpListener listener : mListeners) {
                listener.onExtraTouchEvent(ev);
            }
        }
        extraMotionState = ev.getAction();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        /*Log.d(LOG_TAG, "onTouchEvent lastEventY: " + (lastTouchEvent == null ? "null"
                : lastTouchEvent.getY()) + " actualEventY: " + ev.getY());*/
        if (!isEnabled()) return super.onTouchEvent(ev);
        cantScroll = true;
        boolean toReturn = super.onTouchEvent(ev);
        if (toReturn) {
            /*if (!lastTouchState &&
                    ev.getAction() != MotionEvent.ACTION_UP) {
                int action = ev.getAction();
                ev.setAction(MotionEvent.ACTION_UP);
                sendExtraTouchEvent(ev);
                ev.setAction(action);
            }*/
            int action = ev.getAction();
            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN) {
                sendExtraTouchEvent(ev);
            } else {
                if (lastTouchEventY != Float.NaN &&
                        lastTouchEventY < ev.getY()) {
                    if (extraMotionState != MotionEvent.ACTION_DOWN &&
                            extraMotionState != MotionEvent.ACTION_MOVE) {
                        ev.setAction(MotionEvent.ACTION_DOWN);
                        sendExtraTouchEvent(ev);
                        ev.setAction(action);
                    }
                    sendExtraTouchEvent(ev);
                } else {
                    if (extraMotionState != MotionEvent.ACTION_UP) {
                        ev.setAction(MotionEvent.ACTION_UP);
                        sendExtraTouchEvent(ev);
                        ev.setAction(action);
                    }
                }
            }
        } else {
            /*if (lastTouchState &&
                    ev.getAction() != MotionEvent.ACTION_DOWN) {
                int action = ev.getAction();
                ev.setAction(MotionEvent.ACTION_DOWN);
                sendExtraTouchEvent(ev);
                ev.setAction(action);
            }*/
            int action = ev.getAction();
            if (action == MotionEvent.ACTION_DOWN &&
                    extraMotionState != MotionEvent.ACTION_UP) {
                ev.setAction(MotionEvent.ACTION_UP);
                sendExtraTouchEvent(ev);
                ev.setAction(action);
            }
            if (action != MotionEvent.ACTION_DOWN &&
                    extraMotionState != MotionEvent.ACTION_DOWN &&
                    extraMotionState != MotionEvent.ACTION_MOVE) {
                ev.setAction(MotionEvent.ACTION_DOWN);
                sendExtraTouchEvent(ev);
                ev.setAction(action);
            }
            sendExtraTouchEvent(ev);
        }
        //lastTouchState = toReturn;
        lastTouchEventY = ev.getY();
        return toReturn;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) return super.onInterceptTouchEvent(ev);
        cantScroll = false;
        boolean actualState = canChildScrollUp();
        int action = ev.getAction();
        if (lastScrollState && !actualState && action != MotionEvent.ACTION_DOWN) {
            ev.setAction(MotionEvent.ACTION_DOWN);
            super.onInterceptTouchEvent(ev);
            ev.setAction(action);
        }
        lastScrollState = actualState;
        return super.onInterceptTouchEvent(ev);
    }

    public interface CanChildScrollUpListener {

        boolean canChildScrollUp();

        void onExtraTouchEvent(MotionEvent ev);
    }
}
