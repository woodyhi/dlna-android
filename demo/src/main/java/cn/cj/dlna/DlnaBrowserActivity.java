package cn.cj.dlna;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.util.ArrayList;

import cn.cj.dlna.component.DMC_Controller;
import cn.cj.dlna.component.UpnpComponent;

public class DlnaBrowserActivity extends AppCompatActivity {

    private Button search;
    private ListView listView;

    private BrowseRegistryListener registryListener = new BrowseRegistryListener();


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

                new DMC_Controller().setUri(UpnpComponent.getsInstance(getApplicationContext()).getAndroidUpnpService(), device, MainActivity.PLAYURL);
            }
        });
        listView.setAdapter(new DeviceAdapter(this, new ArrayList<Device>()));


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.setAdapter(new DeviceAdapter(DlnaBrowserActivity.this, new ArrayList<Device>()));
                if(UpnpComponent.getsInstance(getApplicationContext()).getAndroidUpnpService() != null){
                    UpnpComponent.getsInstance(getApplicationContext()).getAndroidUpnpService().getRegistry().removeAllRemoteDevices();
                    UpnpComponent.getsInstance(getApplicationContext()).getAndroidUpnpService().getControlPoint().search();
                }
            }
        });

        UpnpComponent.getsInstance(getApplicationContext()).addRegistryListener(registryListener);
        UpnpComponent.getsInstance(getApplicationContext()).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected class BrowseRegistryListener extends DefaultRegistryListener {

        /* Discovery performance optimization for very slow Android devices! */
        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            System.out.println("************* remoteDeviceDiscoveryStarted");
//            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(
                            getApplicationContext(),
                            "Discovery failed of '" + device.getDisplayString() + "': "
                                    + (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"),
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
            deviceRemoved(device);
        }
        /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            System.out.println("************* remoteDeviceAdded");
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            System.out.println("************* localDeviceAdded");
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Service localService = device.findService(new UDAServiceType("AVTransport"));
//                    if(localService != null){
                        ((DeviceAdapter)listView.getAdapter()).add(device);
//                    }
                    Log.d("----------deviceAdded", device.getDisplayString()
                    + "\n" + device.getDetails().getFriendlyName() // 用这个名字显示设备
                    + "\n" + device.getDetails().getSerialNumber()
                    + "\n" + device.getDetails().getUpc()
                    + "\n" + device.getDetails().getBaseURL()
                    + "\n" + device.getDetails().getPresentationURI()
                    + "\n" + device.getDetails().getModelDetails().getModelName());
                }
            });


        }

        public void deviceRemoved(final Device device) {
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
//            factory = new org.droidupnp.controller.cling.Factory();
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
