package com.android.media.ui;

import android.content.Context;

/**
 * 
 * @author June Cheng
 * @date 2015年10月28日 上午10:54:18
 */
public class LocalStore {

	public static final String name = "url";

	public static void saveUrl(Context context, String value) {
		context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().putString(name, value).commit();
	}

	public static String getUrl(Context context) {
		return context.getSharedPreferences(name, Context.MODE_PRIVATE).getString(name, "");
	}
}
