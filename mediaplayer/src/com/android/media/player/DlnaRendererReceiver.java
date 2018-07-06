package com.android.media.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DlnaRendererReceiver extends BroadcastReceiver {
    public static final String DMR = "com.zxt.droiddlna.action.dmr";

    public static final String PLAY = "com.zxt.droiddlna.action.play";
    public static final String PAUSE = "com.zxt.droiddlna.action.pause";
    public static final String STOP = "com.zxt.droiddlna.action.stop";


    private Callback callback;

    public DlnaRendererReceiver(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DMR.equals(action)) {
            String helpAction = intent.getStringExtra("helpAction");
            if (STOP.equals(helpAction)) {
                if (callback != null) {
                    callback.stop();
                }
            }

        }

    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void stop();
    }
}
