package cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter;

import android.widget.FrameLayout;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public abstract class SpecialItem {

    protected abstract void onCreateViewHolder(FrameLayout parent, int itemPosition);

    protected abstract void onBindViewHolder(int itemPosition);

    protected void onClick() {

    }

    protected boolean onLongClick() {
        return false;
    }

    protected void onHideClick(boolean hide) {

    }

    public boolean isShowHideButton() {
        return false;
    }

    public boolean isVisible() {
        return true;
    }

    protected abstract int getPriority();
}
