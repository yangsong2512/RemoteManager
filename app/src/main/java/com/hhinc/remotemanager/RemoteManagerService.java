package com.hhinc.remotemanager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by GKX100212 on 2017/9/9.
 *
 */

public class RemoteManagerService extends Service {
    private MyBinder mBinder = null;
    private final String TAG="RemoteManagerService";
    private NetworkStateChangedReceiver mReceiver = null;
    private DataHandlerThread mThread = null;

    class MyBinder extends Binder{
        RemoteManagerService getService(){
            return RemoteManagerService.this;
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new MyBinder();
        mReceiver = new NetworkStateChangedReceiver();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver,intentFilter);
        mThread = new DataHandlerThread(getBaseContext());
        mThread.start();
    }

    private class NetworkStateChangedReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                {
                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                            WifiManager.WIFI_STATE_UNKNOWN);
                    if(state == WifiManager.WIFI_STATE_ENABLED){
                        Log.d(TAG,"wifi enabled");
                        mThread.onWifiEnabled();
                    }
                }
                    break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                {
                    NetworkInfo networkInfo = intent.
                            getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if(networkInfo.getState()== NetworkInfo.State.CONNECTED){
                        Log.d(TAG,"wifi connected");
                        mThread.onWifiConnected();
                    }
                }
                    break;
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
