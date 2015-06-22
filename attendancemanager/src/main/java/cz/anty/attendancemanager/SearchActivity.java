package cz.anty.attendancemanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import cz.anty.utils.OnceRunThreadWithProgress;
import cz.anty.utils.attendance.AttendanceConnector;
import cz.anty.utils.attendance.man.Man;
import cz.anty.utils.attendance.man.Mans;
import cz.anty.utils.listItem.MultilineAdapter;
import cz.anty.utils.listItem.MultilineItem;
import cz.anty.utils.listItem.TextMultilineItem;

public class SearchActivity extends AppCompatActivity {

    private final AttendanceConnector connector = new AttendanceConnector();
    private EditText searchEditText;
    private MultilineAdapter adapter;
    private OnceRunThreadWithProgress worker;
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchEditText = (EditText) findViewById(R.id.editText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                page = 1;
                update(false);
            }
        });
        ListView resultListView = ((ListView) findViewById(R.id.listView));
        adapter = new MultilineAdapter(this, R.layout.multi_line_list_item);
        resultListView.setAdapter(adapter);
        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == adapter.getCount() - 1) {
                    page++;
                    update(true);
                }
            }
        });

        if (worker == null)
            worker = new OnceRunThreadWithProgress(this);
        update(false);
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

    public void onUpdate(View view) {
        page = 1;
        update(true);
    }

    private void update(final boolean showMessage) {
        final String toSearch = searchEditText.getText().toString();//TODO add auto complete using timetable and marks lessons
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                if (!showMessage && worker.getWaitingThreadsLength() > 0) return;
                //String[] values;
                MultilineItem[] data;
                try {
                    List<Man> mans = Mans.parseMans(connector.getSupElements(toSearch, page));
                    data = mans.toArray(new MultilineItem[mans.size() + 1]);
                    data[data.length - 1] = new TextMultilineItem(getString(R.string.to_page) + " -> " + (page + 1), getString(R.string.on_page) + ": " + page);
                    /*values = new String[mans.size()];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = mans.get(i).toString();
                    }*/
                } catch (IOException | URISyntaxException e) {
                    //values = new String[]{"Connection exception: " + e.getMessage() + "\n" + "Check your internet connection"};
                    data = new MultilineItem[]{new TextMultilineItem("Check your internet connection", "Connection exception: " + e.getMessage())};
                    Log.d(null, null, e);
                }

                /*final ArrayList<String> list = new ArrayList<>();
                Collections.addAll(list, values);
                final StableArrayAdapter adapter = new StableArrayAdapter(SearchActivity.this,
                        android.R.layout.simple_list_item_1, list);*/

                final MultilineItem[] finalData = data;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                        for (MultilineItem item : finalData) {
                            adapter.add(item);
                        }
                        adapter.notifyDataSetChanged();
                        //ListView listView = ((ListView) findViewById(R.id.listView));
                        //listView.setAdapter(adapter);
                    }
                });
            }
        }, showMessage ? getString(R.string.searching) : null);
    }
}
