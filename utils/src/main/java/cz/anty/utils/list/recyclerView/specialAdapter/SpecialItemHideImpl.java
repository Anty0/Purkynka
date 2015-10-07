package cz.anty.utils.list.recyclerView.specialAdapter;

/**
 * Created by anty on 4.10.15.
 *
 * @author anty
 */
public abstract class SpecialItemHideImpl implements SpecialItem {

    boolean mVisible = true;

    @Override
    public void onHideClick() {
        mVisible = false;
    }

    @Override
    public boolean isShowHideButton() {
        return true;
    }

    @Override
    public boolean isVisible() {
        return mVisible;
    }

    public void setVisible(boolean hidden) {
        this.mVisible = hidden;
    }
}
