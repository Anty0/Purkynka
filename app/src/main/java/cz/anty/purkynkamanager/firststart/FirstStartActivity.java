package cz.anty.purkynkamanager.firststart;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
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
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);
        worker = new OnceRunThreadWithSpinner(this);
        contentScrollView = (ScrollView) findViewById(R.id.contentScrollView);
        butSkip = (Button) findViewById(R.id.butSkip);
        butNext = (Button) findViewById(R.id.butNext);

        final FirstStartPage[] firstStartPages = new FirstStartPage[]{
                new WelcomeFirstStartPage(FirstStartActivity.this),
                new TermsFirstStartPage(FirstStartActivity.this),
                new ChangeLogFirstStartPage(FirstStartActivity.this),
                new SASFirstStartPage(FirstStartActivity.this),
                new WifiFirstStartPage(FirstStartActivity.this),
                new ICFirstStartPage(FirstStartActivity.this)
        };
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                pagesManager = new PagesManager(savedInstanceState, firstStartPages);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (worker.getWorkerLock()) {
                            if (validatePage(pagesManager.get())) {
                                butSkip.setOnClickListener(FirstStartActivity.this);
                                butNext.setOnClickListener(FirstStartActivity.this);
                                updateState(false);
                            }
                        }
                    }
                });
            }
        }, getText(R.string.wait_text_please_wait));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        pagesManager.save(outState);
    }

    @SuppressWarnings("ResourceType")
    private void updateState(boolean animate) {
        synchronized (worker.getWorkerLock()) {
            if (animate) {
                final View oldView = contentScrollView.getChildAt(0);
                final View newView = page.getView(contentScrollView);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contentScrollView.removeAllViews();
                        final FrameLayout frameLayout = new FrameLayout(FirstStartActivity.this);
                        frameLayout.addView(oldView);
                        frameLayout.addView(newView);
                        contentScrollView.addView(frameLayout);

                        TranslateAnimation oldAnimation = new TranslateAnimation(0, -contentScrollView.getWidth(), 0, 0);
                        oldAnimation.setDuration(150);
                        TranslateAnimation newAnimation = new TranslateAnimation(contentScrollView.getWidth(), 0, 0, 0);
                        newAnimation.setDuration(150);

                        oldView.setAnimation(oldAnimation);
                        newView.setAnimation(newAnimation);

                        newAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                oldView.clearAnimation();
                                newView.clearAnimation();
                                frameLayout.removeAllViews();
                                contentScrollView.removeAllViews();
                                contentScrollView.addView(newView);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        oldAnimation.startNow();
                        newAnimation.startNow();
                        oldView.invalidate();
                        newView.invalidate();
                    }
                });
                try {
                    Thread.sleep(60);
                    while (oldView.getParent() != null) {
                        Thread.sleep(10);
                    }
                } catch (InterruptedException e) {
                    Log.d(getClass().getSimpleName(), "updateState", e);
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contentScrollView.removeAllViews();
                        contentScrollView.addView(page.getView(contentScrollView));
                    }
                });
            }

        /*if (Build.VERSION.SDK_INT >= 12) {
            ViewPropertyAnimator animator = contentScrollView.animate().scaleX(10);
            if (Build.VERSION.SDK_INT >= 14) {
                animator.start();
            }
        }*/

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setTitle(page.getTitle());

                    butNext.setText(page.getButNextText());
                    butSkip.setText(page.getButSkipText());
                    butNext.setVisibility(page.getButNextVisibility());
                    butSkip.setVisibility(page.getButSkipVisibility());
                }
            });
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
            }, getText(R.string.wait_text_please_wait));
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
            if (validatePage(pagesManager.get()))
                updateState(true);
        }
    }

    private boolean validatePage(FirstStartPage page) {
        synchronized (worker.getWorkerLock()) {
            this.page = page;
            if (this.page == null) {
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
                return false;
            }
            return true;
        }
    }
}
