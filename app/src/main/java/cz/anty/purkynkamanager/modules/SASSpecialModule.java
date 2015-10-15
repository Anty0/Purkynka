package cz.anty.purkynkamanager.modules;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.widget.FrameLayout;
import android.widget.TextView;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.sas.SASManagerService;
import cz.anty.purkynkamanager.sas.SASSplashActivity;
import cz.anty.purkynkamanager.utils.AppDataManager;
import cz.anty.purkynkamanager.utils.Constants;
import cz.anty.purkynkamanager.utils.Log;
import cz.anty.purkynkamanager.utils.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.purkynkamanager.utils.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.purkynkamanager.utils.list.recyclerView.specialAdapter.SpecialModule;
import cz.anty.purkynkamanager.utils.sas.mark.Lesson;
import cz.anty.purkynkamanager.utils.sas.mark.MarksManager;

/**
 * Created by anty on 5.10.15.
 *
 * @author anty
 */
public class SASSpecialModule extends SpecialModule {

    private final MultilineSpecialItem[] mItems;
    private Lesson badLesson = null;

    public SASSpecialModule(Context context) {
        super(context);
        mItems = new MultilineSpecialItem[]{
                new SASLoginSpecialItem(),
                new SASWrongMarkSpecialItem()
        };
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
    protected void onUpdate(SharedPreferences preferences) {
        mItems[0].setEnabled(preferences
                .getBoolean(Constants.SETTING_NAME_ITEM_SAS_LOGIN, true));
        mItems[1].setEnabled(preferences
                .getBoolean(Constants.SETTING_NAME_ITEM_SAS_WRONG_MARK, true));

        SASSplashActivity.initService(getContext(), getWorker(), new Runnable() {
            @Override
            public void run() {
                getWorker().startWorker(new Runnable() {
                    @Override
                    public void run() {
                        updateBadLesson();
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
                mItems[0].isEnabled())
                .putBoolean(Constants.SETTING_NAME_ITEM_SAS_WRONG_MARK,
                        mItems[1].isEnabled());
    }

    private void updateBadLesson() {
        SASManagerService.SASBinder binder =
                SASSplashActivity.serviceManager.getBinder();

        badLesson = null;
        if (binder != null) {
            try {
                for (Lesson lesson : binder.getLessons(MarksManager.Semester.AUTO)) {
                    double diameter = lesson.getDiameter();
                    if (diameter > 3.5d && (badLesson == null
                            || badLesson.getDiameter() < diameter))
                        badLesson = lesson;
                }
            } catch (InterruptedException e) {
                Log.d(getClass().getSimpleName(), "onInitialize", e);
            }
        }
    }

    @Override
    protected SpecialItem[] getItems() {
        return mItems;
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

        public SASWrongMarkSpecialItem() {
            super(SASSpecialModule.this);
        }

        @Override
        public void onCreateViewHolder(FrameLayout parent, int itemPosition) {
            super.onCreateViewHolder(parent, itemPosition);

            ((TextView) parent.findViewById(R.id.text_view_title)).setTextColor(Color.RED);
            ((TextView) parent.findViewById(R.id.text_view_text)).setTextColor(Color.RED);
        }

        @Nullable
        @Override
        public CharSequence getTitle() {
            String name = "NONE", diameter = "1";
            if (badLesson != null) {
                name = badLesson.getShortName();
                diameter = Lesson.FORMAT
                        .format(badLesson.getDiameter());
            }
            return String.format(getContext().getString(R.string
                            .special_item_title_bad_lesson_diameter),
                    name, diameter);
        }

        @Override
        public void onClick() {
            getContext().startActivity(new Intent(getContext(), SASSplashActivity.class));
        }

        @Override
        public boolean isVisible() {
            return super.isVisible() && badLesson != null
                    && AppDataManager.isLoggedIn(AppDataManager.Type.SAS);
        }

        @Override
        public int getPriority() {
            return Constants.SPECIAL_ITEM_PRIORITY_SAS_BAD_LESSON;
        }
    }
}
