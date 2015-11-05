package cz.anty.purkynkamanager.utils.other.list.items;

import android.content.Context;

/**
 * Created by anty on 18.6.15.
 *
 * @author anty
 */
public interface MultilineItem {

    int NO_POSITION = -1;

    CharSequence getTitle(Context context, int position);

    CharSequence getText(Context context, int position);

}
