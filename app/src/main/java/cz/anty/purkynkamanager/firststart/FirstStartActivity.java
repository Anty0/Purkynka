package cz.anty.purkynkamanager.firststart;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import cz.anty.icanteenmanager.ICFirstStartPage;
import cz.anty.purkynkamanager.BuildConfig;
import cz.anty.purkynkamanager.MainActivity;
import cz.anty.purkynkamanager.R;
import cz.anty.sasmanager.SASFirstStartPage;
import cz.anty.utils.Constants;
import cz.anty.utils.FirstStartPage;
import cz.anty.utils.Log;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;
import cz.anty.wifiautologin.WifiFirstStartPage;

public class FirstStartActivity extends AppCompatActivity implements View.OnClickListener {

    private OnceRunThreadWithSpinner worker;
    private ScrollView contentScrollView;
    private Button butSkip, butNext;
    private PagesManager pagesManager;
    private FirstStartPage page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);
        worker = new OnceRunThreadWithSpinner(this);
        contentScrollView = (ScrollView) findViewById(R.id.contentScrollView);
        butSkip = (Button) findViewById(R.id.butSkip);
        butNext = (Button) findViewById(R.id.butNext);

        final FirstStartPage[] firstStartPages = new FirstStartPage[]{
                new WelcomeFirstStartPage(FirstStartActivity.this),
                new TermsFirstStartPage(FirstStartActivity.this),
                new SASFirstStartPage(FirstStartActivity.this),
                new WifiFirstStartPage(FirstStartActivity.this),
                new ICFirstStartPage(FirstStartActivity.this)
        };
        final Thread initThread = worker.startWorker(new Runnable() {
            @Override
            public void run() {
                pagesManager = new PagesManager(firstStartPages);
            }
        }, getString(R.string.wait_text_please_wait));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    worker.waitToWorkerStop(initThread);
                } catch (InterruptedException e) {
                    Log.d(FirstStartActivity.this.getClass()
                            .getSimpleName(), "onCreate", e);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (worker.getWorkerLock()) {
                            if (validatePage(pagesManager.get())) {
                                butSkip.setOnClickListener(FirstStartActivity.this);
                                butNext.setOnClickListener(FirstStartActivity.this);
                                updateState();
                            }
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void finish() {
        super.finish();
        PagesManager.reset();
    }

    @SuppressWarnings("ResourceType")
    private void updateState() {
        synchronized (worker.getWorkerLock()) {
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
    }

    @Override
    public void onClick(View v) {
        synchronized (worker.getWorkerLock()) {
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
                            synchronized (worker.getWorkerLock()) {
                                butSkip.setEnabled(true);
                                butNext.setEnabled(true);
                            }
                        }
                    });
                }
            }, getString(R.string.wait_text_please_wait));
        }
    }

    private void doNext(int id) {
        synchronized (worker.getWorkerLock()) {
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
                    synchronized (worker.getWorkerLock()) {
                        if (validatePage(pagesManager.get()))
                            updateState();
                    }
                }
            });
        }
    }

    private boolean validatePage(FirstStartPage page) {
        synchronized (worker.getWorkerLock()) {
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
}
