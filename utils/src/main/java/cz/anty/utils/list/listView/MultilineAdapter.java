package cz.anty.utils.list.listView;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import cz.anty.utils.Constants;
import cz.anty.utils.R;

/**
 * Created by anty on 18.6.15.
 *
 * @author anty
 */
public class MultilineAdapter<M extends MultilineItem> extends ArrayAdapter<M> {

    private final Context context;
    private final int layoutResourceId;

    public MultilineAdapter(Context context) {
        this(context, R.layout.text_multi_line_list_item);
    }

    public MultilineAdapter(Context context, @LayoutRes int layoutResourceId) {
        super(context, layoutResourceId);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
    }

    public MultilineAdapter(Context context, @LayoutRes int layoutResourceId, M[] data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return generateView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return generateView(position, convertView, parent);
    }

    protected View generateView(int position, View convertView, ViewGroup parent) {
        M item = getItem(position);
        Integer layoutResourceId = this.layoutResourceId;
        if (item instanceof MultilineResourceItem)
            layoutResourceId = ((MultilineResourceItem) item)
                    .getLayoutResourceId(context, position);

        if (convertView != null) {
            if (!((ItemDataHolder) convertView.getTag())
                    .layoutResourceId.equals(layoutResourceId))
                convertView = null;
        }

        ItemDataHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            //LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
            //convertView.setMinimumHeight(200);
            holder = new ItemDataHolder();
            holder.text1 = (TextView) convertView.findViewById(R.id.text_view_title);
            holder.text2 = (TextView) convertView.findViewById(R.id.text_view_text);
            holder.layoutResourceId = layoutResourceId;
            //holder.imgIcon = (ImageView)convertView.findViewById(R.id.imgIcon);

            convertView.setTag(holder);
        }

        holder = (ItemDataHolder) convertView.getTag();
        holder.text1.setText(item.getTitle(context, position));
        CharSequence text = item.getText(context, position);
        if (text == null) {
            if (item instanceof MultilinePaddingItem
                    && !((MultilinePaddingItem) item)
                    .usePadding(context, position)) {
                Constants.setPadding(holder.text1, 1, 1, 1, 1);
            } else Constants.setPadding(holder.text1, 1, 8, 1, 8);
            holder.text2.setVisibility(View.GONE);
        } else {
            Constants.setPadding(holder.text1, 1, 1, 1, 1);
            holder.text2.setVisibility(View.VISIBLE);
            holder.text2.setText(text);
            //holder.imgIcon.setImageResource(item.icon);
        }

        return convertView;
    }

    static class ItemDataHolder {
        //ImageView imgIcon;
        TextView text1;
        TextView text2;
        //ImageView imgIcon2;
        Integer layoutResourceId;
    }

}
