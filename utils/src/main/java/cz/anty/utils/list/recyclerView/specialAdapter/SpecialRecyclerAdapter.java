package cz.anty.utils.list.recyclerView.specialAdapter;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.util.Collection;
import java.util.Comparator;

import cz.anty.utils.Log;
import cz.anty.utils.R;
import cz.anty.utils.list.recyclerView.RecyclerAdapter;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public class SpecialRecyclerAdapter<T extends SpecialItem> extends RecyclerAdapter<T, SpecialRecyclerAdapter.SpecialItemViewHolder<T>> {

    private static final int LAYOUT_RESOURCE_ID = R.layout.special_item_layout;
    private final Comparator<T> comparator = new Comparator<T>() {
        @Override
        public int compare(T lhs, T rhs) {
            return rhs.getPriority() - lhs.getPriority();
        }
    };

    public SpecialRecyclerAdapter() {
        super(LAYOUT_RESOURCE_ID);
    }

    public SpecialRecyclerAdapter(Collection<? extends T> data) {
        super(LAYOUT_RESOURCE_ID, data);
    }

    public SpecialRecyclerAdapter(T... data) {
        super(LAYOUT_RESOURCE_ID, data);
    }

    @Override
    protected void onDataSetChanged() {
        setNotifyOnChange(false);
        sortItems(comparator);
        setNotifyOnChange(true);
        notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    protected SpecialItemViewHolder<T> getViewHolderInstance(View view) {
        return new SpecialItemViewHolder<>(view);
    }

    public static class SpecialItemViewHolder<I extends SpecialItem> extends RecyclerAdapter.ItemViewHolder<I> implements View.OnClickListener, View.OnLongClickListener {

        private final FrameLayout content;
        private final ImageButton hideButton;
        private I mItem = null;

        public SpecialItemViewHolder(View itemView) {
            super(itemView);
            Log.d(getClass().getSimpleName(), "<init>");
            content = (FrameLayout) itemView
                    .findViewById(R.id.content_frame_layout);
            hideButton = (ImageButton) itemView
                    .findViewById(R.id.hide_image_button);
            content.setOnClickListener(this);
            content.setOnLongClickListener(this);
            hideButton.setOnClickListener(this);

        }

        @Override
        protected synchronized void onBindViewHolder(I item, int position) {
            if (mItem != item) {
                content.removeAllViews();
                mItem = item;
                mItem.onCreateViewHolder(content, position);
            }
            hideButton.setVisibility(mItem.isShowHideButton()
                    ? View.VISIBLE : View.GONE);
            mItem.onBindViewHolder(position);
        }

        @Override
        public void onClick(View v) {
            if (mItem != null)
                if (v == hideButton) mItem.onHideClick();
                else mItem.onClick();
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
