package com.yitong.simpleviewplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yitong.simpleviewplayer.utils.CacheFileUtils;
import com.yitong.simpleviewplayer.utils.DensityUtils;
import com.yitong.simpleviewplayer.widget.CustomVideoView;

import java.io.File;
import java.util.Locale;

public class VideoPlayerActivity2 extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, MediaPlayer.OnPreparedListener, View.OnTouchListener {

    private static final String TAG = "VideoPlayerActivity2";

    private CustomVideoView videoView;
    private SeekBar sbPlay, sbVolume;
    private ImageView ivPause, ivScreen, ivVolume;
    private TextView tvCurTime, tvTotalTime;
    private RelativeLayout rlVideo;
    private LinearLayout llControl;// 暂定、调节进度控制台

    private FrameLayout flProgress;// 调节亮度和声音的控制台
    private ImageView ivOperationBg, ivOperationPercentImg;

    private int mScreenWidth, mScreenHeight;// 屏幕的宽高
    private VolumeReceiver mVolumeReceiver;// 声音的接收者
    private AudioManager mAudioManager;
    private int mCurPlayPosition;// 当前播放进度

    private boolean isFirst = true;// 是否第一次设置过总时间和总音量
    private boolean isAdjust = false;// 是否符合滑动范围
    private float lastX = 0, lastY = 0, threshold = 54;// 上一次滑动的结束位置和滑动最小范围

    private static final int UPDATE_TIME = 1;// 更新时间进度
    private static final int HIDE_CONTROL = 2;// 隐藏暂定、调节进度控制台

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TIME:
                    if (isFirst) {
                        isFirst = false;
                        sbPlay.setMax(videoView.getDuration());
                        updateTimeFormat(tvTotalTime, videoView.getDuration());
                    }

                    sbPlay.setProgress(videoView.getCurrentPosition());
                    updateTimeFormat(tvCurTime, videoView.getCurrentPosition());

                    handler.sendEmptyMessageDelayed(UPDATE_TIME, 500);
                    break;

                case HIDE_CONTROL:
                    llControl.setVisibility(View.GONE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player2);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        videoView = (CustomVideoView) findViewById(R.id.videoView);
        sbPlay = (SeekBar) findViewById(R.id.play_seekbar);
        sbVolume = (SeekBar) findViewById(R.id.volume_seekbar);
        ivPause = (ImageView) findViewById(R.id.pause_iv);
        ivScreen = (ImageView) findViewById(R.id.screen_iv);
        ivVolume = (ImageView) findViewById(R.id.volume_iv);
        tvCurTime = (TextView) findViewById(R.id.cur_time_tv);
        tvTotalTime = (TextView) findViewById(R.id.total_time_tv);
        rlVideo = (RelativeLayout) findViewById(R.id.video_layout);
        llControl = (LinearLayout) findViewById(R.id.control_ll);

        flProgress = (FrameLayout) findViewById(R.id.progress_layout);
        ivOperationBg = (ImageView) findViewById(R.id.operation_bg);
        ivOperationPercentImg = (ImageView) findViewById(R.id.operation_percent);
    }

    private void initData() {
        mVolumeReceiver = new VolumeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(mVolumeReceiver, intentFilter);// 注册声音广播接受者

        String path = CacheFileUtils.empVideoPath + File.separator + "demo.mp4";
        videoView.setVideoURI(Uri.parse(path));// 设置视频路径

        // 设置音量
        sbVolume.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        sbVolume.setProgress(volume);
        if (volume == 0) {
            ivVolume.setImageResource(R.drawable.mute);
        }
    }

    private void initListener() {
        videoView.setOnPreparedListener(this);// 准备完成监听
        ivPause.setOnClickListener(this);
        ivScreen.setOnClickListener(this);
        sbPlay.setOnSeekBarChangeListener(this);
        sbVolume.setOnSeekBarChangeListener(this);
        videoView.setOnTouchListener(this);// touch事件
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView.canSeekForward() && mCurPlayPosition != 0) {
            videoView.seekTo(mCurPlayPosition);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.canPause()) {
            ivPause.setImageResource(R.drawable.pause_btn_style);
            mCurPlayPosition = videoView.getCurrentPosition();
            videoView.pause();
        }
        if (handler.hasMessages(UPDATE_TIME)) {
            handler.removeMessages(UPDATE_TIME);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (videoView.canPause()) {
            ivPause.setImageResource(R.drawable.play_btn_style);
            videoView.pause();
        }
        if (handler.hasMessages(UPDATE_TIME)) {
            handler.removeMessages(UPDATE_TIME);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView.canPause()) {
            videoView.stopPlayback();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(0);// 移除所有消息
            handler = null;
        }
        if (mVolumeReceiver != null) {
            unregisterReceiver(mVolumeReceiver);// 解注册
        }
    }

    // 当屏幕发生切换时调用
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {// 横屏
            setSystemUiHide();// 隐藏最上面那一栏
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);// 设置为全屏

            ivScreen.setImageResource(R.drawable.exit_full_screen);
            ivVolume.setVisibility(View.VISIBLE);
            sbVolume.setVisibility(View.VISIBLE);

            // 强制移除半屏状态
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {// 竖屏
            setSystemUiShow();// 显示最上面那一栏
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtils.dp2px(240));

            ivScreen.setImageResource(R.drawable.full_screen);
            ivVolume.setVisibility(View.GONE);
            sbVolume.setVisibility(View.GONE);

            // 强制移除全屏状态
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    // 设置VideoView的大小
    private void setVideoViewScale(int width, int height) {
        ViewGroup.LayoutParams params = rlVideo.getLayoutParams();
        params.width = width;
        params.height = height;
        rlVideo.setLayoutParams(params);

        ViewGroup.LayoutParams params1 = videoView.getLayoutParams();
        params1.width = width;
        params1.height = height;
        videoView.setLayoutParams(params1);
    }

    // 隐藏SystemUi
    private void setSystemUiHide() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    // 显示SystemUi
    private void setSystemUiShow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    // 格式化时间，并显示textView上
    private void updateTimeFormat(TextView tv, int millisecond) {
        int s = millisecond / 1000;
        int hour = s / 3600;
        int minute = s % 3600 / 60;
        int second = s % 60;
        tv.setText(String.format(Locale.CHINA, "%02d:%02d:%02d", hour, minute, second));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pause_iv:// 暂停、播放按钮
                if (videoView.isPlaying()) {
                    ivPause.setImageResource(R.drawable.play_btn_style);
                    videoView.pause();
                    handler.removeMessages(UPDATE_TIME);
                } else {
                    ivPause.setImageResource(R.drawable.pause_btn_style);
                    videoView.start();
                    handler.sendEmptyMessage(UPDATE_TIME);
                }
                break;

            case R.id.screen_iv:// 全屏、半屏按钮
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                break;
        }
    }

    /*====================seekBar的重载方法 start=====================================*/
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.play_seekbar:// 播放进度的seekBar
                if (fromUser) {
                    videoView.seekTo(progress);
                    updateTimeFormat(tvCurTime, progress);
                }
                break;

            case R.id.volume_seekbar:// 声音进度的seekBar
                Log.e(TAG, "volume_seekbar===> " + progress);

                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {// 当手刚开始触摸时，需要先移除时间更新的消息
        if (seekBar.getId() == R.id.play_seekbar) {
            if (handler.hasMessages(UPDATE_TIME)) {
                handler.removeMessages(UPDATE_TIME);
            }
            if (!videoView.isPlaying()) {
                videoView.start();
                ivPause.setImageResource(R.drawable.pause_btn_style);
            }
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {// 当手指停止操作时
        if (seekBar.getId() == R.id.play_seekbar) {
            handler.sendEmptyMessage(UPDATE_TIME);
        }
    }
    /*======================seekBar的重载方法 end===================================*/

    // 视频准备完毕
    @Override
    public void onPrepared(MediaPlayer mp) {
        videoView.start();
        ivPause.setImageResource(R.drawable.pause_btn_style);
        handler.sendEmptyMessage(UPDATE_TIME);
    }

    // 监听videoView的触摸事件
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                llControl.setVisibility(View.VISIBLE);

                lastX = x;
                lastY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                float changeX = x - lastX;
                float changeY = y - lastY;
                float absX = Math.abs(changeX);
                float absY = Math.abs(changeY);

                if (absX > threshold && absY > threshold) {
                    if (absY > absX) {// 说明是上下滑动
                        isAdjust = true;
                    } else {
                        isAdjust = false;
                    }
                } else if (absX < threshold && absY > threshold) {
                    isAdjust = true;
                } else if (absX > threshold && absY < threshold) {
                    isAdjust = false;
                }

                if (isAdjust) {
                    if (x < mScreenWidth / 2) {// 左屏幕，调节亮度
                        changeBrightness(-changeY);
                    } else {// 右屏幕，调节音量
                        changeVolume(-changeY);
                    }
                }

                lastX = x;
                lastY = y;
                break;

            case MotionEvent.ACTION_UP:
                lastX = 0;
                lastY = 0;
                flProgress.setVisibility(View.GONE);

                handler.sendEmptyMessageDelayed(HIDE_CONTROL, 5000);
                break;
        }
        return true;
    }

    // 调节亮度
    private void changeBrightness(float offset) {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        float brightness = attributes.screenBrightness;
        float index = offset / mScreenHeight;
        brightness = Math.max(brightness + index, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF);
        brightness = Math.min(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, brightness);
        attributes.screenBrightness = brightness;
        getWindow().setAttributes(attributes);

        if(flProgress.getVisibility() == View.GONE) {
            flProgress.setVisibility(View.VISIBLE);
        }
        ivOperationBg.setImageResource(R.drawable.video_brightness_bg);
        ViewGroup.LayoutParams params = ivOperationPercentImg.getLayoutParams();
        params.width = (int) (DensityUtils.dp2px(94) * brightness);
        ivOperationPercentImg.setLayoutParams(params);
    }

    // 改变音量
    private void changeVolume(float offset) {
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int cur = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int index = (int) (offset / mScreenHeight * max * 3);

        int volume = Math.max(cur + index, 0);
        volume = Math.min(volume, max);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        sbVolume.setProgress(volume);

        if(flProgress.getVisibility() == View.GONE) {
            flProgress.setVisibility(View.VISIBLE);
        }
        ivOperationBg.setImageResource(R.drawable.video_voice_bg);
        ViewGroup.LayoutParams params = ivOperationPercentImg.getLayoutParams();
        params.width = (int) (DensityUtils.dp2px(94) * (volume * 1.0f / max));
        ivOperationPercentImg.setLayoutParams(params);
    }

    // 音量的广播接收者，接收系统音量发生变化
    private class VolumeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (volume == 0) {
                    ivVolume.setImageResource(R.drawable.mute);
                } else {
                    ivVolume.setImageResource(R.drawable.volume);
                }
                sbVolume.setProgress(volume);
            }
        }
    }

    // 返回事件
    @Override
    public void onBackPressed() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            super.onBackPressed();
        }
    }
}
