package cz.anty.purkynkamanager.utils.other.list.items;

import android.content.Context;
import android.support.annotation.DrawableRes;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public interface MultilineImageItem extends MultilineItem {

    @DrawableRes
    int getImageResourceId(Context context, int position);
}
