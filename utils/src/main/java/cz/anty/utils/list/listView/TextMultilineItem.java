package cz.anty.utils.list.listView;

import android.content.Context;

/**
 * Created by anty on 18.6.15.
 *
 * @author anty
 */
public class TextMultilineItem implements MultilineItem {

    private CharSequence title, text;
    private Object tag;

    public TextMultilineItem(CharSequence title, CharSequence text) {
        this.title = title;
        this.text = text;
    }

    @Override
    public CharSequence getTitle(Context context, int position) {
        return title;
    }

    @Override
    public CharSequence getText(Context context, int position) {
        return text;
    }

    public CharSequence getTitle() {
        return title;
    }

    public void setTitle(CharSequence title) {
        this.title = title;
    }

    public CharSequence getText() {
        return text;
    }

    public void setText(CharSequence text) {
        this.text = text;
    }

    public Object getTag() {
        return tag;
    }

    public TextMultilineItem setTag(Object tag) {
        this.tag = tag;
        return this;
    }
}
