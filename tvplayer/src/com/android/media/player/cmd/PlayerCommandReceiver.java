package com.android.media.player.cmd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PlayerCommandReceiver extends BroadcastReceiver {

    private Callback callback;

    public PlayerCommandReceiver(){

    }

    public PlayerCommandReceiver(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Action.DMR.equals(action)) {
            String helpAction = intent.getStringExtra("helpAction");
            System.out.println("444 " + helpAction);
            if (callback != null) {
                switch (helpAction) {
                    case Action.PLAY:
                        callback.play();
                        break;
                    case Action.PAUSE:
                        callback.pause();
                        break;
                    case Action.STOP:
                        callback.stop();
                        break;
                    case Action.SEEK:
                        int position = intent.getIntExtra("position", 0);
                        callback.seek(position);
                        break;
                    case Action.SET_VOLUME:
                        double volume = intent.getDoubleExtra("volume", 0);
                        break;
                }
            }
        }

    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void play();
        void pause();
        void stop();
        void seek(int position);
        void setVolume(double volume);
    }
}
