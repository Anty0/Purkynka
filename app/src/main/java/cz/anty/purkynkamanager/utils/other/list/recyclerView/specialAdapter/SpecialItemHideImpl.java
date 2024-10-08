package cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter;

/**
 * Created by anty on 4.10.15.
 *
 * @author anty
 */
public abstract class SpecialItemHideImpl extends SpecialItem {

    private final SpecialModule mModule;
    private boolean mEnabled = true;

    public SpecialItemHideImpl(SpecialModule module) {
        mModule = module;
    }

    @Override
    public void onHideClick(boolean hide) {
        setEnabled(!hide);
    }

    @Override
    public boolean isShowHideButton() {
        return true;
    }

    @Override
    public boolean isVisible() {
        return mEnabled;
    }

    public final boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        boolean changed = mEnabled != enabled;
        /*boolean remove = mEnabled && !enabled;
        boolean add = !mEnabled && enabled;*/
        if (changed) {
            this.mEnabled = enabled;
            mModule.notifyItemsChanged();
        }
        /*if (remove) {
            mModule.notifyItemRemoved(this);
            return;
        }
        if (add) mModule.notifyItemAdded(this);*/
    }
}
