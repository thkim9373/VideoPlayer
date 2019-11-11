package com.taehoon.videoplayerview;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VideoView extends ConstraintLayout
        implements TextureView.SurfaceTextureListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private final Context mContext;
    private MediaPlayerHolder mMediaPlayerHolder;
    private PlayTimeTextHandler mPlayTImeTextHandler = new PlayTimeTextHandler(this);
    private Handler mControllerHandler = new Handler();
    private Runnable mControllerHideTask = new Runnable() {
        @Override
        public void run() {
            hideMediaController();
        }
    };

    // View components
    private ConstraintLayout mClContainer, mClController;
    private TextureView mTextureViewVideo;
    private TextView mTvTotalPlayTime, mTvCurrentPlayTime, mTvPlaySpeed;
    private ImageView mIvTogglePlayPause, mIvFastRewind, mIvFastForward, mIvReplay;
    private SeekBar mSbProgress;

    private boolean isPlayingBeforeTracking;
    private boolean isControllerShow = false;

    public VideoView(Context context) {
        super(context);
        this.mContext = context;
        initView();
        setListener();
        initMediaPlayerHolder();
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
        setListener();
        initMediaPlayerHolder();
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
        setListener();
        initMediaPlayerHolder();
    }

    private void initView() {
        View mViewContainer = LayoutInflater.from(mContext).inflate(R.layout.view_video_controller_hide, VideoView.this, true);

        mClContainer = mViewContainer.findViewById(R.id.cl_container);
        mClController = mViewContainer.findViewById(R.id.cl_controller);
        mTextureViewVideo = mViewContainer.findViewById(R.id.texture_view_video);
        mTvTotalPlayTime = mViewContainer.findViewById(R.id.tv_total_play_time);
        mTvCurrentPlayTime = mViewContainer.findViewById(R.id.tv_current_play_time);
        mTvPlaySpeed = mViewContainer.findViewById(R.id.tv_play_speed);
        mIvTogglePlayPause = mViewContainer.findViewById(R.id.iv_toggle_play_pause);
        mIvFastRewind = mViewContainer.findViewById(R.id.iv_rewind);
        mIvFastForward = mViewContainer.findViewById(R.id.iv_forward);
        mIvReplay = mViewContainer.findViewById(R.id.iv_replay);
        mSbProgress = mViewContainer.findViewById(R.id.sb_progress);
    }

    private void setListener() {
        mTextureViewVideo.setSurfaceTextureListener(this);
        mSbProgress.setOnSeekBarChangeListener(this);
        mTextureViewVideo.setOnClickListener(this);
        mTvCurrentPlayTime.setOnClickListener(this);
        mIvTogglePlayPause.setOnClickListener(this);
        mIvFastRewind.setOnClickListener(this);
        mIvFastForward.setOnClickListener(this);
        mIvReplay.setOnClickListener(this);
    }

    private void initMediaPlayerHolder() {
        mMediaPlayerHolder = new MediaPlayerHolder(mContext);
        mMediaPlayerHolder.setPlaybackInfoListener(new PlaybackListener());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.texture_view_video) {
            toggleController();
        } else if (id == R.id.iv_toggle_play_pause) {
            togglePlayPause();
            extensionMediaControllerTime();
        } else if (id == R.id.iv_rewind) {
            fastRewind();
            extensionMediaControllerTime();
        } else if (id == R.id.iv_forward) {
            fastForward();
            extensionMediaControllerTime();
        } else if (id == R.id.tv_play_speed) {

        } else if (id == R.id.iv_replay) {
            replay();
        }
    }

    private void toggleController() {
        if(isControllerShow) {
            hideMediaController();
        } else {
            showMediaController();
            hideMediaControllerDelayed();
        }
    }

    private void hideMediaControllerDelayed() {
        mControllerHandler.removeCallbacks(mControllerHideTask);
        mControllerHandler.postDelayed(mControllerHideTask, 3000);
    }

    private void extensionMediaControllerTime() {
        mControllerHandler.removeCallbacks(mControllerHideTask);
        mControllerHandler.postDelayed(mControllerHideTask, 3000);
    }

    private void showMediaController() {
        isControllerShow = true;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mContext, R.layout.view_video_controller_show);
        constraintSet.setVisibility(mIvReplay.getId(), mIvReplay.getVisibility());
        TransitionManager.beginDelayedTransition(mClContainer);
        constraintSet.applyTo(mClContainer);
    }

    private void hideMediaController() {
        isControllerShow = false;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mContext, R.layout.view_video_controller_hide);
        constraintSet.setVisibility(mIvReplay.getId(), mIvReplay.getVisibility());
        TransitionManager.beginDelayedTransition(mClContainer);
        constraintSet.applyTo(mClContainer);
    }

    private void togglePlayPause() {
        if (mMediaPlayerHolder != null) {
            if (mMediaPlayerHolder.isPlaying()) {
                mMediaPlayerHolder.pause();
            } else {
                mMediaPlayerHolder.play();
            }
        }
    }

    private void fastRewind() {
        if (mMediaPlayerHolder != null) {
            mMediaPlayerHolder.fastRewind();
        }
    }

    private void fastForward() {
        if (mMediaPlayerHolder != null) {
            mMediaPlayerHolder.fastForward();
        }
    }

    private void replay() {
        if (mMediaPlayerHolder != null) {
            mMediaPlayerHolder.play();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mMediaPlayerHolder.seekTo(progress);
            setCurrentPlayTimeText(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isPlayingBeforeTracking = mMediaPlayerHolder.isPlaying();
        if (mMediaPlayerHolder.isPlaying()) {
            mMediaPlayerHolder.pause();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (isPlayingBeforeTracking) {
            mMediaPlayerHolder.play();
        }
    }

    private void setCurrentPlayTimeText(int progress) {
        DateFormat format = new SimpleDateFormat("mm:ss", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String seekTime = format.format(new Date(progress));
        mTvCurrentPlayTime.setText(seekTime);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Surface surface = new Surface(surfaceTexture);
        mMediaPlayerHolder.initMediaPlayer(surface);
        mMediaPlayerHolder.loadMedia(R.raw.wolf);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mMediaPlayerHolder != null) {
            mMediaPlayerHolder.release();
            mMediaPlayerHolder = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private class PlaybackListener extends PlaybackInfoListener {
        @Override
        public void onDurationChanged(int duration) {
            mSbProgress.setMax(duration);
            DateFormat format = new SimpleDateFormat("mm:ss", Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            String totalPlayTime = "/" + format.format(new Date(duration));
            mTvTotalPlayTime.setText(totalPlayTime);
        }

        @Override
        public void onPositionChanged(int position) {
            mSbProgress.setProgress(position);
            Message message = new Message();
            message.arg1 = position;
            mPlayTImeTextHandler.sendMessage(message);
        }

        @Override
        public void onStateChanged(int state) {
            switch (state) {
                case State.COMPLETED:
                    mIvReplay.setVisibility(VISIBLE);
                    mIvTogglePlayPause.setImageResource(R.drawable.play_arrow);
                    break;
                case State.INVALID:
                    break;
                case State.PAUSED:
                    mIvTogglePlayPause.setImageResource(R.drawable.play_arrow);
                    break;
                case State.PLAYING:
                    if (mIvReplay.getVisibility() == VISIBLE) {
                        mIvReplay.setVisibility(GONE);
                    }
                    mIvTogglePlayPause.setImageResource(R.drawable.pause);
                    break;
                case State.FAST_REWEIND:
                case State.FAST_FORWARD:
                    mSbProgress.setProgress(mMediaPlayerHolder.getCurrentPosition());
                    setCurrentPlayTimeText(mSbProgress.getProgress());
                    break;
            }
        }

        @Override
        public void onPlaybackCompleted() {
            super.onPlaybackCompleted();
        }
    }

    private class PlayTimeTextHandler extends Handler {

        private final WeakReference<VideoView> reference;

        public PlayTimeTextHandler(VideoView videoView) {
            this.reference = new WeakReference<>(videoView);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoView videoView = reference.get();
            if (videoView != null) {
                int progress = msg.arg1;
                DateFormat format = new SimpleDateFormat("mm:ss", Locale.getDefault());
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                String currentPlayTime = format.format(new Date(progress));
                mTvCurrentPlayTime.setText(currentPlayTime);
            }
        }
    }
}
