package cz.anty.purkynkamanager.firststart.pages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.icanteen.ICLoginActivity;
import cz.anty.purkynkamanager.icanteen.ICService;
import cz.anty.purkynkamanager.utils.AppDataManager;
import cz.anty.purkynkamanager.utils.FirstStartPage;

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

        String username, password;
        if (AppDataManager.isLoggedIn(AppDataManager.Type.WIFI)
                && !AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN)) {
            username = AppDataManager.getUsername(AppDataManager.Type.WIFI);
            password = AppDataManager.getPassword(AppDataManager.Type.WIFI);
        } else {
            username = AppDataManager.getUsername(AppDataManager.Type.I_CANTEEN);
            password = AppDataManager.getPassword(AppDataManager.Type.I_CANTEEN);
        }
        ((EditText) linearLayout.findViewById(R.id.edit_username)).setText(username);
        ((EditText) linearLayout.findViewById(R.id.edit_password)).setText(password);
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
        return getContext().getText(R.string.app_name_icanteen);
    }

    @Override
    public int getButSkipVisibility() {
        return View.VISIBLE;
    }

    @Override
    public int getButNextVisibility() {
        return View.VISIBLE;
    }

    @Override
    public CharSequence getButSkipText() {
        return getContext().getText(R.string.but_skip);
    }

    @Override
    public CharSequence getButNextText() {
        return getContext().getText(R.string.but_next);
    }

    @Override
    public View getView(ViewGroup rootView) {
        return linearLayout;
    }

    @Override
    public boolean doSkip() {
        return true;
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