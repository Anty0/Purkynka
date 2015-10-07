package cz.anty.purkynkamanager.firststart;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;

import cz.anty.purkynkamanager.R;
import cz.anty.utils.Constants;
import cz.anty.utils.FirstStartPage;
import cz.anty.utils.Log;
import cz.anty.utils.thread.OnceRunThread;
import cz.anty.utils.update.UpdateConnector;

/**
 * Created by anty on 28.8.15.
 *
 * @author anty
 */
public class TermsFirstStartPage extends FirstStartPage {

    private final OnceRunThread worker;
    private Integer latestCode = null;

    public TermsFirstStartPage(Context context) {
        super(context);
        this.worker = new OnceRunThread(context);
    }

    @Override
    public boolean showThisPage() {
        try {
            latestCode = UpdateConnector.getLatestTermsVersionCode();
            return getContext().getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE)
                    .getInt(Constants.SETTING_NAME_LATEST_TERMS_CODE, -1) != latestCode;
        } catch (IOException | NumberFormatException e) {
            Log.d(getClass().getSimpleName(), "showThisPage", e);
            latestCode = null;
            return true;
        }
    }

    @Override
    public CharSequence getTitle() {
        return getContext().getText(R.string.activity_title_first_start_terms);
    }

    @Override
    public int getButSkipVisibility() {
        return View.VISIBLE;
    }

    @Override
    public int getButNextVisibility() {
        return View.VISIBLE;
    }

    @Override
    public CharSequence getButSkipText() {
        return getContext().getText(R.string.but_exit);
    }

    @Override
    public CharSequence getButNextText() {
        return getContext().getText(R.string.but_accept);
    }

    @Override
    public View getView(ViewGroup rootView) {
        Context context = getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_first_start_welcome_terms, rootView, false);
        updateTerms(context, (TextView) view.findViewById(R.id.contentTextView));
        return view;
    }

    private void updateTerms(final Context context, final TextView contentTextView) {
        contentTextView.setText(R.string.wait_text_please_wait);
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                CharSequence terms;
                boolean error = false;
                try {
                    terms = UpdateConnector.getLatestTerms(context.getString(R.string.language));
                    if (((String) terms).toLowerCase().contains("<html>"))
                        throw new IOException("Wrong page loaded");
                } catch (IOException e) {
                    Log.d(getClass().getSimpleName(), "updateTerms", e);
                    terms = context.getText(R.string.text_terms);
                    error = true;
                }

                if (!error && latestCode == null)
                    showThisPage();

                final CharSequence finalTerms = terms;
                final boolean finalError = error;
                new Handler(context.getMainLooper()).post(new Runnable() {
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
    public boolean doSkip() {
        new Handler(getContext().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ((Activity) getContext()).finish();
            }
        });
        return false;
    }

    @Override
    public boolean doFinish() {
        if (latestCode != null)
            getContext().getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE)
                    .edit().putInt(Constants.SETTING_NAME_LATEST_TERMS_CODE, latestCode).apply();
        return true;
    }
}
