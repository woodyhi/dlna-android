package cn.cj.dlna;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.LocalDevice;

import cn.cj.dlna.component.UpnpComponent;
import cn.cj.dlna.dmc.ContentDirectoryCommand;
import cn.cj.dlna.dmc.LocalMediaServer;

public class LocalContentDirectoryActivity extends AppCompatActivity {
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_content_directory);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		listView = (ListView) findViewById(R.id.listview);


		browseLocalDevice();
	}

	private void browseLocalDevice(){
//		AndroidUpnpService upnpService = UpnpComponent.getsInstance(getApplicationContext()).getAndroidUpnpService();
//		LocalMediaServer localMediaServer = LocalMediaServer.getInstance(getApplicationContext());
//		LocalDevice localDevice = localMediaServer.getLocalDevice();
//		new ContentDirectoryCommand(upnpService).browse(localDevice, "0");
	}
}
