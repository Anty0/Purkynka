package cz.anty.attendancemanager;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import cz.anty.attendancemanager.receiver.TrackingReceiver;
import cz.anty.utils.Constants;
import cz.anty.utils.attendance.man.Man;
import cz.anty.utils.attendance.man.TrackingMansManager;
import cz.anty.utils.list.listView.MultilineItem;
import cz.anty.utils.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialModule;

/**
 * Created by anty on 1.10.15.
 *
 * @author anty
 */
public class TrackingSpecialModule extends SpecialModule {

    private final SpecialItem[] mItems;

    public TrackingSpecialModule(Context context) {
        super(context);
        mItems = new SpecialItem[]{
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
    protected boolean onInitialize() {
        if (TrackingActivity.mansManager == null) {
            TrackingActivity.mansManager = new TrackingMansManager(getContext());
        }
        onUpdate();
        return true;
    }

    @Override
    protected void onUpdate() {
        TrackingActivity.mansManager = TrackingReceiver
                .refreshTrackingMans(getContext(),
                        TrackingActivity.mansManager, true);
        notifyItemsChanged();
        //notifyItemsModified();
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
                        .text_widget_multi_line_list_item, mansLinearLayout, false);
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
