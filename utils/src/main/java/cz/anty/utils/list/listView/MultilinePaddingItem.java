package cz.anty.utils.list.listView;

import android.content.Context;

/**
 * Created by anty on 10.10.15.
 *
 * @author anty
 */
public interface MultilinePaddingItem extends MultilineItem {

    boolean usePadding(Context context, int position);
}
