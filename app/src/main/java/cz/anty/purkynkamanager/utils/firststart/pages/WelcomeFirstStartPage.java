package cz.anty.purkynkamanager.utils.firststart.pages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.firststart.FirstStartActivity;
import cz.anty.purkynkamanager.utils.other.FirstStartPage;
import cz.anty.purkynkamanager.utils.other.Utils;

/**
 * Created by anty on 28.8.15.
 *
 * @author anty
 */
@Deprecated
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
    public CharSequence getTitle() {
        return getContext().getText(R.string.activity_title_first_start_welcome);
    }

    @Override
    public int getButSkipVisibility() {
        return View.GONE;
    }

    @Override
    public View getView(ViewGroup rootView) {
        final Context context = getContext();
        View view = LayoutInflater.from(context).inflate(R.layout
                .activity_first_start_welcome_terms, rootView, false);
        TextView textView = ((TextView) view.findViewById(R.id.contentTextView));
        textView.setText(R.string.text_welcome);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.generateLanguageChangeDialog(context, new Runnable() {
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
}
