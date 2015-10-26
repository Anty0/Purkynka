package cz.anty.purkynkamanager.utils.firststart.pages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.icanteen.ICLoginActivity;
import cz.anty.purkynkamanager.modules.icanteen.ICService;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.FirstStartPage;

/**
 * Created by anty on 29.8.15.
 *
 * @author anty
 */
public class ICFirstStartPage extends FirstStartPage {

    private final LinearLayout linearLayout;

    public ICFirstStartPage(Context context) {
        super(context);
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LayoutInflater.from(context).inflate(R.layout.activity_icanteen_login, linearLayout);
        //activity.getLayoutInflater().inflate(R.layout.activity_icanteen_settings, linearLayout);

        linearLayout.findViewById(R.id.but_login).setVisibility(View.GONE);
        //((CheckBox) linearLayout.findViewById(R.id.check_box_sas_marks_update)).setChecked(AppDataManager.isSASMarksAutoUpdate(activity));
    }

    public static boolean available() {
        return !AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN);
    }

    @Override
    public boolean showThisPage() {
        return available();
    }

    @Override
    public CharSequence getTitle() {
        return getContext().getText(R.string.activity_title_login_icanteen);
    }

    @Override
    public int getTheme() {
        return R.style.AppTheme_IC;
    }

    @Override
    public View getView(ViewGroup rootView) {
        EditText editUsername = (EditText) linearLayout.findViewById(R.id.edit_username);
        EditText editPassword = (EditText) linearLayout.findViewById(R.id.edit_password);
        if (editUsername.getText().toString().trim().equals("") &&
                editPassword.getText().toString().trim().equals("")) {
            String username, password;
            if (AppDataManager.isLoggedIn(AppDataManager.Type.WIFI)
                    && !AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN)) {
                username = AppDataManager.getUsername(AppDataManager.Type.WIFI);
                password = AppDataManager.getPassword(AppDataManager.Type.WIFI);
            } else {
                username = AppDataManager.getUsername(AppDataManager.Type.I_CANTEEN);
                password = AppDataManager.getPassword(AppDataManager.Type.I_CANTEEN);
            }
            editUsername.setText(username);
            editPassword.setText(password);
        }
        return linearLayout;
    }

    @Override
    public boolean doFinish() {
        Context context = getContext();
        if (ICLoginActivity.login((Activity) context,
                ((EditText) linearLayout.findViewById(R.id.edit_username)).getText().toString(),
                ((EditText) linearLayout.findViewById(R.id.edit_password)).getText().toString())) {

            context.startService(new Intent(context, ICService.class));
            return true;
        }
        return false;
    }
}