package cz.anty.utils.list.listView;

import android.content.Context;

/**
 * Created by anty on 18.6.15.
 *
 * @author anty
 */
public interface MultilineItem {

    String getTitle(Context context, int position);

    String getText(Context context, int position);

    Integer getLayoutResourceId(Context context, int position);

}
