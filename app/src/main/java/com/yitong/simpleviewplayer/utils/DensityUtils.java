package com.yitong.simpleviewplayer.utils;

import android.content.Context;

/**
 * date：2017/2/13
 * des：
 * Create by suqi
 */

public class DensityUtils {

    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
    }

    public static int dp2px(float dpVal) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpVal * density + 0.5f);
    }

    public static int px3dp(Context context, float pxVal) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return (int) (pxVal / density + 0.5f);
    }
}
