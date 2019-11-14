package com.taehoon.videoplayerview;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class PlaybackInfoListener {

    public void onDurationChanged(int duration) {
    }

    public void onPositionChanged(int position) {
    }

    public void onStateChanged(@State int state) {
    }

    public void onPlaybackCompleted() {
    }

    @IntDef({State.INVALID, State.PREPARED, State.PLAYING, State.PAUSED, State.FAST_REWIND, State.FAST_FORWARD, State.COMPLETED})
    @Retention(RetentionPolicy.CLASS)
    @interface State {
        int INVALID = -1;
        int PLAYING = 0;
        int PAUSED = 1;
        int FAST_REWIND = 2;
        int FAST_FORWARD = 3;
        int COMPLETED = 4;
        int PREPARED = 5;
    }
}
