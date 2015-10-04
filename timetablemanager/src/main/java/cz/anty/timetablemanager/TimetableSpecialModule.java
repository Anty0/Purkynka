package cz.anty.timetablemanager;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import cz.anty.utils.Constants;
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

    private ArrayList<SpecialItem> items = new ArrayList<>();
    private ArrayList<String> disabledTimetables = new ArrayList<>();

    public TimetableSpecialModule(Context context) {
        super(context);
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
    protected void onInitialize() {
        if (TimetableSelectActivity.timetableManager == null)
            TimetableSelectActivity.timetableManager = new TimetableManager(getContext());
        onUpdate();
    }

    @Override
    protected void onUpdate() {
        items.clear();
        for (final Timetable timetable : TimetableSelectActivity
                .timetableManager.getTimetables()) {
            if (disabledTimetables.contains(timetable.getName()))
                continue;

            items.add(new SpecialItem() {
                private TextView title;
                private LinearLayout mLinearLayout;

                @Override
                public void onCreateViewHolder(FrameLayout parent, int itemPosition) {
                    LinearLayout linearLayout = new LinearLayout(getContext());
                    linearLayout.setOrientation(LinearLayout.VERTICAL);

                    LayoutInflater.from(getContext()).inflate(R.layout
                            .base_multiline_text_item, linearLayout);

                    title = (TextView) linearLayout.findViewById(R.id.text_view_title);
                    linearLayout.findViewById(R.id.text_view_text).setVisibility(View.GONE);

                    mLinearLayout = new LinearLayout(getContext());
                    mLinearLayout.setOrientation(LinearLayout.VERTICAL);

                    linearLayout.addView(mLinearLayout);
                    parent.addView(linearLayout);
                }

                @Override
                public void onBindViewHolder(int itemPosition) {
                    title.setText(getContext().getText(R.string.activity_title_timetable_manage)
                            + " - " + timetable.getName());

                    mLinearLayout.removeAllViews();

                    Calendar calendar = Calendar.getInstance();
                    for (int d = 0; d < 7; d++) {
                        int minuteTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
                        int day = calendar.get(Calendar.DAY_OF_WEEK);

                        if (day != Calendar.SUNDAY && day != Calendar.SATURDAY) {
                            for (int i = 0; i < Timetable.MAX_LESSONS; i++) {
                                int requestedTime = Timetable.START_TIMES_HOURS[i] * 60 + Timetable.START_TIMES_MINUTES[i] + 45;
                                if (minuteTime < requestedTime) {
                                    Lesson actualLesson = timetable.getLesson(day - 2, i);
                                    Lesson nextLesson = timetable.getNextLesson(day - 2, i);

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
                                        ((TextView) bigView.findViewById(R.id.text_view_title))
                                                .setText(nextLesson.getTitle(getContext(), i));
                                        ((TextView) bigView.findViewById(R.id.text_view_text))
                                                .setText(nextLesson.getText(getContext(), i));
                                        bigView.findViewById(R.id.text_view_title)
                                                .setPadding(1, 1, 1, 1);
                                        bigView.findViewById(R.id.text_view_text)
                                                .setVisibility(View.VISIBLE);
                                    }
                                    mLinearLayout.addView(bigView);
                                    return;
                                }
                            }
                        }

                        calendar.add(Calendar.DAY_OF_WEEK, 1);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                    }
                }

                @Override
                public void onClick() {
                    TimetableManageActivity.toShow = timetable;
                    getContext().startActivity(new Intent(getContext(),
                            TimetableManageActivity.class));
                }

                @Override
                public void onLongClick() {

                }

                @Override
                public void onHideClick() {
                    disabledTimetables.add(timetable.getName());
                    onUpdate();
                    notifyItemsChanged();
                }

                @Override
                public boolean isShowHideButton() {
                    return true;
                }

                @Override
                public int getPriority() {
                    return Constants.SPECIAL_ITEM_PRIORITY_TEMETABLE;
                }
            });
        }
    }

    @Override
    protected SpecialItem[] getItems() {
        return items.toArray(new SpecialItem[items.size()]);
    }

    @Override
    protected int getModuleNameResId() {
        return R.string.app_name_timetable;
    }
}
