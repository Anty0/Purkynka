package cz.anty.purkynkamanager.utils.other.icanteen.lunch;

import android.content.Context;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.icanteen.ICSplashActivity;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunch;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunchDay;
import proguard.annotation.Keep;
import proguard.annotation.KeepClassMemberNames;
import proguard.annotation.KeepClassMembers;
import proguard.annotation.KeepName;

/**
 * Created by anty on 22.11.2015.
 *
 * @author anty
 */
@Keep
@KeepName
@KeepClassMembers
@KeepClassMemberNames
public class MonthToBurzaLunchOrderRequest extends SimpleLunchOrderRequest {
    private final MonthLunch mMonthLunch;

    public MonthToBurzaLunchOrderRequest(MonthLunch lunch) {
        mMonthLunch = lunch;
    }

    public MonthLunch getMonthLunch() {
        return mMonthLunch;
    }

    @Override
    public boolean doOrder() throws Throwable {
        return ICSplashActivity.serviceManager != null &&
                ICSplashActivity.serviceManager.isConnected() &&
                ICSplashActivity.serviceManager.getBinder().doToBurzaMonthLunch(mMonthLunch);
    }

    @Override
    public CharSequence getTitle(Context context, int position) {
        return context.getText(R.string.text_to_burza) + "/" +
                context.getText(R.string.text_from_burza) + " - " +
                MonthLunchDay.DATE_SHOW_FORMAT.format(mMonthLunch.getDate());
    }

    @Override
    public CharSequence getText(Context context, int position) {
        return super.getText(context, position) + " - " + mMonthLunch.getName();
    }
}