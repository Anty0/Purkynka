package cz.anty.purkynkamanager.firststart;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;

import cz.anty.purkynkamanager.R;
import cz.anty.utils.FirstStartPage;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;
import cz.anty.utils.update.UpdateConnector;

/**
 * Created by anty on 28.8.15.
 *
 * @author anty
 */
public class TermsFirstStartPage implements FirstStartPage {

    private Activity activity;
    private OnceRunThreadWithSpinner worker;

    public TermsFirstStartPage() {
    }

    @Override
    public void initialize(Activity activity) {
        this.activity = activity;
        this.worker = new OnceRunThreadWithSpinner(activity);
    }

    @Override
    public boolean showThisPage(Context context) {
        return true;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.activity_title_first_start_terms);
    }

    @Override
    public int getButSkipVisibility(Context context) {
        return View.VISIBLE;
    }

    @Override
    public int getButNextVisibility(Context context) {
        return View.VISIBLE;
    }

    @Override
    public String getButSkipText(Context context) {
        return context.getString(R.string.but_exit);
    }

    @Override
    public String getButNextText(Context context) {
        return context.getString(R.string.but_accept);
    }

    @Override
    public View getView(final Context context, LayoutInflater layoutInflater, ViewGroup rootView) {
        View view = layoutInflater.inflate(R.layout.activity_first_start_welcome_terms, rootView, false);
        updateTerms(context, (TextView) view.findViewById(R.id.contentTextView));
        return view;
    }

    private void updateTerms(final Context context, final TextView contentTextView) {
        contentTextView.setText(R.string.wait_text_please_wait);
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                String terms;
                boolean error = false;
                try {
                    terms = UpdateConnector.getLatestTerms(context.getString(R.string.language));
                } catch (IOException e) {
                    terms = context.getString(R.string.text_terms);
                    error = true;
                }
                final String finalTerms = terms;
                final boolean finalError = error;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contentTextView.setText(finalTerms);
                        if (finalError) contentTextView
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        contentTextView.setOnClickListener(null);
                                        updateTerms(context, contentTextView);
                                    }
                                });
                    }
                });
            }
        });
    }

    @Override
    public boolean doSkip(Context context) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.finish();
            }
        });
        return false;
    }

    @Override
    public boolean doFinish(Context context) {
        return true;
    }
}
