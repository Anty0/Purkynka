package cz.anty.purkynkamanager.utils.list.recyclerView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collection;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.Log;
import cz.anty.purkynkamanager.utils.Utils;
import cz.anty.purkynkamanager.utils.list.listView.MultilineItem;
import cz.anty.purkynkamanager.utils.list.listView.MultilinePaddingItem;

/**
 * Created by anty on 28.9.15.
 *
 * @author anty
 */
public class MultilineRecyclerAdapter<T extends MultilineItem>
        extends RecyclerAdapter<T, RecyclerAdapter.ItemViewHolder<T>> {

    private static final String LOG_TAG = "MultilineRecyclerAdapter";

    private static final int DEFAULT_LAYOUT_ID = R.layout.list_item_multi_line_text;

    public MultilineRecyclerAdapter(Context context) {
        super(context, DEFAULT_LAYOUT_ID);
    }

    public MultilineRecyclerAdapter(Context context, Collection<? extends T> data) {
        super(context, DEFAULT_LAYOUT_ID, data);
    }

    public MultilineRecyclerAdapter(Context context, T... data) {
        super(context, DEFAULT_LAYOUT_ID, data);
    }

    public MultilineRecyclerAdapter(Context context, int multilineLayoutResourceId) {
        super(context, multilineLayoutResourceId);
    }

    public MultilineRecyclerAdapter(Context context, int multilineLayoutResourceId, Collection<? extends T> data) {
        super(context, multilineLayoutResourceId, data);
    }

    public MultilineRecyclerAdapter(Context context, int multilineLayoutResourceId, T... data) {
        super(context, multilineLayoutResourceId, data);
    }

    @Override
    protected MultilineViewHolder<T> getViewHolderInstance(View view) {
        Log.d(LOG_TAG, "getViewHolderInstance");
        view.getLayoutParams().width =
                ViewGroup.LayoutParams.MATCH_PARENT;
        return new MultilineViewHolder<>(view);
    }

    public static class MultilineViewHolder<T extends MultilineItem> extends ItemViewHolder<T> {

        private static final String LOG_TAG = "MultilineViewHolder";

        private final Context context;
        private final TextView titleView, textView;

        public MultilineViewHolder(View itemView) {
            super(itemView);
            Log.d(LOG_TAG, "<init>");
            context = itemView.getContext();
            titleView = (TextView) itemView.findViewById(R.id.text_view_title);
            textView = (TextView) itemView.findViewById(R.id.text_view_text);
        }

        @Override
        protected void onBindViewHolder(T item, int position) {
            Log.d(LOG_TAG, "onBindViewHolder");
            titleView.setText(item.getTitle(context, position));
            CharSequence text = item.getText(context, position);
            if (text == null) {
                if (item instanceof MultilinePaddingItem
                        && !((MultilinePaddingItem) item)
                        .usePadding(context, position)) {
                    Utils.setPadding(titleView, 1, 1, 1, 1);
                } else Utils.setPadding(titleView, 1, 8, 1, 8);
                textView.setText("");
                textView.setVisibility(View.GONE);
            } else {
                Utils.setPadding(titleView, 1, 1, 1, 1);
                textView.setVisibility(View.VISIBLE);
                textView.setText(text);
            }
        }
    }
}
