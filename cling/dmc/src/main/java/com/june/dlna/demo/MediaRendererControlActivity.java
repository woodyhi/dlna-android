package com.june.dlna.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.june.dlna.component.UpnpComponent;
import com.june.dlna.dmc.DMCController;

import org.fourthline.cling.model.meta.Device;

public class MediaRendererControlActivity extends AppCompatActivity implements View.OnClickListener {
    public static Device selectedDevice;

    private TextView dmrName;
    private Button transfer;
    private Button pause;
    private Button stop;

    private UpnpComponent upnpComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_renderer_control);
        dmrName = findViewById(R.id.dmr_device_name);
        transfer = findViewById(R.id.transfer);
        stop = findViewById(R.id.stop);

//        dmrName.setText(selectedDevice.getDetails().getFriendlyName());

        setTitle(selectedDevice.getDetails().getFriendlyName());

        transfer.setOnClickListener(this);
        stop.setOnClickListener(this);

        upnpComponent = UpnpComponent.getsInstance();
        upnpComponent.init(getApplicationContext());
        upnpComponent.start();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.transfer:
                String playurl = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
                new DMCController().setAVTransport(upnpComponent.getAndroidUpnpService(), selectedDevice, playurl);
                break;
            case R.id.stop:
                new DMCController().stop(upnpComponent.getAndroidUpnpService(), selectedDevice);
                break;
        }
    }
}
