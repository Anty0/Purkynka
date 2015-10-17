package cz.anty.purkynkamanager.firststart.pages;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.ApplicationBase;
import cz.anty.purkynkamanager.utils.FirstStartPage;
import cz.anty.purkynkamanager.utils.Log;
import cz.anty.purkynkamanager.utils.update.UpdateConnector;

/**
 * Created by anty on 25.9.15.
 *
 * @author anty
 */
public class ChangeLogFirstStartPage extends FirstStartPage {

    private static final String LOG_TAG = "ChangeLogFirstStartPage";

    public ChangeLogFirstStartPage(Context context) {
        super(context);
    }

    @Override
    public boolean showThisPage() {
        return true;
    }

    @Override
    public CharSequence getTitle() {
        return getContext().getText(R.string
                .activity_title_first_start_change_log);
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
    public CharSequence getButSkipText() {
        return getContext().getText(R.string.but_skip);
    }

    @Override
    public CharSequence getButNextText() {
        return getContext().getText(R.string.but_next);
    }

    @Override
    public View getView(ViewGroup rootView) {
        Context context = getContext();
        View view = LayoutInflater.from(context).inflate(R.layout
                .activity_first_start_welcome_terms, rootView, false);
        updateTerms(context, (TextView) view.findViewById(R.id.contentTextView));
        return view;
    }

    private void updateTerms(final Context context, final TextView contentTextView) {
        contentTextView.setText(R.string.wait_text_please_wait);
        ApplicationBase.WORKER.startWorker(new Runnable() {
            @Override
            public void run() {
                CharSequence changeLog;
                boolean error = false;
                try {
                    changeLog = UpdateConnector.getLatestChangeLog();
                    if (((String) changeLog).toLowerCase().contains("<html>"))
                        throw new IOException("Wrong page loaded");
                } catch (IOException e) {
                    Log.d(LOG_TAG, "updateTerms", e);
                    changeLog = context.getText(R.string.text_change_log);
                    error = true;
                }

                final CharSequence finalChangeLog = changeLog;
                final boolean finalError = error;
                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        contentTextView.setText(finalChangeLog);
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
        return true;
    }

    @Override
    public boolean doFinish() {
        return true;
    }
}