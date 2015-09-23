package cz.anty.purkynkamanager.firststart;

import android.os.Bundle;

import cz.anty.utils.FirstStartPage;

/**
 * Created by anty on 30.8.15.
 *
 * @author anty
 */
class PagesManager {

    private static final String SAVE_PAGE_KEY = "FIRST_START_PAGE";
    private final FirstStartPage[] firstStartPages;
    private int page = -1;

    PagesManager(Bundle savedInstanceState, FirstStartPage[] firstStartPages) {
        this.firstStartPages = firstStartPages;
        page = savedInstanceState.getInt(SAVE_PAGE_KEY, 0) - 1;
        next();
    }

    public synchronized void save(Bundle outState) {
        outState.putInt(SAVE_PAGE_KEY, page);
    }

    public synchronized void next() {
        do {
            page++;
        } while (page < firstStartPages.length
                && !firstStartPages[page].showThisPage());
    }

    public synchronized FirstStartPage get() {
        return page < firstStartPages.length ? firstStartPages[page] : null;
    }
}
