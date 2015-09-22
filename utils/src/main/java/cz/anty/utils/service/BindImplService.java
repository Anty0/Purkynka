package cz.anty.utils.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by anty on 22.9.15.
 *
 * @author anty
 */
public abstract class BindImplService<B extends IBinder> extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public abstract B getBinder();

    @Nullable
    @Override
    public B onBind(Intent intent) {
        return getBinder();
    }
}
