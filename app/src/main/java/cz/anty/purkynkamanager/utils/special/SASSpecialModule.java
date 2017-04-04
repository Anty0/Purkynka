package cz.anty.purkynkamanager.utils.special;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.sas.SASManagerService;
import cz.anty.purkynkamanager.modules.sas.SASSplashActivity;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.SpecialModule;
import cz.anty.purkynkamanager.utils.other.sas.mark.Lesson;
import cz.anty.purkynkamanager.utils.other.sas.mark.MarksManager;

/**
 * Created by anty on 5.10.15.
 *
 * @author anty
 */
public class SASSpecialModule extends SpecialModule {

    private static final String LOG_TAG = "SASSpecialModule";

    private final SASLoginSpecialItem loginItem;
    private final ArrayList<SASWrongMarkSpecialItem> mItems = new ArrayList<>();

    public SASSpecialModule(Context context) {
        super(context);
        loginItem = new SASLoginSpecialItem();
    }

    private void updateItems(SharedPreferences preferences) {
        SASManagerService.SASBinder binder =
                SASSplashActivity.serviceManager.getBinder();

        mItems.clear();
        if (binder != null) {
            try {
                for (Lesson lesson : binder.getLessons(MarksManager.Semester.AUTO)) {
                    if (lesson.getDiameter() > 3.5d)
                        mItems.add(new SASWrongMarkSpecialItem(preferences, lesson));
                }
            } catch (InterruptedException e) {
                Log.d(LOG_TAG, "onInitialize", e);
            }
        }
    }

    @Override
    protected boolean isInitOnThread() {
        return true;
    }

    @Override
    protected boolean isUpdateOnThread() {
        return true;
    }

    @Override
    protected boolean onInitialize(SharedPreferences preferences) {
        onUpdate(preferences);
        return false;
    }

    @Override
    protected void onUpdate(final SharedPreferences preferences) {
        loginItem.setEnabled(preferences
                .getBoolean(Constants.SETTING_NAME_ITEM_SAS_LOGIN, true));

        SASSplashActivity.initService(getContext(), getWorker(), new Runnable() {
            @Override
            public void run() {
                getWorker().startWorker(new Runnable() {
                    @Override
                    public void run() {
                        updateItems(preferences);
                        if (!isInitialized()) {
                            notifyInitializeCompleted();
                            return;
                        }
                        notifyItemsChanged();
                        //notifyItemsModified();
                    }
                });
            }
        });
    }

    @Override
    protected void onSaveState(SharedPreferences.Editor preferences) {
        preferences.putBoolean(Constants.SETTING_NAME_ITEM_SAS_LOGIN,
                loginItem.isEnabled());
        for (SASWrongMarkSpecialItem item : mItems) {
            item.saveItem(preferences);
        }
    }

    @Override
    protected SpecialItem[] getItems() {
        MultilineSpecialItem[] items = mItems.toArray(new
                MultilineSpecialItem[mItems.size() + 1]);
        items[items.length - 1] = loginItem;
        return items;
    }

    @Override
    protected CharSequence getModuleName() {
        return getContext().getText(R.string.app_name_sas);
    }

    private class SASLoginSpecialItem extends MultilineSpecialItem {

        public SASLoginSpecialItem() {
            super(SASSpecialModule.this);
        }

        @Nullable
        @Override
        protected Integer getImageId() {
            return R.mipmap.ic_launcher_sas_no_border;
        }

        @Nullable
        @Override
        public CharSequence getTitle() {
            return getContext().getText(R.string.activity_title_sas_login);
        }

        @Nullable
        @Override
        protected CharSequence getText() {
            if (isShowDescription())
                return getContext().getText(R.string.app_description_sas);
            return null;
        }

        @Override
        public void onClick() {
            getContext().startActivity(new Intent(getContext(), SASSplashActivity.class));
        }

        @Override
        public boolean isVisible() {
            return super.isVisible() && !AppDataManager.isLoggedIn(AppDataManager.Type.SAS);
        }

        @Override
        public int getPriority() {
            return Constants.SPECIAL_ITEM_PRIORITY_SAS_LOGIN;
        }
    }

    private class SASWrongMarkSpecialItem extends MultilineSpecialItem {

        private Lesson mBadLesson = null;

        public SASWrongMarkSpecialItem(SharedPreferences preferences, Lesson badLesson) {
            super(SASSpecialModule.this);
            mBadLesson = badLesson;
            restoreItem(preferences);
        }

        private void restoreItem(SharedPreferences preferences) {
            setEnabled(preferences.getBoolean(mBadLesson.getShortName()
                    + Constants.SETTING_NAME_ITEM_SAS_WRONG_MARK, true));
        }

        void saveItem(SharedPreferences.Editor preferences) {
            preferences.putBoolean(mBadLesson.getShortName() + Constants
                    .SETTING_NAME_ITEM_SAS_WRONG_MARK, isEnabled());
        }

        @Override
        public void onCreateViewHolder(FrameLayout parent, int itemPosition) {
            super.onCreateViewHolder(parent, itemPosition);

            ((TextView) parent.findViewById(R.id.text_view_title)).setTextColor(Color.RED);
            ((TextView) parent.findViewById(R.id.text_view_text)).setTextColor(Color.RED);
        }

        @Nullable
        @Override
        protected Integer getImageId() {
            return R.drawable.ic_action_dislike;
        }

        @Nullable
        @Override
        public CharSequence getTitle() {
            String name = "NONE", diameter = "1";
            if (mBadLesson != null) {
                name = mBadLesson.getShortName();
                diameter = Lesson.FORMAT
                        .format(mBadLesson.getDiameter());
            }
            return Utils.getFormattedText(getContext(), R.string
                            .special_item_title_bad_lesson_diameter,
                    name, diameter);
        }

        @Override
        public void onClick() {
            getContext().startActivity(new Intent(getContext(), SASSplashActivity.class));
        }

        @Override
        public boolean isVisible() {
            return super.isVisible() && mBadLesson != null
                    && AppDataManager.isLoggedIn(AppDataManager.Type.SAS);
        }

        @Override
        public int getPriority() {
            return Constants.SPECIAL_ITEM_PRIORITY_SAS_BAD_LESSON + (int) ((mBadLesson.getDiameter() - 3.5d) * 2.5d);
        }
    }
}
