package cz.anty.sasmanager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.widget.FrameLayout;
import android.widget.TextView;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialModule;
import cz.anty.utils.sas.mark.Lesson;
import cz.anty.utils.sas.mark.MarksManager;

/**
 * Created by anty on 5.10.15.
 *
 * @author anty
 */
public class SASSpecialModule extends SpecialModule {

    private final SpecialItem[] mItems;
    private Lesson badLesson = null;

    public SASSpecialModule(Context context) {
        super(context);
        mItems = new SpecialItem[]{
                new SASLoginSpecialItem(context),
                new SASWrongMarkSpecialItem(context)
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
    protected boolean onInitialize() {
        SASSplashActivity.initService(getContext(), getWorker(), new Runnable() {
            @Override
            public void run() {
                getWorker().startWorker(new Runnable() {
                    @Override
                    public void run() {
                        updateBadLesson();
                        notifyInitializeCompleted();
                    }
                });
            }
        });
        return false;
    }

    @Override
    protected void onUpdate() {
        SASSplashActivity.initService(getContext(), getWorker(), new Runnable() {
            @Override
            public void run() {
                getWorker().startWorker(new Runnable() {
                    @Override
                    public void run() {
                        updateBadLesson();
                        notifyItemsModified();
                    }
                });
            }
        });
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

        public SASLoginSpecialItem(Context context) {
            super(context);
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

        public SASWrongMarkSpecialItem(Context context) {
            super(context);
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
            if (badLesson == null) return null;
            return String.format(getContext().getString(R.string
                            .special_item_title_bad_lesson_diameter),
                    badLesson.getShortName(), Lesson.FORMAT.format(badLesson.getDiameter()));
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
