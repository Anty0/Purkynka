package cz.anty.purkynkamanager.firststart;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
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
    private ScrollView contentScrollView;
    private Button butSkip, butNext;
    private PagesManager pagesManager;
    private FirstStartPage page;

    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);

        if (pagesManager == null) {
            try {
                pagesManager = new PagesManager(this, new FirstStartPage[]{
                        WelcomeFirstStartPage.class.newInstance(),
                        TermsFirstStartPage.class.newInstance(),
                        SASFirstStartPage.class.newInstance(),
                        WifiFirstStartPage.class.newInstance(),
                        ICanteenFirstStartPage.class.newInstance()
                });
                page = pagesManager.get();
            } catch (Exception e) {
                Log.d("FirstStartActivity", "onCreate", e);
                Toast.makeText(this, R.string.exception_toast_text_can_not_load_first_start, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        worker = new OnceRunThreadWithSpinner(this);
        contentScrollView = (ScrollView) findViewById(R.id.contentScrollView);
        butSkip = (Button) findViewById(R.id.butSkip);
        butNext = (Button) findViewById(R.id.butNext);
        butSkip.setOnClickListener(this);
        butNext.setOnClickListener(this);
        updateState();
    }

    @SuppressWarnings("ResourceType")
    private synchronized void updateState() {
        setTitle(page.getTitle(this));

        contentScrollView.removeAllViews();
        contentScrollView.addView(page.getView(this, getLayoutInflater(), contentScrollView));

        /*if (Build.VERSION.SDK_INT >= 12) {
            ViewPropertyAnimator animator = contentScrollView.animate().scaleX(10);
            if (Build.VERSION.SDK_INT >= 14) {
                animator.start();
            }
        }*/

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
                }
            });
        }
    }
}
