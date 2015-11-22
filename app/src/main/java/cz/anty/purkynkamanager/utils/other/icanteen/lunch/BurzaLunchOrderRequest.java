package cz.anty.purkynkamanager.utils.other.icanteen.lunch;

import android.content.Context;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.icanteen.ICSplashActivity;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.burza.BurzaLunch;
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
public class BurzaLunchOrderRequest extends SimpleLunchOrderRequest {
    private final BurzaLunch mBurzaLunch;

    public BurzaLunchOrderRequest(BurzaLunch lunch) {
        mBurzaLunch = lunch;
    }

    public BurzaLunch getBurzaLunch() {
        return mBurzaLunch;
    }

    @Override
    public boolean doOrder() throws Throwable {
        return ICSplashActivity.serviceManager != null &&
                ICSplashActivity.serviceManager.isConnected() &&
                ICSplashActivity.serviceManager.getBinder().doOrderLunch(mBurzaLunch);
    }

    @Override
    public CharSequence getTitle(Context context, int position) {
        return context.getText(R.string.but_order_from_burza) + " - " + BurzaLunch
                .DATE_FORMAT.format(mBurzaLunch.getDate());
    }

    @Override
    public CharSequence getText(Context context, int position) {
        return super.getText(context, position) + " - " + mBurzaLunch.getName();
    }
}