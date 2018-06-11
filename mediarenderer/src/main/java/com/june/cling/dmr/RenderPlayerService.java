/*
 * RenderPlayerService.java
 * Description:
 * Author: zxt
 */

package com.june.cling.dmr;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import com.android.media.ui.VideoPlayerActivity;

public class RenderPlayerService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleIntent(Intent intent) {
        String type = intent.getStringExtra("type");
        if (intent == null)
            return;

        Intent intent2 = new Intent();
        if (type.equals("video")) {
            intent2.setClass(getApplicationContext(), VideoPlayerActivity.class);
            intent2.setDataAndType(Uri.parse(intent.getStringExtra("playURI")), "video/*");
            startActivity(intent2);
        }
    }



//			Intent intent2;
//			if (type.equals("audio")) {
//				intent2 = new Intent(this, GPlayer.class);
//				intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				intent2.putExtra("name", intent.getStringExtra("name"));
//				intent2.putExtra("playURI", intent.getStringExtra("playURI"));
//				startActivity(intent2);
//			} else if (type.equals("video")) {
//				intent2 = new Intent(this, GPlayer.class);
//				intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				intent2.putExtra("name", intent.getStringExtra("name"));
//				intent2.putExtra("playURI", intent.getStringExtra("playURI"));
//				startActivity(intent2);
//			} else if (type.equals("image")) {
//				intent2 = new Intent(this, ImageDisplay.class);
//				intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				intent2.putExtra("name", intent.getStringExtra("name"));
//				intent2.putExtra("playURI", intent.getStringExtra("playURI"));
//				intent2.putExtra("isRender", true);
//				startActivity(intent2);
//			} else {
//				intent2 = new Intent(Action.DMR);
//				intent2.putExtra("playpath", intent.getStringExtra("playURI"));
//				sendBroadcast(intent2);
//			}
}
