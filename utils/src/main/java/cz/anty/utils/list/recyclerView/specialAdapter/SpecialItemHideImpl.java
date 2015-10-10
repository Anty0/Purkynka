package cz.anty.utils.list.recyclerView.specialAdapter;

/**
 * Created by anty on 4.10.15.
 *
 * @author anty
 */
public abstract class SpecialItemHideImpl extends SpecialItem {

    private final SpecialModule mModule;
    private boolean mVisible = true;

    public SpecialItemHideImpl(SpecialModule module) {
        mModule = module;
    }

    @Override
    public void onHideClick() {
        setVisible(false);
    }

    @Override
    public boolean isShowHideButton() {
        return true;
    }

    @Override
    public boolean isVisible() {
        return mVisible;
    }

    public void setVisible(boolean visible) {
        boolean remove = mVisible && !visible;
        this.mVisible = visible;
        if (remove)
            mModule.notifyItemRemoved(this);
    }
}
