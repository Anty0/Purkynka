package cz.anty.utils.listItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import cz.anty.utils.R;

/**
 * Created by anty on 18.6.15.
 *
 * @author anty
 */
public class MultilineAdapter extends ArrayAdapter<MultilineItem> {

    private final Context context;
    private final int layoutResourceId;

    public MultilineAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
    }

    public MultilineAdapter(Context context, int layoutResourceId, MultilineItem[] data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MultilineItem item = getItem(position);
        Integer layoutResourceId = item.getLayoutResourceId(context);
        layoutResourceId = layoutResourceId == null ?
                this.layoutResourceId : layoutResourceId;

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
            holder.text1 = (TextView) convertView.findViewById(R.id.txtTitle);
            holder.text2 = (TextView) convertView.findViewById(R.id.txtTitle2);
            holder.layoutResourceId = layoutResourceId;
            //holder.imgIcon = (ImageView)convertView.findViewById(R.id.imgIcon);

            convertView.setTag(holder);
        }

        holder = (ItemDataHolder) convertView.getTag();
        holder.text1.setText(item.getTitle(context));
        String text = item.getText(context);
        if (text == null) {
            holder.text1.setPadding(1, 8, 1, 8);
            holder.text2.setVisibility(View.GONE);
        } else {
            holder.text1.setPadding(1, 1, 1, 1);
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
