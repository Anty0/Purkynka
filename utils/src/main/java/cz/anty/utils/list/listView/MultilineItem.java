package cz.anty.utils.list.listView;

import android.content.Context;

/**
 * Created by anty on 18.6.15.
 *
 * @author anty
 */
public interface MultilineItem {

    CharSequence getTitle(Context context, int position);

    CharSequence getText(Context context, int position);

}
