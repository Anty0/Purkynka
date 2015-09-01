package cz.anty.utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

/**
 * Created by anty on 28.8.15.
 *
 * @author anty
 */
public interface FirstStartPage {

    void initialize(Activity activity);

    boolean showThisPage(Context context);

    String getTitle(Context context);

    int getButSkipVisibility(Context context);

    int getButNextVisibility(Context context);

    String getButSkipText(Context context);

    String getButNextText(Context context);

    void doUpdate(Context context, LayoutInflater layoutInflater, FrameLayout contentFrameLayout);

    boolean doSkip(Context context);

    boolean doFinish(Context context);
}
