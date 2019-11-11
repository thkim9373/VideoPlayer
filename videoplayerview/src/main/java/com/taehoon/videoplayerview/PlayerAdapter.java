package com.taehoon.videoplayerview;

public interface PlayerAdapter {
    void loadMedia(int resId);

    void release();

    boolean isPlaying();

    void play();

    void pause();

    void fastRewind();

    void fastForward();

    void seekTo(int position);

    void initProgressCallback();
}
