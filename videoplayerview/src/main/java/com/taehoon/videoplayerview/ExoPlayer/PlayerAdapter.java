package com.taehoon.videoplayerview.ExoPlayer;

import android.net.Uri;

public interface PlayerAdapter {
    void loadMedia(int resId);

    void loadMediaFromUri(Uri uri);

    void release();

    boolean isPlaying();

    void play();

    void pause();

    void fastRewind();

    void fastForward();

    void seekTo(int position);

    void replay();

    void playSpeedChange(float speed);

    void initProgressCallback();
}
