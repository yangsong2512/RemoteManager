package com.hhinc.remotemanager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * Created by GKX100212 on 2017/9/9.
 *
 */

public class RemoteManagerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals("android.intent.action.BOOT_COMPLETED")){
            Intent service = new Intent();
            ComponentName componentName = new ComponentName("com.hhinc.remotemanager",
                    "com.hhinc.remotemanager.RemoteManagerService");
            service.setComponent(componentName);
            context.startService(service);
        }
    }
}
