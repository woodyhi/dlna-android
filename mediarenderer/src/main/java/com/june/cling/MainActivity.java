package com.june.cling;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.june.cling.dmr.ZxtMediaRenderer;

import org.fourthline.cling.android.AndroidUpnpService;

public class MainActivity extends AppCompatActivity {

    private UpnpComponent upnpComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        upnpComponent = UpnpComponent.getsInstance();
        upnpComponent.init(getApplicationContext());
        upnpComponent.setConnectionCallback(new UpnpComponent.ServiceConnectCallback() {
            @Override
            public void onConnected(AndroidUpnpService upnpService) {
                ZxtMediaRenderer mediaRenderer = new ZxtMediaRenderer(1,
                        MainActivity.this);
                upnpService.getRegistry().addDevice(mediaRenderer.getDevice());
            }

            @Override
            public void onDisconnected() {

            }
        });
        upnpComponent.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        upnpComponent.stop();
    }
}
