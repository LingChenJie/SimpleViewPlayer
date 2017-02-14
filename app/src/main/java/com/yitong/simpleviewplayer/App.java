package com.yitong.simpleviewplayer;

import android.app.Application;

import com.yitong.simpleviewplayer.utils.CacheFileUtils;
import com.yitong.simpleviewplayer.utils.DensityUtils;

/**
 * date：2017/2/13
 * des：
 * Create by suqi
 * Copyright (c) 2016 Shanghai P&C Information Technology Co., Ltd.
 */

public class App extends Application {

    private App mApp;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;

        DensityUtils.init(mApp);
        CacheFileUtils.initFiles();
        CacheFileUtils.copyAssetsFilesToSD(mApp, "video", CacheFileUtils.empVideoPath);
    }
}
