package cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.util.Collection;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.RecyclerAdapter;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public class SpecialRecyclerAdapter extends RecyclerAdapter<SpecialItem,
        SpecialRecyclerAdapter.SpecialItemViewHolder> {

    private static final String LOG_TAG = "SpecialRecyclerAdapter";

    private static final int LAYOUT_RESOURCE_ID = R.layout.special_item_layout;

    public SpecialRecyclerAdapter() {
        super(LAYOUT_RESOURCE_ID);
    }

    public SpecialRecyclerAdapter(Collection<? extends SpecialItem> data) {
        super(LAYOUT_RESOURCE_ID, data);
    }

    public SpecialRecyclerAdapter(SpecialItem... data) {
        super(LAYOUT_RESOURCE_ID, data);
    }

    @Override
    protected SpecialItemViewHolder getViewHolderInstance(View view) {
        return new SpecialItemViewHolder(view);
    }

    public static class SpecialItemViewHolder extends RecyclerAdapter.ItemViewHolder<SpecialItem>
            implements View.OnClickListener, View.OnLongClickListener {

        private final FrameLayout mContent;
        private final ImageButton mHideButton;
        private final ImageButton mRestoreButton;

        private SpecialItem mItem = null;
        //private int mPosition;

        public SpecialItemViewHolder(View itemView) {
            super(itemView);
            Log.d(LOG_TAG, "<init>");

            mContent = (FrameLayout) itemView
                    .findViewById(R.id.content_frame_layout);
            mHideButton = (ImageButton) itemView
                    .findViewById(R.id.hide_image_button);
            mRestoreButton = (ImageButton) itemView
                    .findViewById(R.id.restore_image_button);
            mContent.setOnClickListener(this);
            mContent.setOnLongClickListener(this);
            mHideButton.setOnClickListener(this);
            mRestoreButton.setOnClickListener(this);

        }

        @Override
        protected synchronized void onBindViewHolder(SpecialItem item, int position) {
            //mPosition = position;
            if (mItem != item) {
                mContent.removeAllViews();
                mItem = item;
                mItem.onCreateViewHolder(mContent, position);
            }
            if (mItem.isShowHideButton()) {
                boolean enabled = !(item instanceof SpecialItemHideImpl)
                        || ((SpecialItemHideImpl) item).isEnabled();
                mHideButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
                mRestoreButton.setVisibility(enabled ? View.GONE : View.VISIBLE);
            } else {
                mHideButton.setVisibility(View.GONE);
                mRestoreButton.setVisibility(View.GONE);
            }
            mItem.onBindViewHolder(position);
            /*mItemView.setVisibility(mItem.isVisible()
                    ? View.VISIBLE : View.GONE);*/
        }

        @Override
        public void onClick(View v) {
            if (mItem != null) {
                if (v == mContent) {
                    mItem.onClick();
                    return;
                }
                mItem.onHideClick(v == mHideButton);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return mItem != null && mItem.onLongClick();
        }
    }
}
