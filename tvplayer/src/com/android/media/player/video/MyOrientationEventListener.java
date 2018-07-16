/**
 * 
 */
package com.android.media.player.video;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.util.Log;
import android.view.OrientationEventListener;

/**
 * 播放器随设备旋转
 * 
 * @author June Cheng
 * @date 2015年9月21日 上午12:28:14
 */
public class MyOrientationEventListener extends OrientationEventListener {
	private final String	TAG						= MyOrientationEventListener.class.getSimpleName();

	private final int		deltaDegrees			= 10;
	private int				mLastScreenOrientation	= -1;
	private int				mCurrentScreenOrientation;

	private Context			mContext;

	public MyOrientationEventListener(Context context) {
		super(context);
		this.mContext = context;
	}

	public MyOrientationEventListener(Context context, int rate) {
		super(context, rate);
		this.mContext = context;
	}

	@Override
	public void onOrientationChanged(int orientation) {
		//	Log.v(TAG, "onOrientationChanged to orientation in " + orientation + " degrees");
		// 当接近水平位置时，可能传递的值
		if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
			return;
		} else if (deltaDegrees < orientation && orientation < 90 - deltaDegrees) {
			return;
		} else if (90 + deltaDegrees < orientation && orientation < 180 - deltaDegrees) {
			return;
		} else if (180 + deltaDegrees < orientation && orientation < 270 - deltaDegrees) {
			return;
		} else if (270 + deltaDegrees < orientation && orientation < 360 - deltaDegrees) {
			return;
		}

		if (360 - 15 < orientation || orientation < 15) {
			mCurrentScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			Log.v(TAG, "SCREEN_ORIENTATION_PORTRAIT " + mCurrentScreenOrientation);
		} else if (270 - 15 < orientation && orientation < 270 + 15) {
			mCurrentScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			Log.v(TAG, "SCREEN_ORIENTATION_LANDSCAPE " + mCurrentScreenOrientation);
		} else if (180 - 15 < orientation && orientation < 180 + 15) {
			mCurrentScreenOrientation = Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
					: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			Log.v(TAG, "SCREEN_ORIENTATION_REVERSE_PORTRAIT " + mCurrentScreenOrientation);
		} else if (90 - 15 < orientation && orientation < 90 + 15) {
			mCurrentScreenOrientation = Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
					: ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			Log.v(TAG, "SCREEN_ORIENTATION_REVERSE_LANDSCAPE " + mCurrentScreenOrientation);
		}

		if (mLastScreenOrientation != mCurrentScreenOrientation) {
			((Activity) mContext).setRequestedOrientation(mCurrentScreenOrientation);
			mLastScreenOrientation = mCurrentScreenOrientation;
		}
	}

}
