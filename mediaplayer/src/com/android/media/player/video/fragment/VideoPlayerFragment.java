/**
 * 
 */
package com.android.media.player.video.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.media.player.video.MediaUtil;
import com.android.media.player.video.MyOrientationEventListener;
import com.android.media.utility.Util;
import com.media.player.ott.R;

/**
 * @author June Cheng
 * @date 2015年10月28日 下午10:28:47
 */
public class VideoPlayerFragment extends Fragment implements SurfaceHolder.Callback {
	private final String				TAG								= "MediaPlayerFragment";

	private final int					DELAY_DISMISS_POPUP_TIME		= 10000;
	private final byte					HANDLER_HIDE_PLAYER_BOTTOM_BAR	= 0x6;
	private final byte					REFRESH_PLAYBACK_PROGRESS		= 0x1;
	private Context						mContext;

	private View						rootView;
	//surfaceview's container
	private View						mSurfaceViewParent;
	private int							mPlayerWidth;												//播放器尺寸
	private int							mPlayerHeight;
	private SurfaceView					mSurfaceView;
	private boolean               		surfaceCreated;

	private static final byte			SURFACESIZE_FIT_CENTER			= 0x1;
	private static final byte			SURFACESIZE_FILL				= 0x2;
	private static final byte			SURFACESIZE_CENTER_CROP			= 0x3;
	private byte						mCurrentSurfaceSize				= SURFACESIZE_FIT_CENTER;

	// 尺寸常量
	private int							originalFrameWidth;
	private int							originalFrameHeight;
	private int							mScreenWidth;
	private int							mScreenHeight;
	private float						PLAYER_DEFAULT_ASPECT_RATIO		= 16 / 9f;

	// 视频尺寸
	private int							mVideoWidth;
	private int							mVideoHeight;

	/** center popup */
	// 加载中
	private LinearLayout				mLoadingView;
	private ProgressBar					mProgressBar;
	// 音量
	private LinearLayout				mVolumenLayout;
	private TextView					mVolumePercent;
	// 快进/快退
	private LinearLayout				mFastForwardProgressLayout;
	private TextView					mFastForwardProgresText;
	/* player bottom bar */
	private LinearLayout				mPlayerBottomBar;
	private boolean						mShowing;
	private ImageView					playPauseBtn;
	private LinearLayout				mSeekBarLayout;
	private TextView					mCurrentTime;
	private SeekBar						mSeekBar;													// 进度
	private TextView					mDurationTime;
	private ImageView					openCloseFullscreenBtn;
	private boolean						mSeeking						= false;

	private MediaPlayer					mMediaPlayer;
	private String						mMediaPath;
	private boolean						playingOnSurface				= false;
	private boolean						completed;
	private int							duration;
	private int							lastPosition;
	
	private int							videoType; // 1：点播 2：回看 3：直播

	private VideoType                     enumType = VideoType.VOD;
	public static enum VideoType{
		/** 点播 */
		VOD,
		/** 直播 */
		TV_LIVE,
		/** 回看 */
		SEEKPLAY,
		TV_SHIFT_PLAY
	}

	/** 屏幕旋转事件监听 */
	private MyOrientationEventListener	myOrientationEventListener;
	private GestureDetector				mGestureDetector;
	/** 手势 */
	public static final byte			GESTURE_NON						= 0x0;
	public static final byte			GESTURE_HORIZONTAL				= 0x1;
	public static final byte			GESTURE_VERTICAL				= 0x2;
	public byte							gestureOrientaion				= GESTURE_NON;
	private int							positionToSeek;											//手势控制播放位置

	public VideoPlayerFragment() {
	}

//	public void setMediaPath(String path) {
//		this.mMediaPath = path;
//	}
//	
//	public void setMediaPath(String path, int playedTime){
//		this.mMediaPath = path;
//		this.lastPosition = playedTime;
//	}

	public void play(String path, int playedTime, VideoType videoType) {
		enumType = videoType;
		int type = -1;
		switch (videoType){
			case VOD:
				type = 1;
				break;
			case TV_LIVE:
				type = 3;
				break;
			case SEEKPLAY:
				type = 2;
				break;
		}
		play(path, playedTime, type);
	}

	public void play(String path, int playedTime, int videoType) {
		if(path == null){
			Log.e(TAG, "********** playurl is null **********");
			return;
		}
		mMediaPath = path;
		lastPosition = playedTime;
		this.videoType = videoType;
		if(videoType == 3){
			mPlayerBottomBar.setVisibility(View.GONE);
		}

		if(mMediaPlayer != null){
			mMediaPlayer.release();	
			mMediaPlayer = null;
		}
		if(surfaceCreated){
			mLoadingView.setVisibility(View.VISIBLE);
			loadMedia(mSurfaceView.getHolder());
		}
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_media_player, null);
		rootView = view;
		initView(view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = getActivity();
		// 获取屏幕尺寸
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels;
		mScreenHeight = dm.heightPixels;

		// 设置surfaceview容器大小
		mPlayerWidth = originalFrameWidth = mScreenWidth;
		mPlayerHeight = originalFrameHeight = (int) (mScreenWidth / PLAYER_DEFAULT_ASPECT_RATIO);
		SurfaceHolder surfaceHolder = mSurfaceView.getHolder();//SurfaceHolder是SurfaceView的控制接口
		surfaceHolder.addCallback(this);

		changeSurfaceViewSize(mSurfaceView, mPlayerWidth, mPlayerHeight, mVideoWidth, mVideoHeight);

		//	surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//Surface类型

		// 屏幕旋转
		myOrientationEventListener = new MyOrientationEventListener(mContext, SensorManager.SENSOR_DELAY_NORMAL);
		if (myOrientationEventListener.canDetectOrientation()) {
			Log.d(TAG, "can Detect Orientation");
//			myOrientationEventListener.enable();
		} else {
			Log.d(TAG, "can not Detect Orientation");
		}
		mGestureDetector = new GestureDetector(mContext, new MySimpleGestureListener());
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()");
	}

	/** when window lose focus then pause player, or get focus then start player */
	public void onWindowFocusChanged(boolean hasFocus) {
		Log.d(TAG, "onWindowFocusChanged : " + hasFocus);
		if (hasFocus) {
			if (playingOnSurface && !mMediaPlayer.isPlaying()) {
				mMediaPlayer.start();
				playPauseBtn.setBackgroundResource(R.drawable.ic_media_pause);
			}
		} else {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				playPauseBtn.setBackgroundResource(R.drawable.ic_media_play);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, " onPause() ");
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			Log.e(TAG, this.getClass().getName() + " pause, toggle playback button : pause");
			playPauseBtn.setBackgroundResource(R.drawable.ic_media_play);
		}
		if (duration - lastPosition < 5000) {// almost completed
			lastPosition = 0;
		} else {
			// currentPosition -= 3000;// go back few seconds, to compensate loading_a time
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	private void initView(View view) {
		mSurfaceViewParent = view.findViewById(R.id.surfaceview_parent);
		mSurfaceView = (SurfaceView) view.findViewById(R.id.surfaceview);
		mLoadingView = (LinearLayout) view.findViewById(R.id.loading_view);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);
		mVolumenLayout = (LinearLayout) view.findViewById(R.id.volume_layout);
		mVolumePercent = (TextView) view.findViewById(R.id.volume_percent_tv);
		mFastForwardProgressLayout = (LinearLayout) view.findViewById(R.id.fast_forward_progress_layout);
		mFastForwardProgresText = (TextView) view.findViewById(R.id.fast_forward_progress_text);
		mPlayerBottomBar = (LinearLayout) view.findViewById(R.id.player_bottom_bar);
		playPauseBtn = (ImageView) view.findViewById(R.id.play_pause_btn);
		mSeekBarLayout = (LinearLayout) view.findViewById(R.id.seekbar_layout);
		mCurrentTime = (TextView) view.findViewById(R.id.video_playtime);
		mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
		mDurationTime = (TextView) view.findViewById(R.id.video_durationtime);
		openCloseFullscreenBtn = (ImageView) view.findViewById(R.id.open_close_fullscreen);

		mSurfaceViewParent.setOnTouchListener(new MyTouchListener());
		playPauseBtn.setEnabled(false);
		playPauseBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				playPausePlayer();
			}
		});
		mSeekBar.setEnabled(false);
		mSeekBar.setOnSeekBarChangeListener(new PlaybackSeekBarChangeListener());
		openCloseFullscreenBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int orientaion = getResources().getConfiguration().orientation;
				if (orientaion == Configuration.ORIENTATION_PORTRAIT) {
					((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					openCloseFullscreenBtn.setBackgroundResource(R.drawable.close_fullscreen);
				} else if (orientaion == Configuration.ORIENTATION_LANDSCAPE) {
					((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					openCloseFullscreenBtn.setBackgroundResource(R.drawable.close_fullscreen);
				}
			}
		});
	}

	private void loadMedia(SurfaceHolder holder) {
		Log.e(TAG, "playOrPause loadmedia pause");
		if (mMediaPlayer == null) {
			Log.d(TAG, "mediaplayer instance is null , creating");
			playPauseBtn.setBackgroundResource(R.drawable.ic_media_pause);
			mMediaPlayer = createMediaPlayer(holder, mMediaPath);

		} else {
			Log.d(TAG, "mediaplayer instance is existsing");
			try {
				mMediaPlayer.setDisplay(holder);// reset surface
				if (playingOnSurface) {
					mLoadingView.setVisibility(View.VISIBLE);
					playPauseBtn.setBackgroundResource(R.drawable.ic_media_pause);
					mMediaPlayer.seekTo(lastPosition);
				}
				showOverlay(playingOnSurface);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private MediaPlayer createMediaPlayer(SurfaceHolder holder, String cur) {
		if(cur == null){
			return null;
		}
		final MediaPlayer mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setDisplay(holder);
		mMediaPlayer.setScreenOnWhilePlaying(true);// works when setDisplay invoked

		mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.d(TAG, "onPrepared");
				if (lastPosition > 0) {
					Log.i(TAG, "xxxxxxxx seekTo " + lastPosition);
					mMediaPlayer.seekTo(lastPosition);
					mMediaPlayer.start();
				} else {
					Log.i(TAG, "xxxxxxxx mediaplayer start");
					mMediaPlayer.start();
				}
				playingOnSurface = true;
				playPauseBtn.setEnabled(true);
				mSeekBar.setEnabled(true);
				showOverlay(true);
			}
		});
		mMediaPlayer.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {

			@Override
			public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
				Log.d(TAG, "video size changed width=" + width + ", height=" + height);
				if (width * height == 0) {
					return;
				}
				mVideoWidth = width;
				mVideoHeight = height;
				changeSurfaceViewSize(mSurfaceView, mPlayerWidth, mPlayerHeight, mVideoWidth, mVideoHeight);
			}
		});
		mMediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				Log.d(TAG, "mediaplayer buffered progress : " + percent + "%");
				mSeekBar.setSecondaryProgress(mSeekBar.getMax() * percent / 100);
			}
		});
		mMediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {

			@Override
			public void onSeekComplete(MediaPlayer mp) {
				Log.d(TAG, "onSeekComplete");
				mLoadingView.setVisibility(View.GONE);
			}
		});
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				Log.d(TAG, "onCompletion " + mp.toString());
				completed = true;
			}
		});
		mMediaPlayer.setOnInfoListener(new OnInfoListener() {

			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				Log.d(TAG, "onInfo what = " + what + ", extra = " + extra);
				switch (what) {
				case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
					Log.d(TAG, "info MEDIA_INFO_BAD_INTERLEAVING");
					break;
				case MediaPlayer.MEDIA_INFO_BUFFERING_END:
					if (mLoadingView.getVisibility() == View.VISIBLE)
						mLoadingView.setVisibility(View.GONE);
					Log.d(TAG, "info MEDIA_INFO_BUFFERING_END");
					break;
				case MediaPlayer.MEDIA_INFO_BUFFERING_START:
					if (mLoadingView.getVisibility() == View.GONE)
						mLoadingView.setVisibility(View.VISIBLE);
					/* 偶尔这个事件被回调后，视频一直不动，mediaplayer状态是playing、网络正常，不知道是不是视频流的问题 */
					Log.d(TAG, "info MEDIA_INFO_BUFFERING_START");
					break;
				case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
					Log.d(TAG, "info MEDIA_INFO_METADATA_UPDATE");
					break;
				case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
					mSeekBar.setEnabled(false);
					Log.d(TAG, "info MEDIA_INFO_NOT_SEEKABLE");
					mSeekBarLayout.setVisibility(View.INVISIBLE);
					break;
				case MediaPlayer.MEDIA_INFO_UNKNOWN:
					Log.d(TAG, "info MEDIA_INFO_UNKNOWN");
					break;
				case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
					Log.d(TAG, "info MEDIA_INFO_VIDEO_TRACK_LAGGING");
					break;
				}
				return false;
			}
		});
		mMediaPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.e(TAG, "onError what = " + what + ", extra = " + extra);
				switch (what) {
				case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
					Log.e(TAG, "error MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
					break;
				case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
					Log.e(TAG, "error MEDIA_ERROR_SERVER_DIED");
					break;
				case MediaPlayer.MEDIA_ERROR_UNKNOWN:
					Log.e(TAG, "error MEDIA_ERROR_UNKNOWN");
					mLoadingView.setVisibility(View.GONE);
					playPauseBtn.setEnabled(true);
					Toast.makeText(mContext, "未知错误", Toast.LENGTH_SHORT).show();
					break;
				}
				return false;
			}
		});

		//设置显示视频显示在SurfaceView上
		try {
			mMediaPlayer.setDataSource(cur);
			mMediaPlayer.prepareAsync();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mMediaPlayer;
	}

	public void playPausePlayer() {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				playingOnSurface = false;
				playPauseBtn.setBackgroundResource(R.drawable.ic_media_play);
			} else {
				mMediaPlayer.start();
				playingOnSurface = true;
				playPauseBtn.setBackgroundResource(R.drawable.ic_media_pause);
			}
		}
		showOverlay(true);
	}

	/**
	 * change player size & surface size
	 * 
	 * @param surfaceView
	 * @param playerWidth
	 * @param playerHeight
	 * @param videoWidth
	 * @param videoHeight
	 */
	private void changeSurfaceViewSize(SurfaceView surfaceView, int playerWidth, int playerHeight, int videoWidth, int videoHeight) {
		Log.d(TAG, "changeSurfaceViewSize() -> surfaceview last size : w=" + surfaceView.getWidth() + ", h=" + surfaceView.getHeight());
		Log.d(TAG, "player: w=" + playerWidth + ", h=" + playerHeight + ", video: w=" + videoWidth + ", h=" + videoHeight);

		// sanity check
		if (playerWidth * playerHeight == 0) {
			return;
		}

		int dw = playerWidth;
		int dh = playerHeight;

		if (videoWidth * videoHeight != 0) {
			float ar = (float) dw / (float) dh;
			Log.d(TAG, "surfaceview's ParentView aspect ratio is " + ar);
			float v_ar = (float) videoWidth / (float) videoHeight;
			Log.d(TAG, "video aspect ratio is " + v_ar);

			switch (mCurrentSurfaceSize) {
			case SURFACESIZE_FIT_CENTER:
				// fit view's size
				if (ar > v_ar) {
					dw = (int) (dh * v_ar);
				} else {
					dh = (int) (dw / v_ar);
				}
				break;
			case SURFACESIZE_FILL:
				break;
			case SURFACESIZE_CENTER_CROP:
				if (ar > v_ar) {
					dh = (int) (dw / v_ar);
				} else {
					dw = (int) (dh * v_ar);
				}
				break;
			}
		}

		// surface size
		ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
		lp.width = dw;
		lp.height = dh;
		Log.d(TAG, "now surfaceview size -> width=" + dw + ", height=" + dh);
		surfaceView.setLayoutParams(lp);

		// frame size
		LinearLayout.LayoutParams p_lp = (LayoutParams) mSurfaceViewParent.getLayoutParams();
		p_lp.gravity = Gravity.CENTER;
		p_lp.width = playerWidth;
		p_lp.height = playerHeight;
		mSurfaceViewParent.setLayoutParams(p_lp);
		surfaceView.invalidate();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "onConfigurationChanged() " + newConfig.orientation);
		super.onConfigurationChanged(newConfig);

		WindowManager.LayoutParams params = ((Activity) mContext).getWindow().getAttributes();

		switch (newConfig.orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			// 设置非全屏(show taskbar)
			params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			((Activity) mContext).getWindow().setAttributes(params);
			((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

			// change player size
			openCloseFullscreenBtn.setBackgroundResource(R.drawable.open_fullscreen);
			mPlayerWidth = originalFrameWidth;
			mPlayerHeight = originalFrameHeight;
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			// 设置全屏(hide taskbar)
			params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			((Activity) mContext).getWindow().setAttributes(params);
			((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

			// change player size
			openCloseFullscreenBtn.setBackgroundResource(R.drawable.close_fullscreen);
			mPlayerWidth = mScreenHeight;
			mPlayerHeight = mScreenWidth;
			break;
		}

		changeSurfaceViewSize(mSurfaceView, mPlayerWidth, mPlayerHeight, mVideoWidth, mVideoHeight);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		String _format = "other/unknown";
		if (format == PixelFormat.RGBX_8888)
			_format = "RGBX_8888";
		else if (format == PixelFormat.RGB_565)
			_format = "RGB_565";
		else if (format == ImageFormat.YV12)
			_format = "YV12";

		Log.d(TAG, "surfaceChanged -> PixelFormat is " + _format + ", width = " + width + ", height = " + height);

		changeSurfaceViewSize(mSurfaceView, mPlayerWidth, mPlayerHeight, mVideoWidth, mVideoHeight);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated");
		surfaceCreated = true;
		loadMedia(holder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");
		surfaceCreated = false;
	}

	/** 显示进度条 */
	private void showOverlay(boolean fadeout) {
		Log.d(TAG, "showOverlay() : visible");
		if(enumType == VideoType.TV_LIVE || enumType == VideoType.TV_SHIFT_PLAY){
			return;
		}
		myHandler.sendEmptyMessage(REFRESH_PLAYBACK_PROGRESS);
		if (!mShowing) {
			mShowing = true;
			mPlayerBottomBar.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.show_up));
			mPlayerBottomBar.setVisibility(View.VISIBLE);
		}
		myHandler.removeMessages(HANDLER_HIDE_PLAYER_BOTTOM_BAR);
		if (fadeout)
			myHandler.sendEmptyMessageDelayed(HANDLER_HIDE_PLAYER_BOTTOM_BAR, DELAY_DISMISS_POPUP_TIME);
	}

	/** 隐藏进度条 */
	private void hideOverlay() {
		Log.d(TAG, "hideOverlay() : hiden");
		if (mShowing) {
			mShowing = false;
			mPlayerBottomBar.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.hide_down));
			mPlayerBottomBar.setVisibility(View.GONE);
		}
	}

	/** refresh playback progress */
	private void refreshProgress() {
		Log.e(TAG, "refresh progress ffffff");
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			Log.e(TAG, "refresh progress xxxx");
			duration = mMediaPlayer.getDuration();
			int currentPosition = mMediaPlayer.getCurrentPosition();
			lastPosition = currentPosition;
			Log.v(TAG, "duration=" + duration + ", currentPosition=" + currentPosition);

			if (duration > 0 && currentPosition <= duration) {
				if (!mSeeking) {
					Log.e(TAG, "vvvvvvvvvvvvvvvvvvvvvv");
					mSeekBar.setMax(duration);
					mSeekBar.setProgress(currentPosition);
				}

				mCurrentTime.setText(MediaUtil.formatMillisTime(currentPosition));
				mDurationTime.setText(MediaUtil.formatMillisTime(duration));
			}
			if (duration == 0) {
				mSeekBarLayout.setVisibility(View.INVISIBLE);
			}
			if (mLoadingView.getVisibility() != View.GONE)
				mLoadingView.setVisibility(View.GONE);

			lastPosition = currentPosition;
		}
	}

	Handler	myHandler	= new Handler() {
							@Override
							public void handleMessage(android.os.Message msg) {
								switch (msg.what) {
								case HANDLER_HIDE_PLAYER_BOTTOM_BAR:
									hideOverlay();
									break;
								case REFRESH_PLAYBACK_PROGRESS:
									removeMessages(REFRESH_PLAYBACK_PROGRESS);// avoid multi messge in the queue
									refreshProgress();
									Log.i(TAG, "REFRESH_PLAYBACK_PROGRESS sssssss msg.hashCode():" + msg.hashCode());
									if (mShowing && mMediaPlayer != null && mMediaPlayer.isPlaying()) {
										if (mPlayerBottomBar.getVisibility() == View.VISIBLE) {
											Message m = obtainMessage(REFRESH_PLAYBACK_PROGRESS);
											myHandler.sendMessageDelayed(m, 1000 - lastPosition % 1000);
										}
									} else {
										Log.e(TAG, " cant refresh progress");
									}
									break;
								default:
									break;
								}
							}
						};

	private class PlaybackSeekBarChangeListener implements OnSeekBarChangeListener {
		int	tmpSeekProgress	= 0;

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if (tmpSeekProgress >= 0 && mMediaPlayer != null) {
				Log.d(TAG, "seek To " + tmpSeekProgress);
				mMediaPlayer.seekTo(tmpSeekProgress);
			}
			mSeeking = false;
			mLoadingView.setVisibility(View.VISIBLE);
			showOverlay(true);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			Log.d(TAG, "onStartTrackingTouch");
			mSeeking = true;
			tmpSeekProgress = 0;
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			Log.v(TAG, "onProgressChanged  progress=" + progress);
			if (fromUser) {
				tmpSeekProgress = progress;
			}
		}
	}

	private class MyTouchListener implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			Log.i(TAG, " MyTouchListener " + event.getAction());
			if (event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
				// avoid multi point action causing confusion
				return true;
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				mVolumenLayout.setVisibility(View.GONE);
				mFastForwardProgressLayout.setVisibility(View.GONE);

				if (gestureOrientaion == GESTURE_HORIZONTAL) {
					if (mMediaPlayer != null) {
						showOverlay(true);
						mMediaPlayer.seekTo(positionToSeek);
					}
				} else if (gestureOrientaion == GESTURE_VERTICAL) {
				}
				gestureOrientaion = GESTURE_NON;
			} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
				mVolumenLayout.setVisibility(View.GONE);
				mFastForwardProgressLayout.setVisibility(View.GONE);
			}
			if (mGestureDetector.onTouchEvent(event)) {
				return true;
			}
			return false;
		}
	}

	private class MySimpleGestureListener extends SimpleOnGestureListener {
		private AudioManager	audioMgr;
		private int				maxVolume;
		private int             currentVolume;
		private boolean			gestureDown	= false;
		private float			volumePercent;

		public MySimpleGestureListener() {
			audioMgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
			maxVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			currentVolume = audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
			volumePercent = (float)currentVolume / (float)maxVolume;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Log.d(TAG, "onSingleTapUp " + e.getAction());
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Log.d(TAG, "onSingleTapConfirmed " + e.getAction());
			if (e.getAction() == MotionEvent.ACTION_DOWN) {
				if (mShowing) {
					hideOverlay();
				} else {
					showOverlay(true);
				}
			}
			return super.onSingleTapConfirmed(e);
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Log.d(TAG, "onDoubleTap " + e.getAction());
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			Log.d(TAG, "onDoubleTapEvent " + e.getAction());
			if (e.getAction() == MotionEvent.ACTION_UP) {
				if (mCurrentSurfaceSize == SURFACESIZE_FIT_CENTER) {
					mCurrentSurfaceSize = SURFACESIZE_CENTER_CROP;
				} else {
					mCurrentSurfaceSize = SURFACESIZE_FIT_CENTER;
				}
				changeSurfaceViewSize(mSurfaceView, mPlayerWidth, mPlayerHeight, mVideoWidth, mVideoHeight);
				return true;
			}
			return super.onDoubleTapEvent(e);
		}

		@Override
		public void onShowPress(MotionEvent e) {
			Log.d(TAG, "onShowPress " + e.getAction());
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			Log.i(TAG, "e1:x=" + e1.getRawX() + ",y=" + e1.getRawY() + ", e2:x=" + e2.getRawX() + ",y=" + e2.getRawY() + ",  distanceX=" + distanceX + ", distanceY=" + distanceY);
			float X = e2.getRawX() - e1.getRawX();
			float Y = e2.getRawY() - e1.getRawY();
			Log.i(TAG, "X = " + X + ", Y = " + Y);
			if (gestureDown) {// 1、首先，判定此次手势方向
				Log.d(TAG, "onScroll gestureDown");
				if (Math.abs(Y) - Math.abs(X) > 1) {
					gestureOrientaion = GESTURE_VERTICAL;
					mVolumenLayout.setVisibility(View.VISIBLE);
				} else if (Math.abs(X) - Math.abs(Y) > 1) {
					Log.e(TAG, "xxxxxxxx" + (mSeekBar.isEnabled()));
					if (mSeekBarLayout.getVisibility() == View.VISIBLE) {
						gestureOrientaion = GESTURE_HORIZONTAL;
						mFastForwardProgressLayout.setVisibility(View.VISIBLE);
					}
				}
				gestureDown = false;
			} else { // 2、然后，处理

				// 垂直方向
				if (gestureOrientaion == GESTURE_VERTICAL) {
					int y = Util.px2dp(mContext, (int)Y); // 优化滑动的流畅性
					Log.d(TAG, "yyyyyyyy " + y);
					if(0 <= volumePercent && volumePercent <= 100) {
						volumePercent -= y / 50;
						if(volumePercent < 0){
							volumePercent = 0;
						}
						if(volumePercent > 100){
							volumePercent = 100;
						}
					}
					if (volumePercent > 0) {
						mVolumePercent.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.ic_lock_silent_mode_off), null,
								null, null);
					} else {
						mVolumePercent.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.ic_lock_silent_mode), null, null,
								null);
					}
					mVolumePercent.setText((int) volumePercent + "%");
					int volume = (int) (volumePercent / 100 * maxVolume);
					audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
				}
				// 水平
				else if (gestureOrientaion == GESTURE_HORIZONTAL) {
					if (positionToSeek >= 0 || positionToSeek <= duration) {
						int x = Util.px2dp(mContext, (int)X); // 优化滑动的流畅性
						Log.d(TAG, "xxxxxxx " + x);
						positionToSeek = lastPosition + x * 1000; //根据X计算快进/后退时间
						String b = MediaUtil.formatMillisTime(positionToSeek) + "/" + MediaUtil.formatMillisTime(duration);
						mFastForwardProgresText.setText(b);
					}
				}
			}

			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			Log.d(TAG, "onDown X=" + e.getX() + ", Y=" + e.getY() + ", rawX=" + e.getRawX() + ", rawY=" + e.getRawY());
			int delta = mPlayerBottomBar.getHeight();
			Log.d(TAG, "PlayerBottomBar.getHeight() " + delta);
			// 设置手势有效区域
//			if (e.getX() > delta && e.getY() > delta && mPlayerWidth - e.getX() > delta && mPlayerHeight - e.getY() > delta) {
//				Log.d(TAG, " gesture is at available region");
				gestureDown = true;
//			} else {
				// any down action will result in setting gestureDown true on the available rect region
//				gestureDown = false;
//				Log.d(TAG, " gesture is unavailable ");
//			}
			return true;
		}
	}

	
	long tt;
	
	/** 前进or后退 **/
	private void forwardOrRewind(int X){
//		if(System.currentTimeMillis() - tt < 500){
//			tt = System.currentTimeMillis();
//			return;
//		}
		if (positionToSeek >= 0 || positionToSeek <= duration) {
			positionToSeek = lastPosition + X; //根据X计算快进/后退时间
			if(positionToSeek < 0){
				positionToSeek = 0;
			}
			if(positionToSeek > duration){
				positionToSeek = duration;
			}
			String b = MediaUtil.formatMillisTime(positionToSeek) + "/" + MediaUtil.formatMillisTime(duration);
			mFastForwardProgresText.setText(b);
		}
		showOverlay(true);
		if(mMediaPlayer != null)
			mMediaPlayer.seekTo(positionToSeek);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event){
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
				if(mShowing){
					hideOverlay();
				}else {
					showOverlay(true);
				}
				return true;
			case KeyEvent.KEYCODE_DPAD_LEFT:
                if(event.getRepeatCount() == 0){
                    event.startTracking();
                }
				if(videoType != 3)
					forwardOrRewind(-30000);
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(event.getRepeatCount() == 0){
                    event.startTracking();
                }
				if(videoType != 3)
					forwardOrRewind(30000);
				return true;
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				playPausePlayer();
				return true;
			default:
				break;
		}
		return false;
	}

	private int totalDeltaProgress;
    private long tmpTime;

	private void fORr(int val){
	    if(System.currentTimeMillis() - tmpTime < 300){
	        tmpTime = System.currentTimeMillis();
	        totalDeltaProgress += val;
        }else{
	        tmpTime = System.currentTimeMillis();
	        totalDeltaProgress += val;
        }
    }

}
