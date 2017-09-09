package com.hhinc.remotemanager;

import android.content.ComponentName;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent service = new Intent();
        ComponentName componentName = new ComponentName("com.hhinc.remotemanager",
                "com.hhinc.remotemanager.RemoteManagerService");
        service.setComponent(componentName);
        startService(service);
    }
}
