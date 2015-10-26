package cz.anty.purkynkamanager.utils.firststart.pages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.sas.SASLoginActivity;
import cz.anty.purkynkamanager.modules.sas.receiver.StartServiceReceiver;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.FirstStartPage;

/**
 * Created by anty on 28.8.15.
 *
 * @author anty
 */
public class SASFirstStartPage extends FirstStartPage {

    private final LinearLayout linearLayout;

    public SASFirstStartPage(Context context) {
        super(context);
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.activity_sas_login, linearLayout);
        inflater.inflate(R.layout.activity_sasmanager_settings, linearLayout);

        ((EditText) linearLayout.findViewById(R.id.edit_username)).setText(AppDataManager.getUsername(AppDataManager.Type.SAS));
        ((EditText) linearLayout.findViewById(R.id.edit_password)).setText(AppDataManager.getPassword(AppDataManager.Type.SAS));
        linearLayout.findViewById(R.id.but_login).setVisibility(View.GONE);
        ((CheckBox) linearLayout.findViewById(R.id.check_box_sas_marks_update)).setChecked(AppDataManager.isSASMarksAutoUpdate());
    }

    public static boolean available() {
        return !AppDataManager.isLoggedIn(AppDataManager.Type.SAS);
    }

    @Override
    public boolean showThisPage() {
        return available();
    }

    @Override
    public CharSequence getTitle() {
        return getContext().getText(R.string.activity_title_sas_login);
    }

    @Override
    public int getTheme() {
        return R.style.AppTheme_AS;
    }

    @Override
    public View getView(ViewGroup rootView) {
        return linearLayout;
    }

    @Override
    public boolean doFinish() {
        Context context = getContext();
        if (SASLoginActivity.login((Activity) context,
                ((EditText) linearLayout.findViewById(R.id.edit_username)).getText().toString(),
                ((EditText) linearLayout.findViewById(R.id.edit_password)).getText().toString())) {
            AppDataManager.setSASMarksAutoUpdate(
                    ((CheckBox) linearLayout.findViewById(R.id.check_box_sas_marks_update)).isChecked());
            context.sendBroadcast(new Intent(context, StartServiceReceiver.class));
            return true;
        }
        return false;
    }
}
