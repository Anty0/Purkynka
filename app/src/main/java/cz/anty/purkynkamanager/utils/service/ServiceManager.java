package cz.anty.purkynkamanager.utils.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import cz.anty.purkynkamanager.utils.Log;

/**
 * Created by anty on 17.9.15.
 *
 * @author anty
 */
public final class ServiceManager<B extends IBinder> {

    private static final List<ServiceManager> SERVICE_MANAGERS
            = new ArrayList<>();
    private final Context mContext;
    private final Class<? extends Service> mServiceClass;
    private final Object mBinderLock = new Object();
    private final List<BinderConnection<B>> mConnections = new ArrayList<>();
    private B mBinder = null;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (mBinderLock) {
                setBinder((B) service);
                if (isConnected())
                    synchronized (mConnections) {
                        for (BinderConnection<B> connection : mConnections) {
                            connection.onBinderConnected(getBinder());
                        }
                    }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            synchronized (mBinderLock) {
                if (isConnected())
                    synchronized (mConnections) {
                        for (BinderConnection<B> listener : mConnections) {
                            listener.onBinderDisconnected();
                        }
                    }
                setBinder(null);
            }
        }
    };
    public ServiceManager(Context context, Class<? extends BindImplService<B>> serviceClass) {
        mContext = context.getApplicationContext();
        mServiceClass = serviceClass;
        SERVICE_MANAGERS.add(this);
    }

    public static void disconnectAll() {
        for (ServiceManager manager : SERVICE_MANAGERS) {
            manager.forceDisconnect();
            manager.stopService();
        }
    }

    public void addBinderConnection(BinderConnection<B> connection) {
        synchronized (mConnections) {
            if (!mConnections.contains(connection))
                synchronized (mBinderLock) {
                    mConnections.add(connection);
                    if (isConnected())
                        connection.onBinderConnected(getBinder());
                }
        }
    }

    public void removeBinderConnection(BinderConnection<B> connection) {
        synchronized (mConnections) {
            if (mConnections.contains(connection))
                synchronized (mBinderLock) {
                    mConnections.remove(connection);
                    if (isConnected())
                        connection.onBinderDisconnected();
                }
        }
    }

    public void startService() {
        Intent service = new Intent(mContext, mServiceClass);
        mContext.startService(service);
    }

    public boolean stopService() {
        Intent service = new Intent(mContext, mServiceClass);
        return mContext.stopService(service);
    }

    public void connect() {
        if (!isConnected()) {
            Intent service = new Intent(mContext, mServiceClass);
            //mContext.startService(service);
            mContext.bindService(service, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void disconnect() {
        if (isConnected())
            mContext.unbindService(mConnection);
    }

    public void forceDisconnect() {
        try {
            disconnect();
        } catch (RuntimeException e) {
            Log.d(getClass().getSimpleName(), "forceDisconnect", e);
            mConnection.onServiceDisconnected(null);
        }
    }

    public boolean isConnected() {
        return getBinder() != null;
    }

    public B getBinder() {
        synchronized (mBinderLock) {
            return mBinder;
        }
    }

    private void setBinder(B binder) {
        synchronized (mBinderLock) {
            mBinder = binder;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        SERVICE_MANAGERS.remove(this);
        forceDisconnect();
    }

    public interface BinderConnection<B extends IBinder> {

        void onBinderConnected(B binder);

        void onBinderDisconnected();
    }
}
