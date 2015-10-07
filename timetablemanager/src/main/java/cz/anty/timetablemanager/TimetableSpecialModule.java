package cz.anty.timetablemanager;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import cz.anty.utils.Constants;
import cz.anty.utils.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialModule;
import cz.anty.utils.timetable.Lesson;
import cz.anty.utils.timetable.Timetable;
import cz.anty.utils.timetable.TimetableManager;

/**
 * Created by anty on 2.10.15.
 *
 * @author anty
 */
public class TimetableSpecialModule extends SpecialModule {

    private final ArrayList<TimetableSpecialItem> mItems = new ArrayList<>();
    private final TimetableAddSpecialItem addSpecialItem;

    public TimetableSpecialModule(Context context) {
        super(context);
        addSpecialItem = new TimetableAddSpecialItem(context);
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
        if (TimetableSelectActivity.timetableManager == null)
            TimetableSelectActivity.timetableManager = new TimetableManager(getContext());

        mItems.clear();
        for (Timetable timetable : TimetableSelectActivity
                .timetableManager.getTimetables()) {
            mItems.add(new TimetableSpecialItem(getContext(), timetable));
        }
        return true;
    }

    @Override
    protected void onUpdate() {
        boolean changed = false;
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
                mItems.add(new TimetableSpecialItem(getContext(), timetable));
                changed = true;
            }
        }
        if (changed) notifyItemsChanged();
        else notifyItemsModified();
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
        private LinearLayout mLinearLayout;

        public TimetableSpecialItem(Context context, Timetable timetable) {
            super(context);
            mTimetable = timetable;
        }

        public Timetable getTimetable() {
            return mTimetable;
        }

        @Override
        public void onCreateViewHolder(FrameLayout parent, int itemPosition) {
            super.onCreateViewHolder(parent, itemPosition);

            mLinearLayout = new LinearLayout(getContext());
            mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        }

        @Nullable
        @Override
        public CharSequence getTitle() {
            return getContext().getText(R.string.activity_title_timetable_manage)
                    + " - " + mTimetable.getName();
        }

        @Nullable
        @Override
        protected View getContentView(ViewGroup parent) {
            mLinearLayout.removeAllViews();
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

                            if (minuteTime < requestedTime - 60) {
                                nextLesson = actualLesson;
                                actualLesson = null;
                            }

                            LayoutInflater inflater = LayoutInflater.from(getContext());
                            View smallView = inflater.inflate(R.layout
                                    .text_widget_multi_line_list_item, mLinearLayout, false);
                            if (actualLesson == null) {
                                ((TextView) smallView.findViewById(R.id.widget_text_view_title))
                                        .setText(R.string.list_item_text_no_actual_lesson);
                                smallView.findViewById(R.id.widget_text_view_title)
                                        .setPadding(1, 8, 1, 8);
                                smallView.findViewById(R.id.widget_text_view_text)
                                        .setVisibility(View.GONE);
                            } else {
                                ((TextView) smallView.findViewById(R.id.widget_text_view_title))
                                        .setText(actualLesson.getTitle(getContext(), i));
                                ((TextView) smallView.findViewById(R.id.widget_text_view_text))
                                        .setText(actualLesson.getText(getContext(), i));
                                smallView.findViewById(R.id.widget_text_view_title)
                                        .setPadding(1, 1, 1, 1);
                                smallView.findViewById(R.id.widget_text_view_text)
                                        .setVisibility(View.VISIBLE);
                            }
                            mLinearLayout.addView(smallView);

                            View bigView = inflater.inflate(R.layout
                                    .text_widget_big_multi_line_list_item, mLinearLayout, false);
                            if (nextLesson == null) {
                                ((TextView) bigView.findViewById(R.id.text_view_title))
                                        .setText(R.string.list_item_text_no_next_lesson);
                                bigView.findViewById(R.id.text_view_title)
                                        .setPadding(1, 8, 1, 8);
                                bigView.findViewById(R.id.text_view_text)
                                        .setVisibility(View.GONE);
                            } else {
                                int index = mTimetable.getLessonIndex(nextLesson);
                                ((TextView) bigView.findViewById(R.id.text_view_title))
                                        .setText(nextLesson.getTitle(getContext(), index));
                                ((TextView) bigView.findViewById(R.id.text_view_text))
                                        .setText(nextLesson.getText(getContext(), index));
                                bigView.findViewById(R.id.text_view_title)
                                        .setPadding(1, 1, 1, 1);
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
            }
            return super.getContentView(parent);
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

        public TimetableAddSpecialItem(Context context) {
            super(context);
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
