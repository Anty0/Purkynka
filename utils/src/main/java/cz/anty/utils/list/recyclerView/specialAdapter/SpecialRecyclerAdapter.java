package cz.anty.utils.list.recyclerView.specialAdapter;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.util.Collection;

import cz.anty.utils.Log;
import cz.anty.utils.R;
import cz.anty.utils.list.recyclerView.RecyclerAdapter;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public class SpecialRecyclerAdapter extends RecyclerAdapter<SpecialItem, SpecialRecyclerAdapter.SpecialItemViewHolder> {

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

        private SpecialItem mItem = null;
        //private int mPosition;

        public SpecialItemViewHolder(View itemView) {
            super(itemView);
            Log.d(getClass().getSimpleName(), "<init>");

            mContent = (FrameLayout) itemView
                    .findViewById(R.id.content_frame_layout);
            mHideButton = (ImageButton) itemView
                    .findViewById(R.id.hide_image_button);
            mContent.setOnClickListener(this);
            mContent.setOnLongClickListener(this);
            mHideButton.setOnClickListener(this);

        }

        @Override
        protected synchronized void onBindViewHolder(SpecialItem item, int position) {
            //mPosition = position;
            if (mItem != item) {
                mContent.removeAllViews();
                mItem = item;
                mItem.onCreateViewHolder(mContent, position);
            }
            mHideButton.setVisibility(mItem.isShowHideButton()
                    ? View.VISIBLE : View.GONE);
            mItem.onBindViewHolder(position);
            /*mItemView.setVisibility(mItem.isVisible()
                    ? View.VISIBLE : View.GONE);*/
        }

        @Override
        public void onClick(View v) {
            if (mItem != null) {
                if (v == mHideButton) mItem.onHideClick();
                else mItem.onClick();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mItem != null) {
                mItem.onLongClick();
                return true;
            }
            return false;
        }
    }
}
