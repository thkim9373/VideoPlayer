package com.taehoon.videoplayerview.ExoPlayer;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class PlaybackInfoListener {

    public void onDurationChanged(long duration) {
    }

    public void onPositionChanged(long position) {
    }

    public void onStateChanged(@State int state) {
    }

    public void onPlaybackCompleted() {
    }

    @IntDef({State.IDLE, State.BUFFERING, State.READY, State.ENDED, State.PLAYING, State.PAUSED, State.FAST_REWIND, State.FAST_FORWARD, State.PREPARED})
    @Retention(RetentionPolicy.CLASS)
    @interface State {
        int IDLE = -1;
        int BUFFERING = 0;
        int READY = 1;
        int ENDED = 2;
        int PLAYING = 3;
        int PAUSED = 4;
        int FAST_REWIND = 5;
        int FAST_FORWARD = 6;
        int PREPARED = 7;
    }
}
