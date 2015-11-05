package cz.anty.purkynkamanager.utils.special;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.attendance.TrackingActivity;
import cz.anty.purkynkamanager.modules.attendance.receiver.TrackingReceiver;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.attendance.man.Man;
import cz.anty.purkynkamanager.utils.other.attendance.man.TrackingMansManager;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.SpecialModule;

/**
 * Created by anty on 1.10.15.
 *
 * @author anty
 */
public class TrackingSpecialModule extends SpecialModule {

    private final TrackingSpecialItem[] mItems;

    public TrackingSpecialModule(Context context) {
        super(context);
        mItems = new TrackingSpecialItem[]{
                new TrackingSpecialItem()
        };
    }

    @Override
    protected boolean isInitOnThread() {
        return true;
    }

    @Override
    protected boolean isUpdateOnThread() {
        return true;
    }

    @Override
    protected boolean onInitialize(SharedPreferences preferences) {
        if (TrackingActivity.mansManager == null) {
            TrackingActivity.mansManager = new TrackingMansManager(getContext());
        }
        refresh(preferences);
        return true;
    }

    @Override
    protected void onUpdate(SharedPreferences preferences) {
        refresh(preferences);
        notifyItemsChanged();
        //notifyItemsModified();
    }

    private void refresh(SharedPreferences preferences) {
        mItems[0].setEnabled(preferences
                .getBoolean(Constants.SETTING_NAME_ITEM_TRACKING, true));

        TrackingActivity.mansManager = TrackingReceiver
                .refreshTrackingMans(getContext(),
                        TrackingActivity.mansManager, true);

    }

    @Override
    protected void onSaveState(SharedPreferences.Editor preferences) {
        preferences.putBoolean(Constants.SETTING_NAME_ITEM_TRACKING,
                mItems[0].isEnabled());
    }

    @Override
    protected SpecialItem[] getItems() {
        return mItems;
    }

    @Override
    protected CharSequence getModuleName() {
        return getContext().getText(R.string.activity_title_tracking);
    }

    private class TrackingSpecialItem extends MultilineSpecialItem {

        private LinearLayout mansLinearLayout;

        public TrackingSpecialItem() {
            super(TrackingSpecialModule.this);
        }

        @Override
        public void onCreateViewHolder(FrameLayout parent, int itemPosition) {
            super.onCreateViewHolder(parent, itemPosition);

            mansLinearLayout = new LinearLayout(getContext());
            mansLinearLayout.setOrientation(LinearLayout.VERTICAL);
        }

        @Nullable
        @Override
        protected Integer getImageId() {
            return R.mipmap.ic_launcher_a;
        }

        @Nullable
        @Override
        public CharSequence getTitle() {
            return getContext().getText(R.string.activity_title_tracking);
        }

        @Nullable
        @Override
        protected View getContentView(ViewGroup parent) {
            mansLinearLayout.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (Man man : TrackingActivity.mansManager.get()) {
                View view = inflater.inflate(R.layout
                        .widget_list_item_multi_line_text, mansLinearLayout, false);
                ((TextView) view.findViewById(R.id.widget_text_view_title))
                        .setText(man.getTitle(getContext(), MultilineItem.NO_POSITION));
                ((TextView) view.findViewById(R.id.widget_text_view_text))
                        .setText(man.getText(getContext(), MultilineItem.NO_POSITION));
                mansLinearLayout.addView(view);
            }
            return mansLinearLayout;
        }

        @Override
        public void onClick() {
            getContext().startActivity(
                    new Intent(getContext(), TrackingActivity.class));
        }

        @Override
        public boolean isVisible() {
            return super.isVisible() && TrackingActivity.mansManager.get().length > 0;
        }

        @Override
        public int getPriority() {
            return Constants.SPECIAL_ITEM_PRIORITY_TRACKING;
        }
    }
}
