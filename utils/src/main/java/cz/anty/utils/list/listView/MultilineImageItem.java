package cz.anty.utils.list.listView;

import android.content.Context;
import android.support.annotation.DrawableRes;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public interface MultilineImageItem {

    @DrawableRes
    int getImageResourceId(Context context, int position);
}
