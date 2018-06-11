package com.android.media.ui;

import com.media.player.ott.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PlayerMainActivity extends Activity {

	private EditText	editText;
	private Button		play;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_activity_main);

		editText = (EditText) findViewById(R.id.edit);
		play = (Button) findViewById(R.id.play);

		play.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String s = editText.getText().toString();
				if (!TextUtils.isEmpty(s)) {
					play(s);
				}
			}
		});

		String purl = "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_640x360.m4v";
//		String purl = "file:///sdcard/test.3gp";
		editText.setText(purl);
	}

	private void play(String path) {
		Intent intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
		intent.setDataAndType(Uri.parse(path), "video/*");
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalStore.saveUrl(getApplicationContext(), editText.getText().toString());
	}

}
