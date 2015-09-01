package cz.anty.purkynkamanager.firststart;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

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
    private FrameLayout contentFrameLayout;
    private Button butSkip, butNext;
    private PagesManager pagesManager;
    private FirstStartPage page;

    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);

        if (pagesManager == null) {
            try {
                FirstStartPage[] firstStartPages = new FirstStartPage[]{
                        WelcomeFirstStartPage.class.newInstance(),
                        TermsFirstStartPage.class.newInstance(),
                        SASFirstStartPage.class.newInstance(),
                        WifiFirstStartPage.class.newInstance(),
                        ICanteenFirstStartPage.class.newInstance()
                };
                pagesManager = new PagesManager(this, firstStartPages);
                page = pagesManager.get();
            } catch (Exception e) {
                Log.d("FirstStartActivity", "onCreate", e);
                Toast.makeText(this, "Can't load first start page, try open application again", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        worker = new OnceRunThreadWithSpinner(this);
        contentFrameLayout = (FrameLayout) findViewById(R.id.contentFrameLayout);
        butSkip = (Button) findViewById(R.id.butSkip);
        butNext = (Button) findViewById(R.id.butNext);
        butSkip.setOnClickListener(this);
        butNext.setOnClickListener(this);
        updateState();
    }

    @SuppressWarnings("ResourceType")
    private synchronized void updateState() {
        setTitle(page.getTitle(this));

        contentFrameLayout.removeAllViews();
        page.doUpdate(this, getLayoutInflater(), contentFrameLayout);

        if (Build.VERSION.SDK_INT >= 12) {
            ViewPropertyAnimator animator = contentFrameLayout.animate().scaleX(10);
            if (Build.VERSION.SDK_INT >= 14) {
                animator.start();
            }
        }

        butNext.setText(page.getButNextText(this));
        butSkip.setText(page.getButSkipText(this));
        butNext.setVisibility(page.getButNextVisibility(this));
        butSkip.setVisibility(page.getButSkipVisibility(this));
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
            }
        }, getString(R.string.wait_text_please_wait));
    }

    private synchronized void doNext(int id) {
        switch (id) {
            case R.id.butSkip:
                if (!page.doSkip(this))
                    return;
                break;
            case R.id.butNext:
                if (!page.doFinish(this))
                    return;
                break;
        }

        pagesManager.next();
        page = pagesManager.get();
        if (page == null) {
            getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                    .edit().putInt(Constants.SETTING_NAME_FIRST_START,
                    BuildConfig.VERSION_CODE).apply();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(FirstStartActivity.this, MainActivity.class));
                    finish();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateState();
                    butSkip.setEnabled(true);
                    butNext.setEnabled(true);
                }
            });
        }
    }
}
