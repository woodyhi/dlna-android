package com.android.media.player.video;

/**
 * TODO description
 *
 * @auther: june.ch
 * @date: 2018/7/16 14:56
 */
public interface IPlayerController {
    void play();

    void pause();

    void stop();

    void seek(int position);
}
