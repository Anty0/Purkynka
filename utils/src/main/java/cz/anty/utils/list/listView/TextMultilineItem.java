package cz.anty.utils.list.listView;

import android.content.Context;

/**
 * Created by anty on 18.6.15.
 *
 * @author anty
 */
public class TextMultilineItem implements MultilineItem {

    private final String title;
    private final String text;
    private Object tag;

    public TextMultilineItem(String title, String text) {
        this.title = title;
        this.text = text;
    }

    @Override
    public String getTitle(Context context, int position) {
        return title;
    }

    @Override
    public String getText(Context context, int position) {
        return text;
    }

    public Object getTag() {
        return tag;
    }

    public TextMultilineItem setTag(Object tag) {
        this.tag = tag;
        return this;
    }
}
