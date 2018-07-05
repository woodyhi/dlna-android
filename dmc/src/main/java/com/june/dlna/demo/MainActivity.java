package com.june.dlna.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.june.dlna.component.UpnpComponent;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.FixedAndroidLogHandler;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private ListView listView;

    private UpnpComponent upnpComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device device = (Device) parent.getItemAtPosition(position);
                MediaRendererControlActivity.selectedDevice = device;
                startActivity(new Intent(getApplicationContext(), MediaRendererControlActivity.class));
            }
        });

        listView.setAdapter(new DeviceAdapter(this, new ArrayList<Device>()));

        // Fix the logging integration between java.util.logging and Android internal logging
        org.seamless.util.logging.LoggingUtil.resetRootHandler(
                new FixedAndroidLogHandler()
        );

        upnpComponent = UpnpComponent.getsInstance();
        upnpComponent.init(getApplicationContext());
        upnpComponent.addRegistryListener(registryListener);
        upnpComponent.start();

        int[] position = new int[2];
        listView.getLocationInWindow(position);
        System.out.println("++++++++++++++++++++++++ list view " + position[0] + " " + position[1]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_browser, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println(" click +++++++++++++++++++++++++++");
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                search();
                return true;
            case R.id.action_toggle_net:
                AndroidUpnpService upnpService = upnpComponent.getAndroidUpnpService();
                if (upnpService != null) {
                    Router router = upnpService.get().getRouter();
                    try {
                        if (router.isEnabled()) {
                            Toast.makeText(this, "关闭网络", Toast.LENGTH_SHORT).show();
                            router.disable();
                        } else {
                            Toast.makeText(this, "打开网络", Toast.LENGTH_SHORT).show();
                            router.enable();
                        }
                    } catch (RouterException ex) {
                        Toast.makeText(this, "网络切换出错" + ex.toString(), Toast.LENGTH_LONG).show();
                        ex.printStackTrace(System.err);
                    }
                }
                return true;
            case R.id.action_toggle_log:
                Logger logger = Logger.getLogger("org.fourthline.cling");
                if (logger.getLevel() != null && !logger.getLevel().equals(Level.INFO)) {
                    Toast.makeText(this, "关闭调试日志", Toast.LENGTH_SHORT).show();
                    logger.setLevel(Level.INFO);
                } else {
                    Toast.makeText(this, "打开调试日志", Toast.LENGTH_SHORT).show();
                    logger.setLevel(Level.FINEST);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        upnpComponent.stop();
    }

    private void search() {
        ((DeviceAdapter) listView.getAdapter()).clear();
        if (upnpComponent.getAndroidUpnpService() != null) {
            upnpComponent.getAndroidUpnpService().getRegistry().removeAllRemoteDevices();
            for (Device d : upnpComponent.getAndroidUpnpService().getRegistry().getDevices()) {
                ((DeviceAdapter) listView.getAdapter()).add(d);
            }
            upnpComponent.getAndroidUpnpService().getControlPoint().search();
        }
    }

    protected DefaultRegistryListener registryListener = new DefaultRegistryListener() {


        @Override
        public void deviceAdded(Registry registry, final Device device) {
            if ("MediaServer".equals(device.getType().getType())
                    || "MediaRenderer".equals(device.getType().getType())) {
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
            if ("MediaServer".equals(device.getType().getType())
                    || "MediaRenderer".equals(device.getType().getType())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((DeviceAdapter) listView.getAdapter()).remove(device);
                    }
                });
            }
        }
    };
}
