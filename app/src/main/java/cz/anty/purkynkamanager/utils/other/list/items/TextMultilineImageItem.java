package cz.anty.purkynkamanager.utils.other.list.items;

import android.content.Context;
import android.support.annotation.DrawableRes;

/**
 * Created by anty on 03.11.2015.
 *
 * @author anty
 */
public class TextMultilineImageItem extends TextMultilineItem implements MultilineImageItem {

    private int mImageResourceId;

    public TextMultilineImageItem() {
        super();
    }

    public TextMultilineImageItem(CharSequence title, CharSequence text,
                                  @DrawableRes int imageResourceId) {
        super(title, text);
        mImageResourceId = imageResourceId;
    }

    public TextMultilineImageItem(CharSequence title, CharSequence text,
                                  @DrawableRes int imageResourceId, boolean usePadding) {
        super(title, text, usePadding);
        mImageResourceId = imageResourceId;
    }

    public int getImageResourceId() {
        return mImageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.mImageResourceId = imageResourceId;
    }

    @Override
    public int getImageResourceId(Context context, int position) {
        return mImageResourceId;
    }
}
