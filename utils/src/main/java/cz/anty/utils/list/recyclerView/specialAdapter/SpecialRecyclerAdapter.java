package cz.anty.utils.list.recyclerView.specialAdapter;

import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.util.ArrayList;
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
public class SpecialRecyclerAdapter extends RecyclerAdapter<SpecialItem, SpecialRecyclerAdapter.SpecialItemViewHolder> {

    private static final int LAYOUT_RESOURCE_ID = R.layout.special_item_layout;
    private static final ArrayList<SpecialItem> visibleItems = new ArrayList<>();
    private final Comparator<SpecialItem> comparator = new Comparator<SpecialItem>() {
        @Override
        public int compare(SpecialItem lhs, SpecialItem rhs) {
            return rhs.getPriority() - lhs.getPriority();
        }
    };

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
    protected void onDataSetChanged() {
        setNotifyOnChange(false);
        sortItems(comparator);
        setNotifyOnChange(true);

        visibleItems.clear();
        for (SpecialItem item : getItems(new
                SpecialItem[super.getItemCount()])) {
            if (item.isVisible()) visibleItems.add(item);
        }
        notifyDataSetChanged();
        //notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    protected SpecialItemViewHolder getViewHolderInstance(View view) {
        return new SpecialItemViewHolder(view, this);
    }

    @Override
    public SpecialItem getItem(int position) {
        return visibleItems.get(position);
    }

    @Override
    public int getItemCount() {
        return visibleItems.size();
    }

    @Override
    public int getItemPosition(SpecialItem item) {
        return visibleItems.indexOf(item);
    }

    public static class SpecialItemViewHolder extends RecyclerAdapter.ItemViewHolder<SpecialItem>
            implements View.OnClickListener, View.OnLongClickListener {

        private final FrameLayout mContent;
        private final ImageButton mHideButton;

        private final SpecialRecyclerAdapter mAdapter;
        private SpecialItem mItem = null;
        //private int mPosition;

        public SpecialItemViewHolder(View itemView, SpecialRecyclerAdapter adapter) {
            super(itemView);
            Log.d(getClass().getSimpleName(), "<init>");
            mAdapter = adapter;

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
                boolean visible = mItem.isVisible();
                if (v == mHideButton) mItem.onHideClick();
                else mItem.onClick();
                if (visible && !mItem.isVisible()) {
                    //mAdapter.notifyItemChanged(mPosition);
                    AnimationSet animation = new AnimationSet(true);
                    animation.addAnimation(new TranslateAnimation(0, itemView.getWidth() * 1.2f, 0, 0));
                    animation.addAnimation(new AlphaAnimation(1, 0));
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            itemView.clearAnimation();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(75);
                                    } catch (InterruptedException e) {
                                        Log.d(getClass().getSimpleName(),
                                                "onAnimationEnd", e);
                                    }
                                    new Handler(itemView.getContext().getMainLooper())
                                            .post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mAdapter.onDataSetChanged();
                                                }
                                            });
                                }
                            }).start();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    animation.setDuration(125);
                    itemView.setAnimation(animation);
                    animation.startNow();
                    itemView.invalidate();
                    return;
                }
                mAdapter.onDataSetChanged();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mItem != null) {
                mItem.onLongClick();
                //mAdapter.notifyItemChanged(mPosition);
                mAdapter.onDataSetChanged();
                return true;
            }
            return false;
        }
    }
}
