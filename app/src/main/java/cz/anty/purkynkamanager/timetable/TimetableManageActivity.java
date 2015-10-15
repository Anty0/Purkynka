package cz.anty.purkynkamanager.timetable;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Calendar;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.timetable.widget.TimetableLessonWidget;
import cz.anty.purkynkamanager.utils.Log;
import cz.anty.purkynkamanager.utils.list.listView.MultilineItem;
import cz.anty.purkynkamanager.utils.list.listView.TextMultilineItem;
import cz.anty.purkynkamanager.utils.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.purkynkamanager.utils.list.recyclerView.RecyclerAdapter;
import cz.anty.purkynkamanager.utils.list.recyclerView.RecyclerItemClickListener;
import cz.anty.purkynkamanager.utils.timetable.Lesson;
import cz.anty.purkynkamanager.utils.timetable.Timetable;
import cz.anty.purkynkamanager.utils.timetable.TimetableManager;

public class TimetableManageActivity extends AppCompatActivity {

    public static final String EXTRA_TIMETABLE_NAME = "TIMETABLE_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String timetableName = getIntent().getStringExtra(EXTRA_TIMETABLE_NAME);
        if (timetableName == null) {
            startActivity(new Intent(this, TimetableSelectActivity.class));
            finish();
            return;
        }
        if (TimetableSelectActivity.timetableManager == null)
            TimetableSelectActivity.timetableManager = new TimetableManager(this);

        /*Timetable toShow = TimetableSelectActivity.timetableManager
                .getTimetableByName(timetableName);*/

        setContentView(R.layout.activity_timetable_manage);
        setTitle(getText(R.string.activity_title_timetable_manage) + " - " + timetableName);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        /*
      The {@link android.support.v4.view.PagerAdapter} that will provide
      fragments for each of the sections. We use a
      {@link FragmentPagerAdapter} derivative, which will keep every
      loaded fragment in memory. If this becomes too memory intensive, it
      may be best to switch to a
      {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
        SectionsPagerAdapter mSectionsPagerAdapter = new
                SectionsPagerAdapter(getSupportFragmentManager(), timetableName);

        final TextView textView = (TextView) findViewById(R.id.textView);

        // Set up the ViewPager with the sections adapter.
        /*
      The {@link ViewPager} that will host the section contents.
     */
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                textView.setText(positionOffset < 0.5f ? getText(Timetable.DAYS_STRINGS_IDS[position])
                        : getText(Timetable.DAYS_STRINGS_IDS[position + 1]));
                if (Build.VERSION.SDK_INT >= 11) {
                    float width;
                    Display display = getWindowManager().getDefaultDisplay();
                    if (Build.VERSION.SDK_INT >= 13) {
                        Point size = new Point();
                        display.getSize(size);
                        width = size.x;
                    } else {
                        //noinspection deprecation
                        width = display.getWidth();
                    }
                    float newWidth = width + textView.getWidth();
                    float offset = (float) positionOffsetPixels / width * newWidth;
                    if (positionOffset < 0.5f) {
                        textView.setTranslationX(-offset);
                    } else {
                        textView.setTranslationX(newWidth - offset);
                    }
                } else {
                    textView.setTextColor(Color.argb((int) ((positionOffset < 0.5f ?
                            Math.abs(1f - positionOffset) : positionOffset) * 255f), 255, 255, 255));
                }
            }

            @Override
            public void onPageSelected(int position) {
                //textView.setText(Timetable.DAYS_STRINGS_IDS[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        int index = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
        Log.d(getClass().getSimpleName(), "onCreate dayIndex: " + index);
        if (index < Timetable.DAYS_STRINGS_IDS.length)
            mViewPager.setCurrentItem(index);
    }

    @Override
    protected void onStop() {
        TimetableLessonWidget.callUpdate(this);
        super.onStop();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_default, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private MultilineRecyclerAdapter<MultilineItem> adapter;
        private Timetable toShow;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(String timetableName, int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString(EXTRA_TIMETABLE_NAME, timetableName);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (TimetableSelectActivity.timetableManager == null)
                TimetableSelectActivity.timetableManager
                        = new TimetableManager(getContext());

            toShow = TimetableSelectActivity.timetableManager
                    .getTimetableByName(getArguments()
                            .getString(EXTRA_TIMETABLE_NAME));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final int day = getArguments().getInt(ARG_SECTION_NUMBER, 0);
            final Context context = container.getContext();
            adapter = new MultilineRecyclerAdapter<>();
            View result = RecyclerAdapter.inflate(context, container, false, null, adapter,
                    new RecyclerItemClickListener.ClickListener() {
                        @Override
                        public void onClick(View view, final int position) {
                            if (toShow == null) return;
                            final Lesson lesson = toShow.getLesson(day, position);
                            if (lesson == null) {
                                edit(context, null, day, position);
                            } else {
                                new AlertDialog.Builder(context)
                                        .setTitle(R.string.dialog_title_lesson_info)
                                                //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon T
                                        .setMessage(context.getText(R.string.dialog_message_lesson_name) + ": " + lesson.getName() + "\n" +
                                                context.getText(R.string.dialog_message_short_lesson_name) + ": " + lesson.getShortName() + "\n" +
                                                context.getText(R.string.dialog_message_class) + ": " + lesson.getClassString() + "\n" +
                                                context.getText(R.string.dialog_message_teacher) + ": " + lesson.getTeacher())
                                        .setPositiveButton(R.string.but_ok, null)
                                        .setNegativeButton(R.string.but_delete, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                toShow.setLesson(null, day, position);
                                                initializeListView(context);
                                            }
                                        })
                                        .setNeutralButton(R.string.but_edit, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                edit(context, lesson, day, position);
                                            }
                                        })
                                        .setCancelable(true)
                                        .show();
                            }
                        }

                        @Override
                        public void onLongClick(View view, int position) {

                        }

                        private void edit(final Context context, @Nullable Lesson lesson, final int day, final int lessonIndex) {
                            TextView nameTextView = new AppCompatTextView(context);
                            nameTextView.setText(R.string.dialog_message_lesson_name);

                            TextView shortNameTextView = new AppCompatTextView(context);
                            shortNameTextView.setText(R.string.dialog_message_short_lesson_name);

                            TextView classTextView = new AppCompatTextView(context);
                            classTextView.setText(R.string.dialog_message_class);

                            TextView teacherTextView = new AppCompatTextView(context);
                            teacherTextView.setText(R.string.dialog_message_teacher);

                            final EditText nameEditText = new AppCompatEditText(context);
                            final EditText shortNameEditText = new AppCompatEditText(context);
                            final EditText classEditText = new AppCompatEditText(context);
                            final EditText teacherEditText = new AppCompatEditText(context);

                            if (lesson != null) {
                                nameEditText.setText(lesson.getName());
                                shortNameEditText.setText(lesson.getShortName());
                                classEditText.setText(lesson.getClassString());
                                teacherEditText.setText(lesson.getTeacher());
                            }

                            LinearLayout linearLayout = new LinearLayout(context);
                            linearLayout.setOrientation(LinearLayout.VERTICAL);

                            linearLayout.addView(nameTextView);
                            linearLayout.addView(nameEditText);

                            linearLayout.addView(shortNameTextView);
                            linearLayout.addView(shortNameEditText);

                            linearLayout.addView(classTextView);
                            linearLayout.addView(classEditText);

                            linearLayout.addView(teacherTextView);
                            linearLayout.addView(teacherEditText);

                            ScrollView mainScrollView = new ScrollView(context);
                            mainScrollView.addView(linearLayout);

                            new AlertDialog.Builder(context)
                                    .setTitle(String.format(context.getString(R.string.dialog_title_lesson),
                                            context.getString(Timetable.DAYS_STRINGS_IDS[day]), lessonIndex))
                                            //TODO add set icon with icon "T"
                                    .setView(mainScrollView)
                                    .setCancelable(true)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            toShow.setLesson(new Lesson(nameEditText.getText().toString(),
                                                    shortNameEditText.getText().toString(), classEditText.getText().toString(),
                                                    teacherEditText.getText().toString()), day, lessonIndex);
                                            initializeListView(context);
                                        }
                                    })
                                    .setNegativeButton(R.string.but_cancel, null)
                                    .show();

                            initializeListView(context);
                        }
                    });
            initializeListView(context);
            return result;
        }

        private void initializeListView(final Context context) {
            int day = getArguments().getInt(ARG_SECTION_NUMBER, 0);
            Lesson[] lessons = toShow == null ? new Lesson[0] : toShow.getDay(day);

            adapter.clearItems();
            for (int i = 0; i < lessons.length; i++) {
                Lesson lesson = lessons[i];
                if (lesson != null) {
                    adapter.addItem(lesson);
                    continue;
                }
                adapter.addItem(new TextMultilineItem(String.format(context
                        .getString(R.string.list_item_text_click_to_edit), i), null));
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final String mTimetableName;

        public SectionsPagerAdapter(FragmentManager fm, String timetableName) {
            super(fm);
            mTimetableName = timetableName;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(mTimetableName, position);
        }

        @Override
        public int getCount() {
            return Timetable.DAYS_STRINGS_IDS.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getText(Timetable.DAYS_STRINGS_IDS[position]);
        }
    }

}
