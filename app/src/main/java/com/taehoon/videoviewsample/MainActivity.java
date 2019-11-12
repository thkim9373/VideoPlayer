package com.taehoon.videoviewsample;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.taehoon.videoplayerview.VideoView;

public class MainActivity extends AppCompatActivity {

    VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("TAG", "Main Activity : onCreate");
        mVideoView = findViewById(R.id.vv_main);
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