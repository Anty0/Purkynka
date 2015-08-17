package cz.anty.icanteenmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.listItem.MultilineAdapter;
import cz.anty.utils.listItem.MultilineItem;
import cz.anty.utils.listItem.TextMultilineItem;

public class ICanteenSelectServiceActivity extends AppCompatActivity {

    private MultilineAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("START", "DEBUG-MODE: " + AppDataManager.isDebugMode(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        listView = (ListView) findViewById(R.id.listView);
        adapter = new MultilineAdapter(this, R.layout.text_multi_line_list_item);
        listView.setAdapter(adapter);

        init();
    }

    private void init() {
        MultilineItem[] data = new MultilineItem[]{
                new TextMultilineItem(getString(R.string.app_name_icanteen_burza), getString(R.string.app_description_icanteen_burza))};

        adapter.setNotifyOnChange(false);
        adapter.clear();
        for (MultilineItem item : data) {
            adapter.add(item);
        }
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                //final String item = (String) parent.getItemAtPosition(position);
                switch (position) {
                    case 0:
                        startActivity(new Intent(ICanteenSelectServiceActivity.this, ICanteenSelectServiceActivity.class));//TODO start Burza checker
                        break;
                }
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
            //TODO open settings
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
