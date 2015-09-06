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
public class ICanteenFirstStartPage implements FirstStartPage {

    private Activity activity;
    private LinearLayout linearLayout;

    public ICanteenFirstStartPage() {
    }

    @Override
    public void initialize(Activity activity) {
        this.activity = activity;
        linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        activity.getLayoutInflater().inflate(R.layout.activity_icanteen_login, linearLayout);
        //activity.getLayoutInflater().inflate(R.layout.activity_icanteen_settings, linearLayout);

        ((EditText) linearLayout.findViewById(R.id.edit_username)).setText(AppDataManager.getUsername(AppDataManager.Type.SAS, activity));
        ((EditText) linearLayout.findViewById(R.id.edit_password)).setText(AppDataManager.getPassword(AppDataManager.Type.SAS, activity));
        linearLayout.findViewById(R.id.but_login).setVisibility(View.GONE);
        //((CheckBox) linearLayout.findViewById(R.id.check_box_sas_marks_update)).setChecked(AppDataManager.isSASMarksAutoUpdate(activity));
    }

    @Override
    public boolean showThisPage(Context context) {
        return !AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN, context);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.app_name_icanteen);
    }

    @Override
    public int getButSkipVisibility(Context context) {
        return View.VISIBLE;
    }

    @Override
    public int getButNextVisibility(Context context) {
        return View.VISIBLE;
    }

    @Override
    public String getButSkipText(Context context) {
        return context.getString(R.string.but_skip);
    }

    @Override
    public String getButNextText(Context context) {
        return context.getString(R.string.but_next);
    }

    @Override
    public View getView(Context context, LayoutInflater layoutInflater, ViewGroup rootView) {
        return linearLayout;
    }

    @Override
    public boolean doSkip(Context context) {
        return true;
    }

    @Override
    public boolean doFinish(Context context) {
        if (ICanteenLoginActivity.login(activity,
                ((EditText) linearLayout.findViewById(R.id.edit_username)).getText().toString(),
                ((EditText) linearLayout.findViewById(R.id.edit_password)).getText().toString())) {

            context.startService(new Intent(context, ICanteenService.class));
            return true;
        }
        return false;
    }
}