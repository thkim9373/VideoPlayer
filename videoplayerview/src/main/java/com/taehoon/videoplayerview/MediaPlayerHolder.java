package com.taehoon.videoplayerview;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MediaPlayerHolder implements PlayerAdapter {

    public static final int PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 500;

    private final Context mContext;
    private MediaPlayer mMediaPlayer;
    private int resId;
    private PlaybackInfoListener mPlaybackInfoListener;
    private ScheduledExecutorService mExecutor;
    private Runnable mSeekbarPositionUpdateTask;

    public MediaPlayerHolder(Context context) {
        this.mContext = context;
    }

    public void initMediaPlayer(Surface surface) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setSurface(surface);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopUpdatingCallbackWithPosition();
                    if (mPlaybackInfoListener != null) {
                        mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.COMPLETED);
                        mPlaybackInfoListener.onPlaybackCompleted();
                    }
                }
            });
        }
    }

    public void setPlaybackInfoListener(PlaybackInfoListener listener) {
        this.mPlaybackInfoListener = listener;
    }

    @Override
    public void loadMedia(int resId) {
        String videoPath = "android.resource://" + mContext.getPackageName() + "/" + resId;
        Uri videoUri = Uri.parse(videoPath);
        try {
            mMediaPlayer.setDataSource(mContext, videoUri);
        } catch (Exception e) {
            Log.d("TAG", e.toString());
        }

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                initProgressCallback();
                startUpdatingCallbackWithPosition();
                play();
            }
        });

        try {
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.d("TAG", e.toString());
        }
    }

    @Override
    public void release() {
        if(mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public boolean isPlaying() {
        if(mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public void play() {
        if(mMediaPlayer != null) {
            mMediaPlayer.start();
            startUpdatingCallbackWithPosition();
            if(mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PLAYING);
            }
        }
    }

    @Override
    public void pause() {
        if(mMediaPlayer != null) {
            mMediaPlayer.pause();
            stopUpdatingCallbackWithPosition();
            if(mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PAUSED);
            }
        }
    }

    @Override
    public void fastRewind() {
        if(mMediaPlayer != null) {
            mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() - 3000);
            if(mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.FAST_REWEIND);
            }
        }
    }

    @Override
    public void fastForward() {
        if(mMediaPlayer != null) {
            mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() + 3000);
            if(mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.FAST_FORWARD);
            }
        }
    }

    int getCurrentPosition() {
        if(mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return -1;
    }

    @Override
    public void seekTo(int position) {
        if(mMediaPlayer != null) {
            mMediaPlayer.seekTo(position);
        }
    }

    private void startUpdatingCallbackWithPosition() {
        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        if (mSeekbarPositionUpdateTask == null) {
            mSeekbarPositionUpdateTask = new Runnable() {
                @Override
                public void run() {
                    updateProgressCallbackTask();
                }
            };
        }
        mExecutor.scheduleAtFixedRate(
                mSeekbarPositionUpdateTask,
                0,
                PLAYBACK_POSITION_REFRESH_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }

    private void stopUpdatingCallbackWithPosition() {
        if (mExecutor != null) {
            mExecutor.shutdown();
            mExecutor = null;
            mSeekbarPositionUpdateTask = null;
        }
    }

    private void updateProgressCallbackTask() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            int currentPosition = mMediaPlayer.getCurrentPosition();
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onPositionChanged(currentPosition);
            }
        }
    }

    @Override
    public void initProgressCallback() {
        final int duration = mMediaPlayer.getDuration();
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener.onDurationChanged(duration);
            mPlaybackInfoListener.onPositionChanged(0);
        }
    }
}