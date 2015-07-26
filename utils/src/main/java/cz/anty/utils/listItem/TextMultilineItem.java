package cz.anty.utils.listItem;

import android.content.Context;

/**
 * Created by anty on 18.6.15.
 *
 * @author anty
 */
public class TextMultilineItem implements MultilineItem {

    private final String title;
    private final String text;

    public TextMultilineItem(String title, String text) {
        this.title = title;
        this.text = text;
    }

    @Override
    public String getTitle(Context context) {
        return title;
    }

    @Override
    public String getText(Context context) {
        return text;
    }
}
