package cz.anty.utils.list.recyclerView.specialAdapter;

import android.widget.FrameLayout;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public interface SpecialItem {

    void onCreateViewHolder(FrameLayout parent, int itemPosition);

    void onBindViewHolder(int itemPosition);

    void onClick();

    void onLongClick();

    void onHideClick();

    boolean isShowHideButton();

    int getPriority();
}
