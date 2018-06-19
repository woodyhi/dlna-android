package cn.cj.dlna;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import cn.cj.dlna.dms.LocalMediaServer;
import cn.cj.dlna.util.NetworkUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	public static String PLAYURL = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";

	Button browser;
	Button localContent;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		browser = (Button) findViewById(R.id.browser);
		localContent = (Button) findViewById(R.id.localContent);
		browser.setOnClickListener(this);
		localContent.setOnClickListener(this);


		startContentService();
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.browser:
				startActivity(new Intent(getApplicationContext(), DlnaBrowserActivity.class));
				break;
			case R.id.localContent:
				startActivity(new Intent(getApplicationContext(), LocalContentDirectoryActivity.class));
				break;
		}
	}

	private void startContentService(){
		new Thread(new Runnable(){
			@Override
			public void run() {

				Log.d("MainActivity", "" + NetworkUtil.isNetworkPortUsed(8192));

				LocalMediaServer localMediaServer = LocalMediaServer.getInstance(getApplicationContext());
				String s = localMediaServer.getAddress() + "\n " + 	localMediaServer.getLocalDevice()
						+ localMediaServer.getListeningPort();
				Log.d("MainActivity", s);

			}
		}).start();
	}
}
