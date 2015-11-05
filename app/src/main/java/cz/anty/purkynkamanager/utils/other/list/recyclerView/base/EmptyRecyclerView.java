package cz.anty.purkynkamanager.utils.other.list.recyclerView.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by anty on 02.11.2015.
 *
 * @author anty
 */
public class EmptyRecyclerView extends RecyclerView {

    private View mEmptyView, mFrame;
    final private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            updateEmptyView();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            updateEmptyView();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            updateEmptyView();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            updateEmptyView();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            updateEmptyView();
        }
    };

    public EmptyRecyclerView(Context context) {
        super(context);
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void updateEmptyView() {
        if (mEmptyView != null) {
            Adapter adapter = getAdapter();
            final boolean emptyViewVisible = adapter == null
                    || adapter.getItemCount() == 0;
            mEmptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
            setVisibility(emptyViewVisible ? GONE : VISIBLE);
        } else setVisibility(VISIBLE);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final RecyclerView.Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }

        updateEmptyView();
    }

    @Override
    public void setVisibility(int visibility) {
        if (mFrame == null) {
            super.setVisibility(visibility);
            return;
        }
        mFrame.setVisibility(visibility);
        super.setVisibility(VISIBLE);
    }

    public void setFrame(View frame) {
        if (mFrame != null)
            mFrame.setVisibility(VISIBLE);
        mFrame = frame;
        updateEmptyView();
    }

    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
        updateEmptyView();
    }
}