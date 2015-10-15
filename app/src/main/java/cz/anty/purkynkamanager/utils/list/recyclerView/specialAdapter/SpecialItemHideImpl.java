package cz.anty.purkynkamanager.utils.list.recyclerView.specialAdapter;

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
        boolean remove = mEnabled && !enabled;
        boolean changed = mEnabled != enabled;
        this.mEnabled = enabled;
        if (remove) {
            mModule.notifyItemRemoved(this);
            return;
        }
        if (changed) mModule.notifyItemsChanged();
    }
}
