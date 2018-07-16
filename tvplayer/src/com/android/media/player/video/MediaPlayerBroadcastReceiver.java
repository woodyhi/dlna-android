package com.android.media.player.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 
 * @author June Cheng
 * @date 2015年10月29日 下午9:02:51
 */
public class MediaPlayerBroadcastReceiver extends BroadcastReceiver {
	private static final String	TAG					= "";
	public static final String	PLAYSTATE_CHANGED	= "com.android.music.playstatechanged";
	public static final String	META_CHANGED		= "com.android.music.metachanged";
	public static final String	QUEUE_CHANGED		= "com.android.music.queuechanged";
	public static final String	PLAYBACK_COMPLETE	= "com.android.music.playbackcomplete";
	public static final String	ASYNC_OPEN_COMPLETE	= "com.android.music.asyncopencomplete";
	public static final String	SERVICECMD			= "com.android.music.musicservicecommand";
	public static final String	CMDNAME				= "command";
	public static final String	CMDTOGGLEPAUSE		= "togglepause";
	public static final String	CMDSTOP				= "stop";
	public static final String	CMDPAUSE			= "pause";
	public static final String	CMDPREVIOUS			= "previous";
	public static final String	CMDNEXT				= "next";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		 String cmd = intent.getStringExtra("command");  
//		if(){
			
//		}
	}
}
