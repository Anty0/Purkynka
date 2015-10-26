package cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Utils;

/**
 * Created by anty on 4.10.15.
 *
 * @author anty
 */
public abstract class MultilineSpecialItem extends SpecialItemHideImpl {

    private Context mContext;
    private TextView mTitle, mText;
    private ImageView mImage;
    private FrameLayout mContent;

    public MultilineSpecialItem(SpecialModule module) {
        super(module);
        mContext = module.getContext();
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public void onCreateViewHolder(FrameLayout parent, int itemPosition) {
        LinearLayout body = new LinearLayout(mContext);
        body.setOrientation(LinearLayout.VERTICAL);

        LayoutInflater.from(mContext).inflate(R.layout
                .base_list_item_multi_line_image_text, body);

        mContent = new FrameLayout(mContext);
        Utils.setPadding(mContent, 15, 3, 1, 0);
        body.addView(mContent);

        mImage = (ImageView) body.findViewById(R.id.image_view);
        mTitle = (TextView) body.findViewById(R.id.text_view_title);
        mText = (TextView) body.findViewById(R.id.text_view_text);

        parent.addView(body);
    }

    @Override
    public void onBindViewHolder(int itemPosition) {
        Integer imageId = getImageId();
        if (imageId == null) {
            mImage.setVisibility(View.GONE);
        } else {
            mImage.setVisibility(View.VISIBLE);
            mImage.setImageDrawable(Utils
                    .getDrawable(mContext, imageId));
        }

        updateTextView(mTitle, mText, getTitle());
        updateTextView(mText, mTitle, getText());

        mContent.removeAllViews();
        View content = getContentView(mContent);
        if (content == null) {
            mContent.setVisibility(View.GONE);
        } else {
            mContent.setVisibility(View.VISIBLE);
            mContent.addView(content);
        }
    }

    private void updateTextView(TextView toSet, TextView second, CharSequence text) {
        if (text == null) {
            toSet.setVisibility(View.GONE);
            toSet.setText("");
            Utils.setPadding(second, 1, 8, 1, 8);
        } else {
            toSet.setVisibility(View.VISIBLE);
            toSet.setText(text);
            Utils.setPadding(second, 1, 1, 1, 1);
        }
    }

    @Nullable
    @DrawableRes
    protected Integer getImageId() {
        return null;
    }

    @Nullable
    protected CharSequence getTitle() {
        return null;
    }

    @Nullable
    protected CharSequence getText() {
        return null;
    }

    @Nullable
    protected View getContentView(ViewGroup parent) {
        return null;
    }
}
