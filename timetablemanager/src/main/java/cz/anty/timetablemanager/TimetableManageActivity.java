package cz.anty.timetablemanager;

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
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import cz.anty.utils.listItem.MultilineAdapter;
import cz.anty.utils.listItem.MultilineItem;
import cz.anty.utils.listItem.TextMultilineItem;
import cz.anty.utils.timetable.Lesson;
import cz.anty.utils.timetable.Timetable;

public class TimetableManageActivity extends AppCompatActivity {

    public static Timetable toShow = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (toShow == null) {
            startActivity(new Intent(this, TimetableSelectActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_timetable_manage);
        setTitle(getString(R.string.activity_title_timetable_manage) + " - " + toShow.getName());

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
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

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
                textView.setText(positionOffset < 0.5f ? getString(Timetable.DAYS_STRINGS_IDS[position]) : getString(Timetable.DAYS_STRINGS_IDS[position + 1]));
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
                    textView.setTextColor(Color.argb((int) ((positionOffset < 0.5f ? Math.abs(1f - positionOffset) : positionOffset) * 255f), 255, 255, 255));
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

        private MultilineAdapter<MultilineItem> adapter;

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
            Context context = container.getContext();
            ListView rootView = new ListView(context); //inflater.inflate(R.layout.fragment_timetable_manage, container, false);
            adapter = new MultilineAdapter<>(context);
            rootView.setAdapter(adapter);
            initializeListView(context, rootView);
            return rootView;
        }

        private void initializeListView(final Context context, final ListView rootView) {
            final int day = getArguments().getInt(ARG_SECTION_NUMBER, 0);
            Lesson[] lessons = toShow == null ? new Lesson[0] : toShow.getDay(day);

            adapter.setNotifyOnChange(false);
            adapter.clear();
            for (int i = 0; i < lessons.length; i++) {
                Lesson lesson = lessons[i];
                if (lesson != null) {
                    adapter.add(lesson);
                    continue;
                }
                adapter.add(new TextMultilineItem(i + ". " + context
                        .getString(R.string.list_item_text_click_to_edit), null));
            }
            adapter.notifyDataSetChanged();

            rootView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    if (toShow == null) return;
                    final Lesson lesson = toShow.getLesson(day, position);
                    if (lesson == null) {
                        edit(context, null, day, position);
                    } else {
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.dialog_title_lesson_info)
                                        //TODO add set icon with icon "T"
                                .setMessage(context.getString(R.string.dialog_message_lesson_name) + ": " + lesson.getName() + "\n" +
                                        context.getString(R.string.dialog_message_short_lesson_name) + ": " + lesson.getShortName() + "\n" +
                                        context.getString(R.string.dialog_message_class) + ": " + lesson.getClassString() + "\n" +
                                        context.getString(R.string.dialog_message_teacher) + ": " + lesson.getTeacher())
                                .setPositiveButton(R.string.but_ok, null)
                                .setNegativeButton(R.string.but_delete, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        toShow.setLesson(null, day, position);
                                        initializeListView(context, rootView);
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
                    nameTextView.setText(R.string.dialog_message_lesson_name);

                    TextView shortNameTextView = new TextView(context);
                    shortNameTextView.setText(R.string.dialog_message_short_lesson_name);

                    TextView classTextView = new TextView(context);
                    classTextView.setText(R.string.dialog_message_class);

                    TextView teacherTextView = new TextView(context);
                    teacherTextView.setText(R.string.dialog_message_teacher);

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

                    ScrollView mainScrollView = new ScrollView(context);
                    mainScrollView.addView(linearLayout);

                    new AlertDialog.Builder(context)
                            .setTitle(String.format(context.getString(R.string.dialog_title_lesson),
                                    context.getString(Timetable.DAYS_STRINGS_IDS[day]), lessonIndex))
                                    //TODO add set icon with icon "T"
                            .setView(mainScrollView)
                            .setCancelable(false)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    toShow.setLesson(new Lesson(nameEditText.getText().toString(),
                                            shortNameEditText.getText().toString(), classEditText.getText().toString(),
                                            teacherEditText.getText().toString()), day, lessonIndex);
                                    initializeListView(context, rootView);
                                }
                            })
                            .setNegativeButton(R.string.but_cancel, null)
                            .show();

                    initializeListView(context, rootView);
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
            return Timetable.DAYS_STRINGS_IDS.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(Timetable.DAYS_STRINGS_IDS[position]);
        }
    }

}
