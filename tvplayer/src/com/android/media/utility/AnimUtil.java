package com.android.media.utility;

import android.content.Context;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.media.player.ott.R;

/**
 * TODO description
 *
 * @auther: june.ch
 * @date: 2018/7/18 17:15
 */
public class AnimUtil {

    public static void slideInAtTop(View view){
        view.setAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.top_slide_in));
        view.setVisibility(View.VISIBLE);
    }

    public static void slideOutAtTop(View view){
        view.setAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.top_slide_out));
        view.setVisibility(View.GONE);
    }

    public static void slideInAtBottom(View view){
        view.setAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.bottom_slide_in));
        view.setVisibility(View.VISIBLE);
    }

    public static void slideOutAtBottom(View view){
        view.setAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.bottom_slide_out));
        view.setVisibility(View.GONE);
    }
}
