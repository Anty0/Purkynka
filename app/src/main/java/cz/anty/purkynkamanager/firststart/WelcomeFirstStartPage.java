package cz.anty.purkynkamanager.firststart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.anty.icanteenmanager.ICFirstStartPage;
import cz.anty.purkynkamanager.R;
import cz.anty.sasmanager.SASFirstStartPage;
import cz.anty.utils.FirstStartPage;
import cz.anty.utils.settings.AboutActivity;
import cz.anty.wifiautologin.WifiFirstStartPage;

/**
 * Created by anty on 28.8.15.
 *
 * @author anty
 */
public class WelcomeFirstStartPage extends FirstStartPage {

    public WelcomeFirstStartPage(Context context) {
        super(context);
    }

    @Override
    public boolean showThisPage() {
        return SASFirstStartPage.available()
                || WifiFirstStartPage.available()
                || ICFirstStartPage.available();
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.activity_title_first_start_welcome);
    }

    @Override
    public int getButSkipVisibility() {
        return View.GONE;
    }

    @Override
    public int getButNextVisibility() {
        return View.VISIBLE;
    }

    @Override
    public String getButSkipText() {
        return getContext().getString(R.string.but_skip);
    }

    @Override
    public String getButNextText() {
        return getContext().getString(R.string.but_next);
    }

    @Override
    public View getView(ViewGroup rootView) {
        final Context context = getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_first_start_welcome_terms, rootView, false);
        TextView textView = ((TextView) view.findViewById(R.id.contentTextView));
        textView.setText(R.string.text_welcome);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutActivity.generateLanguageChangeDialog(context, new Runnable() {
                    @Override
                    public void run() {
                        ((Activity) context).finish();
                        context.startActivity(new Intent(context, FirstStartActivity.class));
                    }
                });
            }
        });
        return view;
    }

    @Override
    public boolean doSkip() {
        return false;
    }

    @Override
    public boolean doFinish() {
        return true;
    }
}
