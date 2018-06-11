/**
 * 
 */
package com.android.media.player.video;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;

/**
 * @author June Cheng
 * @date 2015年9月25日 下午11:59:05
 */
public class MediaUtil {

	public static String formatMillisTime(long millis) {
		if (millis < 1000) {
			return "00:00";
		}

		long total_s = millis / 1000;
		int s = (int) (total_s % 60);// 秒

		long total_m = total_s / 60;
		int m = (int) (total_m % 60);

		long total_h = total_m / 60;
		int h = (int) total_h;

		if (h > 0) {
			return String.format("%02d:%02d:%02d", h, m, s);
		} else {
			return String.format("%02d:%02d", m, s);
		}
	}

	/**
	 * Convert time to a string
	 * 
	 * @param millis
	 *            e.g.time/length from file
	 * @return formated string "[hh]h[mm]min" / "[mm]min[s]s"
	 */
	public static String millisToText(long millis) {
		return millisToString(millis, true);
	}

	public static String millisToString(long millis, boolean text) {
		boolean negative = millis < 0;
		millis = java.lang.Math.abs(millis);

		millis /= 1000;
		int sec = (int) (millis % 60);
		millis /= 60;
		int min = (int) (millis % 60);
		millis /= 60;
		int hours = (int) millis;

		String time;
		DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		format.applyPattern("00");
		if (text) {
			if (millis > 0)
				time = (negative ? "-" : "") + hours + "h" + format.format(min) + "min";
			else if (min > 0)
				time = (negative ? "-" : "") + min + "min";
			else
				time = (negative ? "-" : "") + sec + "s";
		} else {
			if (millis > 0)
				time = (negative ? "-" : "") + hours + ":" + format.format(min) + ":" + format.format(sec);
			else
				time = (negative ? "-" : "") + min + ":" + format.format(sec);
		}
		return time;
	}

	public static void stop(Context context) {
		Intent freshIntent = new Intent();
		freshIntent.setAction("com.android.music.musicservicecommand.stop");
		freshIntent.putExtra("command", "stop");
		context.sendBroadcast(freshIntent);
	}
}
