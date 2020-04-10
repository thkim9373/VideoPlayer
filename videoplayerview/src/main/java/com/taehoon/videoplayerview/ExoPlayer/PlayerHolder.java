package com.taehoon.videoplayerview.ExoPlayer;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.util.Util;
import com.taehoon.videoplayerview.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.android.exoplayer2.Player.STATE_BUFFERING;
import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static com.google.android.exoplayer2.Player.STATE_IDLE;
import static com.google.android.exoplayer2.Player.STATE_READY;

public class PlayerHolder implements PlayerAdapter {

    private static final int PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 500;

    private final Context mContext;
    private ExoPlayer mPlayer;
    private ControlDispatcher mControlDispatcher = new com.google.android.exoplayer2.DefaultControlDispatcher();
    private PlaybackInfoListener mPlaybackInfoListener;
    private ScheduledExecutorService mExecutor;
    private Runnable mSeekbarPositionUpdateTask;

    PlayerHolder(Context context) {
        this.mContext = context;
    }

    void initMediaPlayer() {
        if (mPlayer == null) {
            mPlayer = ExoPlayerFactory.newSimpleInstance(mContext);
            mPlayer.addListener(new Player.EventListener() {
                @Override
                public void onLoadingChanged(boolean isLoading) {
                    if (isLoading) {
                        initProgressCallback();
                    }
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    switch (playbackState) {
                        case STATE_IDLE:
                            if (mPlaybackInfoListener != null)
                                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.IDLE);
                            break;
                        case STATE_BUFFERING:
                            if (mPlaybackInfoListener != null) {
                                if (playWhenReady) {
                                    mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PREPARED);
                                } else {
                                    mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.BUFFERING);
                                }
                            }
                            break;
                        case STATE_READY:
                            if (playWhenReady) {
                                startUpdatingCallbackWithPosition();
                                if (mPlaybackInfoListener != null)
                                    mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PLAYING);
                            } else {
                                stopUpdatingCallbackWithPosition();
                                if (mPlaybackInfoListener != null)
                                    mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PAUSED);
                            }
                            break;
                        case STATE_ENDED:
                            stopUpdatingCallbackWithPosition();
                            if (mPlaybackInfoListener != null)
                                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.ENDED);
                            break;
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {

                }
            });
        }
    }

    void setPlayer(PlayerView playerView) {
        if (mPlayer != null) {
            playerView.setPlayer(mPlayer);
        }
    }

    void setPlaybackInfoListener(PlaybackInfoListener listener) {
        this.mPlaybackInfoListener = listener;
    }

    float getPlaySpeed() {
        if (mPlayer != null) {
            return mPlayer.getPlaybackParameters().speed;
        }
        return -1;
    }

    @Override
    public void loadMedia(int resId) {

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                mContext,
                Util.getUserAgent(mContext, mContext.getString(R.string.app_name))
        );

        MediaSource videoSource =
                new ProgressiveMediaSource
                        .Factory(dataSourceFactory)
                        .createMediaSource(RawResourceDataSource.buildRawResourceUri(resId));

        mPlayer.prepare(videoSource);
        mPlayer.setPlayWhenReady(true);
    }

    @Override
    public void loadMediaFromUri(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                mContext,
                Util.getUserAgent(mContext, mContext.getString(R.string.app_name))
        );

        MediaSource videoSource =
                new ProgressiveMediaSource
                        .Factory(dataSourceFactory)
                        .createMediaSource(uri);

        mPlayer.prepare(videoSource);
        mPlayer.setPlayWhenReady(true);
    }

    void showThumbnail() {
        if (mPlayer != null) {
            mPlayer.seekTo(1);
        }
    }

    @Override
    public void release() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.getPlayWhenReady();
        }
        return false;
    }

    @Override
    public void play() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(!mPlayer.getPlayWhenReady());
        }
    }

    @Override
    public void pause() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(!mPlayer.getPlayWhenReady());
        }
    }

    @Override
    public void fastRewind() {
        if (mPlayer != null && mPlayer.isCurrentWindowSeekable()) {
            seekTo(mPlayer, mPlayer.getCurrentPosition() - 3000);
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.FAST_REWIND);
            }
        }
    }

    @Override
    public void fastForward() {
        if (mPlayer != null && mPlayer.isCurrentWindowSeekable()) {
            seekTo(mPlayer, mPlayer.getCurrentPosition() + 3000);
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.FAST_FORWARD);
            }
        }
    }

    private void seekTo(Player player, long positionMs) {
        seekTo(player, player.getCurrentWindowIndex(), positionMs);
    }

    private boolean seekTo(Player player, int windowIndex, long positionMs) {
        long durationMs = player.getDuration();
        if (durationMs != C.TIME_UNSET) {
            positionMs = Math.min(positionMs, durationMs);
        }
        positionMs = Math.max(positionMs, 0);
        return mControlDispatcher.dispatchSeekTo(player, windowIndex, positionMs);
    }

    long getCurrentPosition() {
        if (mPlayer != null) {
            return mPlayer.getCurrentPosition();
        }
        return -1;
    }

    @Override
    public void seekTo(int position) {
        if (mPlayer != null) {
            mPlayer.seekTo(position);
        }
    }

    @Override
    public void replay() {
        if (mPlayer != null) {
            mPlayer.seekTo(0);
            mPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void playSpeedChange(float speed) {
        if (mPlayer != null) {
            PlaybackParameters playbackParameters = new PlaybackParameters(speed);
            mPlayer.setPlaybackParameters(playbackParameters);
        }
    }

    private void startUpdatingCallbackWithPosition() {
        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        if (mSeekbarPositionUpdateTask == null) {
            mSeekbarPositionUpdateTask = this::updateProgressCallbackTask;
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
        if (mPlayer != null && mPlayer.getPlayWhenReady()) {
            long currentPosition = mPlayer.getCurrentPosition();
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onPositionChanged(currentPosition);
            }
        }
    }

    @Override
    public void initProgressCallback() {
        final long duration = mPlayer.getDuration();
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener.onDurationChanged(duration);
            mPlaybackInfoListener.onPositionChanged(0);
        }
    }
}
