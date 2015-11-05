package cz.anty.purkynkamanager.utils.other.list.recyclerView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collection;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineImageItem;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;
import cz.anty.purkynkamanager.utils.other.list.items.MultilinePaddingItem;

/**
 * Created by anty on 28.9.15.
 *
 * @author anty
 */
public class MultilineRecyclerAdapter<T extends MultilineItem>
        extends RecyclerAdapter<T, RecyclerAdapter.ItemViewHolder<T>> {

    private static final String LOG_TAG = "MultilineRecyclerAdapter";
    private static final int DEFAULT_LAYOUT_ID = R.layout.list_item_multi_line_text;

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
        Log.d(LOG_TAG, "getViewHolderInstance");
        view.getLayoutParams().width =
                ViewGroup.LayoutParams.MATCH_PARENT;
        return new MultilineViewHolder<>(view);
    }

    public static class MultilineViewHolder<T extends MultilineItem> extends ItemViewHolder<T> {

        private static final String LOG_TAG = "MultilineViewHolder";

        private final Context context;
        private final TextView titleView, textView;
        private final ImageView imageView;

        public MultilineViewHolder(View itemView) {
            super(itemView);
            Log.d(LOG_TAG, "<init>");
            context = itemView.getContext();
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            titleView = (TextView) itemView.findViewById(R.id.text_view_title);
            textView = (TextView) itemView.findViewById(R.id.text_view_text);
        }

        @Override
        protected void onBindViewHolder(T item, int position) {
            Log.d(LOG_TAG, "onBindViewHolder");
            if (imageView != null) {
                if (item instanceof MultilineImageItem) {
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageResource(((MultilineImageItem) item)
                            .getImageResourceId(context, position));
                } else imageView.setVisibility(View.GONE);
            }
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
