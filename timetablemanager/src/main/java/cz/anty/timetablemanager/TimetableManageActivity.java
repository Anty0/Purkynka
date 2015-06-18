package cz.anty.timetablemanager;

import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import cz.anty.utils.listItem.StableArrayAdapter;
import cz.anty.utils.timetable.Lesson;
import cz.anty.utils.timetable.Timetable;

public class TimetableManageActivity extends AppCompatActivity {

    public static Timetable toShow = null;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_manage);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        textView = (TextView) findViewById(R.id.textView);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                textView.setText(positionOffset < 0.5f ? Timetable.DAYS[position] : Timetable.DAYS[position + 1]);
                if (Build.VERSION.SDK_INT >= 11) {
                    float width;
                    Display display = getWindowManager().getDefaultDisplay();
                    if (Build.VERSION.SDK_INT >= 13) {
                        Point size = new Point();
                        display.getSize(size);
                        width = size.x;
                    } else {
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
                    textView.setTextColor(Color.argb((int) ((positionOffset < 0.5f ? Math.abs(1f - positionOffset) : positionOffset) * 255f), 255, 255, 255));
                }
            }

            @Override
            public void onPageSelected(int position) {
                //textView.setText(Timetable.DAYS[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    @Override
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
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ListView rootView = new ListView(container.getContext()); //inflater.inflate(R.layout.fragment_timetable_manage, container, false);
            initializeListView(rootView);
            return rootView;
        }

        private void initializeListView(final ListView rootView) {
            final int day = getArguments().getInt(ARG_SECTION_NUMBER, 0);
            Context context = rootView.getContext();

            Lesson[] lessons = toShow == null ? new Lesson[0] : toShow.getDay(day);
            String[] values = new String[lessons.length];
            for (int i = 0; i < values.length; i++) {
                Lesson lesson = lessons[i];
                if (lesson != null)
                    values[i] = i + ". " + lesson.getShortName() + " " + context.getString(R.string.lesson_in) + " " + lesson.getClassString();
                else values[i] = i + ". " + context.getString(R.string.but_list_click_edit);
            }

            final ArrayList<String> list = new ArrayList<>();
            Collections.addAll(list, values);
            final StableArrayAdapter adapter = new StableArrayAdapter(context,
                    android.R.layout.simple_list_item_1, list);
            rootView.setAdapter(adapter);

            rootView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    if (toShow == null) return;
                    final Context context = view.getContext();
                    final Lesson lesson = toShow.getLesson(day, position);
                    if (lesson == null) {
                        edit(context, null, day, position);
                    } else {
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.title_lesson_info)
                                        //TODO add set icon with icon "T"
                                .setMessage(context.getString(R.string.message_lesson_name) + ": " + lesson.getName() + "\n" +
                                        context.getString(R.string.message_short_lesson_name) + ": " + lesson.getShortName() + "\n" +
                                        context.getString(R.string.message_class) + ": " + lesson.getClassString() + "\n" +
                                        context.getString(R.string.message_teacher) + ": " + lesson.getTeacher())
                                .setPositiveButton(R.string.but_ok, null)
                                .setNegativeButton(R.string.but_delete, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        toShow.setLesson(context, null, day, position);
                                        initializeListView(rootView);
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

                private void edit(final Context context, @Nullable Lesson lesson, final int day, final int lessonIndex) {
                    TextView nameTextView = new TextView(context);
                    nameTextView.setText(R.string.message_lesson_name);

                    TextView shortNameTextView = new TextView(context);
                    shortNameTextView.setText(R.string.message_short_lesson_name);

                    TextView classTextView = new TextView(context);
                    classTextView.setText(R.string.message_class);

                    TextView teacherTextView = new TextView(context);
                    teacherTextView.setText(R.string.message_teacher);

                    final EditText nameEditText = new EditText(context);
                    final EditText shortNameEditText = new EditText(context);
                    final EditText classEditText = new EditText(context);
                    final EditText teacherEditText = new EditText(context);

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

                    new AlertDialog.Builder(context)
                            .setTitle(Timetable.DAYS[day] + " " + lessonIndex + ". " + context.getString(R.string.lesson))
                                    //TODO add set icon with icon "T"
                            .setView(linearLayout)
                            .setCancelable(false)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    toShow.setLesson(context, new Lesson(nameEditText.getText().toString(),
                                            shortNameEditText.getText().toString(), classEditText.getText().toString(),
                                            teacherEditText.getText().toString()), day, lessonIndex);
                                    initializeListView(rootView);
                                }
                            })
                            .setNegativeButton(R.string.but_cancel, null)
                            .show();

                    initializeListView(rootView);
                }
            });
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return Timetable.DAYS.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            try {
                return Timetable.DAYS[position];
            } catch (Exception ignored) {
                return null;
            }
        }
    }

}
