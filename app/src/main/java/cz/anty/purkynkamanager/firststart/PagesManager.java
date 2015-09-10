package cz.anty.purkynkamanager.firststart;

import cz.anty.utils.FirstStartPage;

/**
 * Created by anty on 30.8.15.
 *
 * @author anty
 */
class PagesManager {

    private final FirstStartPage[] firstStartPages;
    private int page = 0;

    PagesManager(FirstStartPage[] firstStartPages) {
        this.firstStartPages = firstStartPages;
        for (FirstStartPage page : firstStartPages) {
            if (!(page instanceof WelcomeFirstStartPage) && page.showThisPage()) {
                return;
            }
        }
        page = firstStartPages.length;
    }

    public void next() {
        do {
            page++;
        } while (page < firstStartPages.length
                && !firstStartPages[page].showThisPage());
    }

    public FirstStartPage get() {
        return page < firstStartPages.length ? firstStartPages[page] : null;
    }
}
