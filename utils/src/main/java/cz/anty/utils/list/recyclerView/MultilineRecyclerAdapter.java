package cz.anty.utils.list.recyclerView;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.util.Collection;

import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.R;
import cz.anty.utils.list.listView.MultilineItem;
import cz.anty.utils.list.listView.MultilinePaddingItem;

/**
 * Created by anty on 28.9.15.
 *
 * @author anty
 */
public class MultilineRecyclerAdapter<T extends MultilineItem>
        extends RecyclerAdapter<T, RecyclerAdapter.ItemViewHolder<T>> {

    private static final int DEFAULT_LAYOUT_ID = R.layout.text_multi_line_list_item;

    public MultilineRecyclerAdapter() {
        super(DEFAULT_LAYOUT_ID);
    }

    public MultilineRecyclerAdapter(Collection<? extends T> data) {
        super(DEFAULT_LAYOUT_ID, data);
    }

    public MultilineRecyclerAdapter(T... data) {
        super(DEFAULT_LAYOUT_ID, data);
    }

    public MultilineRecyclerAdapter(int multilineLayoutResourceId) {
        super(multilineLayoutResourceId);
    }

    public MultilineRecyclerAdapter(int multilineLayoutResourceId, Collection<? extends T> data) {
        super(multilineLayoutResourceId, data);
    }

    public MultilineRecyclerAdapter(int multilineLayoutResourceId, T... data) {
        super(multilineLayoutResourceId, data);
    }

    @Override
    protected MultilineViewHolder<T> getViewHolderInstance(View view) {
        Log.d(getClass().getSimpleName(), "getViewHolderInstance");
        return new MultilineViewHolder<>(view);
    }

    public static class MultilineViewHolder<T extends MultilineItem> extends ItemViewHolder<T> {

        private final Context context;
        private final TextView titleView, textView;

        public MultilineViewHolder(View itemView) {
            super(itemView);
            Log.d(getClass().getSimpleName(), "<init>");
            context = itemView.getContext();
            titleView = (TextView) itemView.findViewById(R.id.text_view_title);
            textView = (TextView) itemView.findViewById(R.id.text_view_text);
        }

        @Override
        protected void onBindViewHolder(T item, int position) {
            Log.d(getClass().getSimpleName(), "onBindViewHolder");
            titleView.setText(item.getTitle(context, position));
            CharSequence text = item.getText(context, position);
            if (text == null) {
                if (item instanceof MultilinePaddingItem
                        && !((MultilinePaddingItem) item)
                        .usePadding(context, position)) {
                    Constants.setPadding(titleView, 1, 1, 1, 1);
                } else Constants.setPadding(titleView, 1, 8, 1, 8);
                textView.setText("");
                textView.setVisibility(View.GONE);
            } else {
                Constants.setPadding(titleView, 1, 1, 1, 1);
                textView.setVisibility(View.VISIBLE);
                textView.setText(text);
            }
        }
    }
}
