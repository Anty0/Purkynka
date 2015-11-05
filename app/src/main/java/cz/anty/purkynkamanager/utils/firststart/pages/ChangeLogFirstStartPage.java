package cz.anty.purkynkamanager.utils.firststart.pages;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;

import cz.anty.purkynkamanager.ApplicationBase;
import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.FirstStartPage;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.update.UpdateConnector;

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
    public CharSequence getTitle() {
        return getContext().getText(R.string
                .activity_title_first_start_change_log);
    }

    @Override
    public int getButSkipVisibility() {
        return View.GONE;
    }

    @Override
    public View getView(ViewGroup rootView) {
        Context context = getContext();
        View view = LayoutInflater.from(context).inflate(R.layout
                .activity_first_start_welcome_terms, rootView, false);
        updateChangeLog(context, (TextView) view.findViewById(R.id.contentTextView));
        return view;
    }

    private void updateChangeLog(final Context context, final TextView contentTextView) {
        contentTextView.setText(R.string.wait_text_please_wait);
        ApplicationBase.WORKER.startWorker(new Runnable() {
            @Override
            public void run() {
                CharSequence changeLog;
                boolean error = false;
                try {
                    changeLog = UpdateConnector.getLatestChangeLog();
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
                                        updateChangeLog(context, contentTextView);
                                    }
                                });
                    }
                });
            }
        });
    }
}