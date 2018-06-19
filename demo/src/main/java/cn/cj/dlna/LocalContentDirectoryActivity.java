package cn.cj.dlna;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.LocalDevice;

import cn.cj.dlna.component.UpnpComponent;
import cn.cj.dlna.dms.ContentDirectoryCommand;
import cn.cj.dlna.dms.LocalMediaServer;

public class LocalContentDirectoryActivity extends AppCompatActivity {
	private ListView listView;

	UpnpComponent upnpComponent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_content_directory);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		listView = (ListView) findViewById(R.id.listview);

		upnpComponent = UpnpComponent.getsInstance();
		upnpComponent.init(this);

		upnpComponent.setConnectionCallback(new UpnpComponent.ConnectionCallback() {
            @Override
            public void onConnected(AndroidUpnpService upnpService) {
                browseLocalDevice();
            }

            @Override
            public void onDisconnected() {

            }
        });
	}

	private void browseLocalDevice(){
		AndroidUpnpService upnpService = upnpComponent.getAndroidUpnpService();
		LocalMediaServer localMediaServer = LocalMediaServer.getInstance(getApplicationContext());
		LocalDevice localDevice = localMediaServer.getLocalDevice();
		new ContentDirectoryCommand(upnpService).browse(localDevice, "0");
	}

	@Override
	protected void onStart() {
		super.onStart();
		upnpComponent.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		upnpComponent.stop();
	}
}
