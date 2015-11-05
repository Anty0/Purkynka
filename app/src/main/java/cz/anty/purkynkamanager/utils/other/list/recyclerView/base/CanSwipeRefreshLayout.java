package cz.anty.purkynkamanager.utils.other.list.recyclerView.base;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

/**
 * Created by anty on 05.11.2015.
 *
 * @author anty
 */
public class CanSwipeRefreshLayout extends SwipeRefreshLayout {

    public CanSwipeRefreshLayout(Context context) {
        super(context);
    }

    public CanSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canChildScrollUp() {
        return false;
    }
}
