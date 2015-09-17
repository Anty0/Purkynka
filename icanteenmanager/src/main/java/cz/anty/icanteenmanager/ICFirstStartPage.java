package cz.anty.icanteenmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.FirstStartPage;

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

        ((EditText) linearLayout.findViewById(R.id.edit_username)).setText(AppDataManager.getUsername(AppDataManager.Type.SAS));
        ((EditText) linearLayout.findViewById(R.id.edit_password)).setText(AppDataManager.getPassword(AppDataManager.Type.SAS));
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
    public String getTitle() {
        return getContext().getString(R.string.app_name_icanteen);
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
    public String getButSkipText() {
        return getContext().getString(R.string.but_skip);
    }

    @Override
    public String getButNextText() {
        return getContext().getString(R.string.but_next);
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