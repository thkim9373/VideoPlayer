package com.taehoon.videoplayerview.ExoPlayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.PlayerView;
import com.taehoon.videoplayerview.R;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.ColorInt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

@SuppressWarnings("ALL")
public class VideoView extends ConstraintLayout
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public static final int MODE_BOTTOM_SHEET_FRAGMENT_DIALOG = 0;
    public static final int MODE_BUTTON = 1;

    private final Context mContext;
    PlaySpeedBottomSheetDialog dialogFragment;
    private PlayerHolder mMediaPlayerHolder;
    private PlayTimeTextHandler mPlayTImeTextHandler = new PlayTimeTextHandler(this);
    private Handler mControllerHandler = new Handler();
    // View components
    private ConstraintLayout mClContainer, mClPlaySpeed;
    private PlayerView mTextureViewVideo;
    private TextView mTvTotalPlayTime, mTvCurrentPlayTime, mTvPlaySpeed, mTvSlow, mTvNormal, mTvFast, mTvReplay;
    private TextView[] mTvPlaySpeedArr = new TextView[3];
    private ImageView mIvTogglePlayPause, mIvFastRewind, mIvFastForward;
    private SeekBar mSbProgress;
    private View mViewCompleteBackground;

    private boolean isPlayingBeforeTracking;
    private boolean isControllerShow = false;
    private Runnable mControllerHideTask = new Runnable() {
        @Override
        public void run() {
            hideMediaController();
        }
    };

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
        Log.d("TAG", "Video View : initView()");
        View mViewContainer = LayoutInflater.from(mContext).inflate(R.layout.player_view, VideoView.this, true);

        mClContainer = mViewContainer.findViewById(R.id.cl_container);
        mTextureViewVideo = mViewContainer.findViewById(R.id.texture_view_video);
        mTvTotalPlayTime = mViewContainer.findViewById(R.id.tv_total_play_time);
        mTvCurrentPlayTime = mViewContainer.findViewById(R.id.tv_current_play_time);
        mTvPlaySpeed = mViewContainer.findViewById(R.id.tv_play_speed);
        mIvTogglePlayPause = mViewContainer.findViewById(R.id.iv_toggle_play_pause);
        mIvFastRewind = mViewContainer.findViewById(R.id.iv_rewind);
        mIvFastForward = mViewContainer.findViewById(R.id.iv_forward);
        mSbProgress = mViewContainer.findViewById(R.id.sb_progress);

        mTvReplay = mViewContainer.findViewById(R.id.tv_replay);
        mViewCompleteBackground = mViewContainer.findViewById(R.id.view_complete_background);

        mClPlaySpeed = mViewContainer.findViewById(R.id.cl_play_speed);
        mTvSlow = mViewContainer.findViewById(R.id.tv_play_speed_slow);
        mTvNormal = mViewContainer.findViewById(R.id.tv_play_speed_normal);
        mTvFast = mViewContainer.findViewById(R.id.tv_play_speed_fast);
        mTvPlaySpeedArr[0] = mTvSlow;
        mTvPlaySpeedArr[1] = mTvNormal;
        mTvPlaySpeedArr[2] = mTvFast;

        setPlaySpeedMode(MODE_BUTTON);
        mTextureViewVideo.setUseController(false);
    }

    private void setListener() {
        Log.d("TAG", "Video View : setListener()");
        mSbProgress.setOnSeekBarChangeListener(this);
        mTextureViewVideo.setOnClickListener(this);
        mTvPlaySpeed.setOnClickListener(this);
        mIvTogglePlayPause.setOnClickListener(this);
        mIvFastRewind.setOnClickListener(this);
        mIvFastForward.setOnClickListener(this);
        mTvReplay.setOnClickListener(this);
        mTvSlow.setOnClickListener(this);
        mTvNormal.setOnClickListener(this);
        mTvFast.setOnClickListener(this);
    }

    private void initMediaPlayerHolder() {
        Log.d("TAG", "Video View : initMediaPlayerHolder()");
        mMediaPlayerHolder = new PlayerHolder(mContext);
        mMediaPlayerHolder.setPlaybackInfoListener(new PlaybackListener());
        mMediaPlayerHolder.initMediaPlayer();
        mMediaPlayerHolder.setPlayer(mTextureViewVideo);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void setPlaySpeedMode(int mode) {
        if (mode == MODE_BUTTON) {
            mClPlaySpeed.setVisibility(VISIBLE);
            mTvPlaySpeed.setVisibility(GONE);
        } else {
            mClPlaySpeed.setVisibility(GONE);
            mTvPlaySpeed.setVisibility(VISIBLE);
        }
    }

    public void setProgressBarBacground(int resId) {
        Drawable drawable = mContext.getResources().getDrawable(resId, mContext.getTheme());
        mSbProgress.setProgressDrawable(drawable);
    }

    public void setProgressBarThumb(int resId) {
        Drawable drawable = mContext.getResources().getDrawable(resId, mContext.getTheme());
        mSbProgress.setThumb(drawable);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.texture_view_video) {
            toggleController();
        } else if (id == R.id.iv_toggle_play_pause) {
            togglePlayPause();
            hideMediaControllerDelayed();
        } else if (id == R.id.iv_rewind) {
            fastRewind();
            hideMediaControllerDelayed();
        } else if (id == R.id.iv_forward) {
            fastForward();
            hideMediaControllerDelayed();
        } else if (id == R.id.tv_replay) {
            replay();
        } else if (id == R.id.tv_play_speed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dialogFragment = new PlaySpeedBottomSheetDialog(mContext, mMediaPlayerHolder.getPlaySpeed(), this);
                dialogFragment.show();
            }
        } else if (id == R.id.cl_item_play_speed_container) {
            float playSpeed = (float) v.getTag();
            if (mMediaPlayerHolder != null) {
                mMediaPlayerHolder.playSpeedChange(playSpeed);
            }
            dialogFragment.dismiss();
        } else if (id == R.id.tv_play_speed_slow) {
            if (mMediaPlayerHolder != null) {
                mMediaPlayerHolder.playSpeedChange(0.5f);
                checkPlaySpeed(id);
                hideMediaControllerDelayed();
            }
        } else if (id == R.id.tv_play_speed_normal) {
            if (mMediaPlayerHolder != null) {
                mMediaPlayerHolder.playSpeedChange(1.0f);
                checkPlaySpeed(id);
                hideMediaControllerDelayed();
            }
        } else if (id == R.id.tv_play_speed_fast) {
            if (mMediaPlayerHolder != null) {
                mMediaPlayerHolder.playSpeedChange(1.5f);
                checkPlaySpeed(id);
                hideMediaControllerDelayed();
            }
        }
    }

    private void toggleController() {
        if (isControllerShow) {
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

    private void showMediaController() {
        isControllerShow = true;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mClContainer);
        constraintSet.clear(R.id.cl_controller, ConstraintSet.TOP);
        constraintSet.connect(R.id.cl_controller, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        TransitionManager.beginDelayedTransition(mClContainer);
        constraintSet.applyTo(mClContainer);
    }

    private void hideMediaController() {
        isControllerShow = false;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mClContainer);
        constraintSet.clear(R.id.cl_controller, ConstraintSet.BOTTOM);
        constraintSet.connect(R.id.cl_controller, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
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
            mMediaPlayerHolder.replay();
        }
    }

    private void checkPlaySpeed(int id) {
        for (TextView textView : mTvPlaySpeedArr) {
            if (textView.getId() == id) {
                TypedValue TypedValue = new TypedValue();
                mContext.getTheme().resolveAttribute(R.attr.colorAccent, TypedValue, true);
                @ColorInt int color = TypedValue.data;
                textView.setTextColor(color);
                textView.setBackgroundResource(R.drawable.background_play_speed_select);
            } else {
                textView.setTextColor(Color.WHITE);
                textView.setBackgroundResource(R.drawable.background_play_speed);
            }
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
        mControllerHandler.removeCallbacks(mControllerHideTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (isPlayingBeforeTracking) {
            mMediaPlayerHolder.play();
        }
        hideMediaControllerDelayed();
    }

    private void setCurrentPlayTimeText(int progress) {
        DateFormat format = new SimpleDateFormat("mm:ss", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String seekTime = format.format(new Date(progress));
        mTvCurrentPlayTime.setText(seekTime);
    }

    public void loadMedia(int resId) {
        mMediaPlayerHolder.loadMedia(resId);
    }

    public void loadMediaFromUri(Uri uri) {
        mMediaPlayerHolder.loadMediaFromUri(uri);
    }

    public void releaseMedia() {
        if (mMediaPlayerHolder != null) {
            mMediaPlayerHolder.release();
            mMediaPlayerHolder = null;
        }
    }

    private static final class PlayTimeTextHandler extends Handler {

        private final WeakReference<VideoView> reference;

        private PlayTimeTextHandler(VideoView videoView) {
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
                videoView.mTvCurrentPlayTime.setText(currentPlayTime);
            }
        }
    }

    private class PlaybackListener extends PlaybackInfoListener {
        @Override
        public void onDurationChanged(long duration) {
            mSbProgress.setMax((int) duration);
            DateFormat format = new SimpleDateFormat("mm:ss", Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            String totalPlayTime = "/" + format.format(new Date(duration));
            mTvTotalPlayTime.setText(totalPlayTime);
        }

        @Override
        public void onPositionChanged(long position) {
            mSbProgress.setProgress((int) position);
            Message message = new Message();
            message.arg1 = (int) position;
            mPlayTImeTextHandler.sendMessage(message);
        }

        @Override
        public void onStateChanged(int state) {
            switch (state) {
                case State.ENDED:
                    mTvReplay.setVisibility(VISIBLE);
                    mViewCompleteBackground.setVisibility(VISIBLE);
                    mIvTogglePlayPause.setImageResource(R.drawable.play_arrow);
                    break;
                case State.IDLE:
                    break;
                case State.PAUSED:
                    mIvTogglePlayPause.setImageResource(R.drawable.play_arrow);
                    break;
                case State.PLAYING:
                    if (mTvReplay.getVisibility() == VISIBLE) {
                        mTvReplay.setVisibility(GONE);
                    }
                    if (mViewCompleteBackground.getVisibility() == VISIBLE) {
                        mViewCompleteBackground.setVisibility(GONE);
                    }
                    mIvTogglePlayPause.setImageResource(R.drawable.pause);
                    break;
                case State.FAST_REWIND:
                case State.FAST_FORWARD:
                    mSbProgress.setProgress((int) mMediaPlayerHolder.getCurrentPosition());
                    setCurrentPlayTimeText(mSbProgress.getProgress());
                    break;
                case State.READY:
                    mMediaPlayerHolder.showThumbnail();
                    break;
            }
        }

        @Override
        public void onPlaybackCompleted() {
            super.onPlaybackCompleted();
        }
    }
}
