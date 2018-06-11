/**
 * 
 */
package com.android.media.utility;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * @author June Cheng
 * @date 2015年10月30日 上午12:13:35
 */
public class Util {

	/**
	 * ScreenOrientation
	 * 
	 * @param orientation
	 * @param deltaDegrees
	 * @return
	 */
	public static final void computeV(int orientation, int deltaDegrees) {
	}

	public static int px2dp(Context context, int px) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		float logicalDensity = metrics.density;
		int dp = Math.round(px / logicalDensity);
		return dp;
	}

	public static int dp2px(Context context, int dp) {
		return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
	}

}
