package cn.cj.dlna;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.util.ArrayList;

import cn.cj.dlna.component.DMC_Controller;
import cn.cj.dlna.component.UpnpComponent;

public class DlnaBrowserActivity extends AppCompatActivity {

    private ListView listView;

    private UpnpComponent upnpComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dlna_browser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device device = (Device) parent.getItemAtPosition(position);

                new DMC_Controller().setAVTransport(upnpComponent.getAndroidUpnpService(), device, MainActivity.PLAYURL);
            }
        });

        listView.setAdapter(new DeviceAdapter(this, new ArrayList<Device>()));


        upnpComponent = UpnpComponent.getsInstance();
        upnpComponent.init(getApplicationContext());
        upnpComponent.start();
        upnpComponent.addRegistryListener(new MediaRendererListener());
        upnpComponent.addRegistryListener(new MediaServerListener());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_browser, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                search();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void search() {
        ((DeviceAdapter) listView.getAdapter()).clear();
        if (upnpComponent.getAndroidUpnpService() != null) {
            upnpComponent.getAndroidUpnpService().getRegistry().removeAllRemoteDevices();
            upnpComponent.getAndroidUpnpService().getControlPoint().search();
        }
    }

    protected class MediaServerListener extends DefaultRegistryListener {

        @Override
        public void deviceAdded(Registry registry, final Device device) {
            if ("MediaServer".equals(device.getType().getType())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((DeviceAdapter) listView.getAdapter()).add(device);
                    }
                });
            }
        }

        @Override
        public void deviceRemoved(Registry registry, final Device device) {
            if ("MediaServer".equals(device.getType().getType())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((DeviceAdapter) listView.getAdapter()).remove(device);
                    }
                });
            }
        }
    }

    protected class MediaRendererListener extends DefaultRegistryListener {

        @Override
        public void deviceAdded(Registry registry, final Device device) {
            if ("MediaRenderer".equals(device.getType().getType())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((DeviceAdapter) listView.getAdapter()).add(device);
                    }
                });
            }
        }

        @Override
        public void deviceRemoved(Registry registry, final Device device) {
            if ("MediaRenderer".equals(device.getType().getType())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((DeviceAdapter) listView.getAdapter()).remove(device);
                    }
                });
            }
        }
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
        }

        @Override
        public void deviceRemoved(Registry registry, final Device device) {
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
