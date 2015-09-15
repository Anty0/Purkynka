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
    private final Integer layoutResourceId;
    private Object tag;

    public TextMultilineItem(String title, String text) {
        this(title, text, null);
    }

    public TextMultilineItem(String title, String text, Integer layoutResourceId) {
        this.title = title;
        this.text = text;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public String getTitle(Context context, int position) {
        return title;
    }

    @Override
    public String getText(Context context, int position) {
        return text;
    }

    @Override
    public Integer getLayoutResourceId(Context context, int position) {
        return layoutResourceId;
    }

    public Object getTag() {
        return tag;
    }

    public TextMultilineItem setTag(Object tag) {
        this.tag = tag;
        return this;
    }
}
