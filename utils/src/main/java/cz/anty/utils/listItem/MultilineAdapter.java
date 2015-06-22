package cz.anty.utils.listItem;

import android.app.Activity;
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
        this.layoutResourceId = layoutResourceId;
        this.context = context;
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemDataHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            //row.setMinimumHeight(200);
            holder = new ItemDataHolder();
            // holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            holder.text1 = (TextView) row.findViewById(R.id.txtTitle);
            holder.text2 = (TextView) row.findViewById(R.id.txtTitle2);

            row.setTag(holder);
        } else holder = (ItemDataHolder) row.getTag();

        MultilineItem item = getItem(position);
        holder.text1.setText(item.getTitle());
        holder.text2.setText(item.getText());
        //    holder.imgIcon.setImageResource(item.icon);

        return row;
    }

    static class ItemDataHolder {
        //   ImageView imgIcon;
        TextView text1;
        TextView text2;
        //    ImageView imgIcon2;
    }

}
