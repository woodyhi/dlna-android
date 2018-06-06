package cn.cj.dlna;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.util.ArrayList;

import cn.cj.dlna.component.DMC_Controller;
import cn.cj.dlna.component.UpnpComponent;

public class DlnaBrowserActivity extends AppCompatActivity {

    private Button search;
    private ListView listView;

    private UpnpComponent upnpComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dlna_browser);
        search = (Button) findViewById(R.id.search);
        listView = (ListView) findViewById(R.id.listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device device = (Device) parent.getItemAtPosition(position);

                new DMC_Controller().setUri(upnpComponent.getAndroidUpnpService(), device, MainActivity.PLAYURL);
            }
        });

        listView.setAdapter(new DeviceAdapter(this, new ArrayList<Device>()));


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DeviceAdapter) listView.getAdapter()).clear();
                if(upnpComponent.getAndroidUpnpService() != null){
                    upnpComponent.getAndroidUpnpService().getRegistry().removeAllRemoteDevices();
                    upnpComponent.getAndroidUpnpService().getControlPoint().search();
                }
            }
        });

        upnpComponent = UpnpComponent.getsInstance();
        upnpComponent.init(getApplicationContext());
        upnpComponent.start();
        upnpComponent.addRegistryListener(new BrowseRegistryListener());
    }

    @Override
    protected void onPause() {
        super.onPause();
        upnpComponent.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected class BrowseRegistryListener extends DefaultRegistryListener {

        @Override
        public void deviceAdded(Registry registry, final Device device) {
            Log.d("----------deviceAdded", "\n" + device.getDisplayString()
                    + "\n" + device.getDetails().getFriendlyName() // 用这个名字显示设备
                    + "\n" + device.getDetails().getSerialNumber()
                    + "\n" + device.getDetails().getBaseURL()
                    + "\n" + device.getDetails().getModelDetails().getModelName()
                    + "\n" + device.getType()
                    + "\n" + device.getType().getType());

            runOnUiThread(new Runnable() {
                public void run() {
                    Service avTransportS = device.findService(new UDAServiceType("AVTransport"));
                    Service renderS = device.findService(new UDAServiceType("RenderingControl"));
                    if(avTransportS != null || renderS != null){
                        ((DeviceAdapter)listView.getAdapter()).add(device);
                    }
                }
            });


        }

        @Override
        public void deviceRemoved(Registry registry, final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    ((DeviceAdapter)listView.getAdapter()).remove(device);
                }
            });
        }
    }


//        if (localService != null) {
//            this.upnpService.getControlPoint().execute(
//                    new SetAVTransportURIActionCallback(localService,
//                            this.uriString, this.metaData, mHandle,
//                            this.controlType));
//        } else {
//            Log.e("null", "null");
//        }

//        if (localService != null) {
//            Log.e("start play", "start play");
//            this.upnpService.getControlPoint().execute(
//                    new PlayerCallback(localService, mHandle));
//        } else {
//            Log.e("null", "null");
//        }

//    private void ma(){
//        // Use cling factory
//        if (factory == null)
//            new UpnpFa
    // factory = new org.droidupnp.controller.cling.Factory();
//
//        // Upnp service
//        if (upnpServiceController == null)
//            upnpServiceController = factory.createUpnpServiceController(this);
//
//    }
//
//    private void play(){
//        upnpService.getControlPoint().execute(new Play(getAVTransportService()) {
//            @Override
//            public void success(ActionInvocation invocation)
//            {
//                Log.v(TAG, "Success playing ! ");
//                // TODO update player state
//            }
//
//            @Override
//            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
//            {
//                Log.w(TAG, "Fail to play ! " + arg2);
//            }
//        });
//    }
//
//    public static Service getAVTransportService()
//    {
//        if (Main.upnpServiceController.getSelectedRenderer() == null)
//            return null;
//
//        return ((CDevice) Main.upnpServiceController.getSelectedRenderer()).getDevice().findService(
//                new UDAServiceType("AVTransport"));
//    }
}
