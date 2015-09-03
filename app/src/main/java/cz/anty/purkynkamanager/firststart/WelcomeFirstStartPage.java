package cz.anty.purkynkamanager.firststart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import cz.anty.purkynkamanager.R;
import cz.anty.utils.FirstStartPage;
import cz.anty.utils.settings.AboutActivity;

/**
 * Created by anty on 28.8.15.
 *
 * @author anty
 */
public class WelcomeFirstStartPage implements FirstStartPage {

    private Activity activity;

    public WelcomeFirstStartPage() {
    }

    @Override
    public void initialize(Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean showThisPage(Context context) {
        return true;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.activity_title_first_start_welcome);
    }

    @Override
    public int getButSkipVisibility(Context context) {
        return View.GONE;
    }

    @Override
    public int getButNextVisibility(Context context) {
        return View.VISIBLE;
    }

    @Override
    public String getButSkipText(Context context) {
        return context.getString(R.string.but_skip);
    }

    @Override
    public String getButNextText(Context context) {
        return context.getString(R.string.but_next);
    }

    @Override
    public void doUpdate(final Context context, LayoutInflater layoutInflater, FrameLayout contentFrameLayout) {
        layoutInflater.inflate(R.layout.activity_first_start_welcome_terms, contentFrameLayout);
        TextView textView = ((TextView) contentFrameLayout.findViewById(R.id.contentTextView));
        textView.setText(R.string.text_welcome);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutActivity.generateLanguageChangeDialog(context, new Runnable() {
                    @Override
                    public void run() {
                        activity.finish();
                        context.startActivity(new Intent(context, FirstStartActivity.class));
                    }
                });
            }
        });
    }

    @Override
    public boolean doSkip(Context context) {
        return false;
    }

    @Override
    public boolean doFinish(Context context) {
        return true;
    }
}
