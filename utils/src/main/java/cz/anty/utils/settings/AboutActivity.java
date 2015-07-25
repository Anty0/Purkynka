package cz.anty.utils.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ((CheckBox) findViewById(R.id.check_box_debug)).setChecked(AppDataManager.isDebugMode(this));
    }

    public void onCheckBoxDebugClick(View view) {
        AppDataManager.setDebugMode(this, ((CheckBox) findViewById(R.id.check_box_debug)).isChecked());
    }
}
