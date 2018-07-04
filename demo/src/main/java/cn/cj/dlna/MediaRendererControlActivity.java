package cn.cj.dlna;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cn.cj.dlna.component.UpnpComponent;
import cn.cj.dlna.dmc.DMCController;

public class MediaRendererControlActivity extends AppCompatActivity implements View.OnClickListener {

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

        dmrName.setText(DlnaBrowserActivity.selectedDmrDevice.getDetails().getFriendlyName());

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
                new DMCController().setAVTransport(upnpComponent.getAndroidUpnpService(), DlnaBrowserActivity.selectedDmrDevice, MainActivity.PLAYURL);
                break;
            case R.id.stop:
                new DMCController().stop(upnpComponent.getAndroidUpnpService(), DlnaBrowserActivity.selectedDmrDevice);
                break;
        }
    }
}
