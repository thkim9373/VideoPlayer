package com.taehoon.videoplayerview;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class PlaybackInfoListener {

    public static String convertStateToString(@State int state) {
        String stateString;
        switch (state) {
            case State.INVALID:
                stateString = "INVALID";
                break;
            case State.PLAYING:
                stateString = "PLAYING";
                break;
            case State.PAUSED:
                stateString = "PAUSED";
                break;
            case State.COMPLETED:
                stateString = "COMPLETED";
                break;
            default:
                stateString = "N/A";
        }
        return stateString;
    }

    public void onDurationChanged(int duration) {
    }

    public void onPositionChanged(int position) {
    }

    public void onStateChanged(@State int state) {
    }

    public void onPlaybackCompleted() {
    }

    @IntDef({State.INVALID, State.PLAYING, State.PAUSED, State.FAST_REWEIND, State.FAST_FORWARD, State.COMPLETED})
    @Retention(RetentionPolicy.CLASS)
    @interface State {
        int INVALID = -1;
        int PLAYING = 0;
        int PAUSED = 1;
        int FAST_REWEIND = 2;
        int FAST_FORWARD = 3;
        int COMPLETED = 4;
    }
}
