package cz.anty.purkynkamanager.firststart;

import android.app.Activity;

import cz.anty.utils.FirstStartPage;

/**
 * Created by anty on 30.8.15.
 *
 * @author anty
 */
class PagesManager {

    private final Activity activity;
    private final FirstStartPage[] firstStartPages;
    private int page = 0;

    PagesManager(Activity activity, FirstStartPage[] firstStartPages) {
        this.activity = activity;
        this.firstStartPages = firstStartPages;
        for (FirstStartPage page : this.firstStartPages)
            page.initialize(activity);
    }

    public void next() {
        do {
            page++;
        } while (page < firstStartPages.length
                && !firstStartPages[page].showThisPage(activity));
    }

    public FirstStartPage get() {
        return page < firstStartPages.length ? firstStartPages[page] : null;
    }
}
