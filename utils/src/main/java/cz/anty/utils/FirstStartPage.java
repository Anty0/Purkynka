package cz.anty.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by anty on 28.8.15.
 *
 * @author anty
 */
public abstract class FirstStartPage {

    private final Context context;

    protected FirstStartPage(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return context;
    }

    public abstract boolean showThisPage();

    public abstract CharSequence getTitle();

    public abstract int getButSkipVisibility();

    public abstract int getButNextVisibility();

    public abstract CharSequence getButSkipText();

    public abstract CharSequence getButNextText();

    public abstract View getView(ViewGroup rootView);

    public abstract boolean doSkip();

    public abstract boolean doFinish();
}
