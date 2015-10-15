package cz.anty.purkynkamanager.utils.list.listView;

import android.content.Context;

/**
 * Created by anty on 18.6.15.
 *
 * @author anty
 */
public class TextMultilineItem implements MultilinePaddingItem {

    private CharSequence title, text;
    private boolean padding = true;
    private Object tag;

    public TextMultilineItem(CharSequence title, CharSequence text) {
        this.title = title;
        this.text = text;
    }

    public TextMultilineItem(CharSequence title, CharSequence text, boolean usePadding) {
        this.title = title;
        this.text = text;
        this.padding = usePadding;
    }

    @Override
    public CharSequence getTitle(Context context, int position) {
        return title;
    }

    @Override
    public CharSequence getText(Context context, int position) {
        return text;
    }

    @Override
    public boolean usePadding(Context context, int position) {
        return padding;
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

    public boolean usePadding() {
        return padding;
    }

    public void setPadding(boolean padding) {
        this.padding = padding;
    }

    public Object getTag() {
        return tag;
    }

    public TextMultilineItem setTag(Object tag) {
        this.tag = tag;
        return this;
    }
}
