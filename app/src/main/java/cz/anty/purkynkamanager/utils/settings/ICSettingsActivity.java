package cz.anty.purkynkamanager.utils.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.icanteen.receiver.StartServiceScheduleReceiver;
import cz.anty.purkynkamanager.utils.other.AppDataManager;

/**
 * Created by anty on 1.10.15.
 *
 * @author anty
 */
public class ICSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icanteen_settings);

        final CheckBox notifyNewLunches = (CheckBox) findViewById(R.id.check_box_notify_new_lunches);
        notifyNewLunches.setChecked(AppDataManager.isICNotifyNewMonthLunches());
        notifyNewLunches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDataManager.setICNotifyNewMonthLunches(notifyNewLunches.isChecked());
                sendBroadcast(new Intent(ICSettingsActivity.this, StartServiceScheduleReceiver.class));
            }
        });
    }
}
