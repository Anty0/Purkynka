package cz.anty.utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    View getView(Context context, LayoutInflater layoutInflater, ViewGroup rootView);

    boolean doSkip(Context context);

    boolean doFinish(Context context);
}
