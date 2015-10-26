package cz.anty.purkynkamanager.utils.special;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RemoteViews;

import java.util.ArrayList;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.timetable.TimetableManageActivity;
import cz.anty.purkynkamanager.modules.timetable.TimetableSelectActivity;
import cz.anty.purkynkamanager.modules.timetable.widget.TimetableLessonWidget;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.SpecialModule;
import cz.anty.purkynkamanager.utils.other.list.widget.WidgetProvider;
import cz.anty.purkynkamanager.utils.other.timetable.Timetable;
import cz.anty.purkynkamanager.utils.other.timetable.TimetableManager;

/**
 * Created by anty on 2.10.15.
 *
 * @author anty
 */
public class TimetableSpecialModule extends SpecialModule {

    private static final String LOG_TAG = "TimetableSpecialModule";

    private final ArrayList<TimetableSpecialItem> mItems = new ArrayList<>();
    private final TimetableAddSpecialItem addSpecialItem;

    public TimetableSpecialModule(Context context) {
        super(context);
        addSpecialItem = new TimetableAddSpecialItem();
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
        if (TimetableSelectActivity.timetableManager == null)
            TimetableSelectActivity.timetableManager = new TimetableManager(getContext());

        mItems.clear();
        for (Timetable timetable : TimetableSelectActivity
                .timetableManager.getTimetables()) {
            mItems.add(new TimetableSpecialItem(timetable));
        }
        restore(preferences);
        return true;
    }

    @Override
    protected void onUpdate(SharedPreferences preferences) {
        //boolean changed = false;
        for (Timetable timetable : TimetableSelectActivity
                .timetableManager.getTimetables()) {
            boolean contains = false;
            for (TimetableSpecialItem item : mItems) {
                if (item.getTimetable().getName()
                        .equals(timetable.getName())) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                mItems.add(new TimetableSpecialItem(timetable));
                //changed = true;
            }
        }
        restore(preferences);
        /*if (changed) notifyItemsChanged();
        else notifyItemsModified();*/
        notifyItemsChanged();
    }

    private void restore(SharedPreferences preferences) {
        addSpecialItem.setEnabled(preferences.getBoolean(
                Constants.SETTING_NAME_ITEM_TIMETABLE_ADD, true));

        for (TimetableSpecialItem item : mItems) {
            item.setEnabled(preferences.getBoolean(item
                    .getTimetable().getName() + Constants
                    .SETTING_NAME_ITEM_TIMETABLE, true));
        }
    }

    @Override
    protected void onSaveState(SharedPreferences.Editor preferences) {
        preferences.putBoolean(Constants.SETTING_NAME_ITEM_TIMETABLE_ADD,
                addSpecialItem.isEnabled());

        for (TimetableSpecialItem item : mItems) {
            preferences.putBoolean(item.getTimetable().getName()
                            + Constants.SETTING_NAME_ITEM_TIMETABLE,
                    item.isEnabled());
        }
    }

    @Override
    protected SpecialItem[] getItems() {
        SpecialItem[] items = mItems.toArray(new SpecialItem[mItems.size() + 1]);
        items[items.length - 1] = addSpecialItem;
        return items;
    }

    @Override
    protected CharSequence getModuleName() {
        return getContext().getText(R.string.app_name_timetable);
    }

    private class TimetableSpecialItem extends MultilineSpecialItem {

        private final Timetable mTimetable;
        //private LinearLayout mLinearLayout;
        private FrameLayout mFrameLayout;

        public TimetableSpecialItem(Timetable timetable) {
            super(TimetableSpecialModule.this);
            mTimetable = timetable;
        }

        public Timetable getTimetable() {
            return mTimetable;
        }

        @Override
        public void onCreateViewHolder(FrameLayout parent, int itemPosition) {
            super.onCreateViewHolder(parent, itemPosition);

            mFrameLayout = new FrameLayout(getContext());
            //mLinearLayout = new LinearLayout(getContext());
            //mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        }

        @Nullable
        @Override
        protected Integer getImageId() {
            return R.mipmap.ic_launcher_t;
        }

        @Nullable
        @Override
        public CharSequence getTitle() {
            return getContext().getText(R.string.activity_title_timetable_manage) + " - " + mTimetable.getName();
        }

        @Nullable
        @Override
        protected View getContentView(ViewGroup parent) {
            Context context = getContext();
            mFrameLayout.removeAllViews();
            try {
                RemoteViews remoteViews = TimetableLessonWidget.getContent(context, new int[0],
                        new Intent().putExtra(TimetableLessonWidget.EXTRA_TIMETABLE_NAME,
                                mTimetable.getName()), TimetableLessonWidget.class,
                        WidgetProvider.ContentType.DATA_CONTENT);

                if (remoteViews != null) {
                    mFrameLayout.addView(remoteViews.apply(context, mFrameLayout));
                    return mFrameLayout;
                }
            } catch (Exception e) {
                Log.d(LOG_TAG, "TimetableSpecialItem getContentView", e);
            }
            return super.getContentView(parent);
            /*mLinearLayout.removeAllViews();
            Calendar calendar = Calendar.getInstance();
            for (int d = 0; d < 7; d++) {
                int minuteTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
                int day = calendar.get(Calendar.DAY_OF_WEEK);

                if (day != Calendar.SUNDAY && day != Calendar.SATURDAY) {
                    for (int i = 0; i < Timetable.MAX_LESSONS; i++) {
                        int requestedTime = Timetable.START_TIMES_HOURS[i] * 60 + Timetable.START_TIMES_MINUTES[i] + 45;
                        if (minuteTime < requestedTime) {
                            Lesson actualLesson = mTimetable.getLesson(day - 2, i);
                            Lesson nextLesson = mTimetable.getNextLesson(day - 2, i);

                            if (actualLesson != null &&
                                    minuteTime < requestedTime - 60) {
                                nextLesson = actualLesson;
                                actualLesson = null;
                            }

                            LayoutInflater inflater = LayoutInflater.from(getContext());
                            View smallView = inflater.inflate(R.layout
                                    .widget_list_item_multi_line_text, mLinearLayout, false);
                            if (actualLesson == null) {
                                ((TextView) smallView.findViewById(R.id.widget_text_view_title))
                                        .setText(R.string.list_item_text_no_actual_lesson);
                                Utils.setPadding(smallView.findViewById(R.id
                                        .widget_text_view_title), 1, 8, 1, 8);
                                smallView.findViewById(R.id.widget_text_view_text)
                                        .setVisibility(View.GONE);
                            } else {
                                ((TextView) smallView.findViewById(R.id.widget_text_view_title))
                                        .setText(actualLesson.getTitle(getContext(), i));
                                ((TextView) smallView.findViewById(R.id.widget_text_view_text))
                                        .setText(actualLesson.getText(getContext(), i));
                                Utils.setPadding(smallView.findViewById(R.id
                                        .widget_text_view_title), 1, 1, 1, 1);
                                smallView.findViewById(R.id.widget_text_view_text)
                                        .setVisibility(View.VISIBLE);
                            }
                            mLinearLayout.addView(smallView);

                            View bigView = inflater.inflate(R.layout
                                    .widget_list_item_multi_line_big_text, mLinearLayout, false);
                            if (nextLesson == null) {
                                ((TextView) bigView.findViewById(R.id.text_view_title))
                                        .setText(R.string.list_item_text_no_next_lesson);
                                Utils.setPadding(bigView.findViewById(R.id
                                        .text_view_title), 1, 8, 1, 8);
                                bigView.findViewById(R.id.text_view_text)
                                        .setVisibility(View.GONE);
                            } else {
                                int index = mTimetable.getLessonIndex(nextLesson);
                                ((TextView) bigView.findViewById(R.id.text_view_title))
                                        .setText(nextLesson.getTitle(getContext(), index));
                                ((TextView) bigView.findViewById(R.id.text_view_text))
                                        .setText(nextLesson.getText(getContext(), index));
                                Utils.setPadding(bigView.findViewById(R.id
                                        .text_view_title), 1, 1, 1, 1);
                                bigView.findViewById(R.id.text_view_text)
                                        .setVisibility(View.VISIBLE);
                            }
                            mLinearLayout.addView(bigView);
                            return mLinearLayout;
                        }
                    }
                }

                calendar.add(Calendar.DAY_OF_WEEK, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
            }
            return super.getContentView(parent);*/
        }

        @Override
        public void onClick() {
            getContext().startActivity(new Intent(getContext(),
                    TimetableManageActivity.class).putExtra(TimetableManageActivity
                    .EXTRA_TIMETABLE_NAME, mTimetable.getName()));
        }

        @Override
        public int getPriority() {
            return Constants.SPECIAL_ITEM_PRIORITY_TIMETABLE;
        }

    }

    private class TimetableAddSpecialItem extends MultilineSpecialItem {

        public TimetableAddSpecialItem() {
            super(TimetableSpecialModule.this);
        }

        @Nullable
        @Override
        protected Integer getImageId() {
            return R.mipmap.ic_launcher_t;
        }

        @Nullable
        @Override
        protected CharSequence getTitle() {
            return getContext().getText(R.string.list_item_title_add_timetable);
        }

        @Nullable
        @Override
        protected CharSequence getText() {
            return getContext().getText(R.string.app_description_timetable);
        }

        @Override
        public void onClick() {
            getContext().startActivity(new Intent(getContext(),
                    TimetableSelectActivity.class).putExtra(TimetableSelectActivity
                    .EXTRA_SHOW_ADD_TIMETABLE_DIALOG, true));
        }

        @Override
        public boolean isVisible() {
            return super.isVisible() && TimetableSelectActivity
                    .timetableManager.getTimetables().length <= 0;
        }

        @Override
        public int getPriority() {
            return Constants.SPECIAL_ITEM_PRIORITY_ADD_TIMETABLE;
        }
    }
}
