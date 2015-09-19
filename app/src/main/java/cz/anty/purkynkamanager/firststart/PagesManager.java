package cz.anty.purkynkamanager.firststart;

import cz.anty.utils.FirstStartPage;

/**
 * Created by anty on 30.8.15.
 *
 * @author anty
 */
class PagesManager {

    private static int page = -1;
    private final FirstStartPage[] firstStartPages;

    PagesManager(FirstStartPage[] firstStartPages) {
        this.firstStartPages = firstStartPages;
        if (page == -1)
            next();
    }

    public static void reset() {
        page = -1;
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
