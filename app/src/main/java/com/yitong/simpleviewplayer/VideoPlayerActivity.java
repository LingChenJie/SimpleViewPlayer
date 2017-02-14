package com.yitong.simpleviewplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.yitong.simpleviewplayer.utils.CacheFileUtils;

import java.io.File;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = (VideoView) findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setMediaPlayer(videoView);
        videoView.setMediaController(mediaController);

        String path = CacheFileUtils.empVideoPath + File.separator + "demo.mp4";
        videoView.setVideoPath(path);
        videoView.start();
    }
}
