package com.taehoon.videoviewsample;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.taehoon.videoplayerview.ExoPlayer.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("TAG", "Main Activity : onCreate");
        mVideoView = findViewById(R.id.vv_main);
        Uri videoUri = Uri.parse("https://www.youtube.com/watch?v=OEvEPF72NIY");
//        mVideoView.loadMediaFromUri(videoUri);
        mVideoView.loadMedia(R.raw.wolf);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("TAG", "Main Activity : onConfigurationChanged");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "Main Activity : onDestroy");
        mVideoView.releaseMedia();
    }
}