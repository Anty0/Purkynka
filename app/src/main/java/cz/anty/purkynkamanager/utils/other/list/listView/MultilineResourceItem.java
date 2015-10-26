package cz.anty.purkynkamanager.utils.other.list.listView;

import android.content.Context;
import android.support.annotation.LayoutRes;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public interface MultilineResourceItem extends MultilineItem {

    @LayoutRes
    int getLayoutResourceId(Context context, int position);
}
