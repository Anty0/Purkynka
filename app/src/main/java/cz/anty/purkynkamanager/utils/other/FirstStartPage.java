package cz.anty.purkynkamanager.utils.other;

import android.content.Context;
import android.support.annotation.StyleRes;
import android.view.View;
import android.view.ViewGroup;

import cz.anty.purkynkamanager.R;

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

    public boolean showThisPage() {
        return true;
    }

    public abstract CharSequence getTitle();

    @StyleRes
    public int getTheme() {
        return R.style.AppTheme;
    }

    public int getButSkipVisibility() {
        return View.VISIBLE;
    }

    public int getButNextVisibility() {
        return View.VISIBLE;
    }

    public CharSequence getButSkipText() {
        return getContext().getText(R.string.but_skip);
    }

    public CharSequence getButNextText() {
        return getContext().getText(R.string.but_next);
    }

    public abstract View getView(ViewGroup rootView);

    public boolean doSkip() {
        return true;
    }

    public boolean doFinish() {
        return true;
    }
}
