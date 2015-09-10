package cz.anty.purkynkamanager.firststart;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import cz.anty.icanteenmanager.ICanteenFirstStartPage;
import cz.anty.purkynkamanager.BuildConfig;
import cz.anty.purkynkamanager.MainActivity;
import cz.anty.purkynkamanager.R;
import cz.anty.sasmanager.SASFirstStartPage;
import cz.anty.utils.Constants;
import cz.anty.utils.FirstStartPage;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;
import cz.anty.wifiautologin.WifiFirstStartPage;

public class FirstStartActivity extends AppCompatActivity implements View.OnClickListener {

    private OnceRunThreadWithSpinner worker;
    private ScrollView contentScrollView;
    private Button butSkip, butNext;
    private PagesManager pagesManager;
    private FirstStartPage page;

    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);
        worker = new OnceRunThreadWithSpinner(this);
        contentScrollView = (ScrollView) findViewById(R.id.contentScrollView);
        butSkip = (Button) findViewById(R.id.butSkip);
        butNext = (Button) findViewById(R.id.butNext);

        if (pagesManager == null) {
            pagesManager = new PagesManager(new FirstStartPage[]{
                    new WelcomeFirstStartPage(this),
                    new TermsFirstStartPage(this),
                    new SASFirstStartPage(this),
                    new WifiFirstStartPage(this),
                    new ICanteenFirstStartPage(this)
            });
        }

        if (validatePage(pagesManager.get())) {
            butSkip.setOnClickListener(this);
            butNext.setOnClickListener(this);
            updateState();
        }
    }

    @SuppressWarnings("ResourceType")
    private synchronized void updateState() {
        setTitle(page.getTitle());

        contentScrollView.removeAllViews();
        contentScrollView.addView(page.getView(contentScrollView));

        /*if (Build.VERSION.SDK_INT >= 12) {
            ViewPropertyAnimator animator = contentScrollView.animate().scaleX(10);
            if (Build.VERSION.SDK_INT >= 14) {
                animator.start();
            }
        }*/

        butNext.setText(page.getButNextText());
        butSkip.setText(page.getButSkipText());
        butNext.setVisibility(page.getButNextVisibility());
        butSkip.setVisibility(page.getButSkipVisibility());
    }

    @Override
    public synchronized void onClick(View v) {
        butSkip.setEnabled(false);
        butNext.setEnabled(false);
        final int id = v.getId();
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                doNext(id);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        butSkip.setEnabled(true);
                        butNext.setEnabled(true);
                    }
                });
            }
        }, getString(R.string.wait_text_please_wait));
    }

    private synchronized void doNext(int id) {
        switch (id) {
            case R.id.butSkip:
                if (!page.doSkip())
                    return;
                break;
            case R.id.butNext:
                if (!page.doFinish())
                    return;
                break;
        }

        pagesManager.next();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (validatePage(pagesManager.get()))
                    updateState();
            }
        });
    }

    private synchronized boolean validatePage(FirstStartPage page) {
        this.page = page;
        if (this.page == null) {
            getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                    .edit().putInt(Constants.SETTING_NAME_FIRST_START,
                    BuildConfig.VERSION_CODE).apply();
            startActivity(new Intent(FirstStartActivity.this, MainActivity.class));
            finish();
            return false;
        }
        return true;
    }
}
