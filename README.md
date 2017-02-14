# VideoView的最佳练习
结合网上学习的一些资料和视频，把VideoView的基本使用整合了起来的，方便以后使用和查看。

### 主界面

<img src="/screen/test_01.png" alt="screenshot" width="270" height="486" />

### 当手指离开屏幕一段时间后，控制暂停和播放进度的控制台自动隐藏
<img src="/screen/test_02.png" alt="screenshot" width="270" height="486" />

### 全屏播放
<img src="/screen/test_03.png" alt="screenshot" width="486" height="270" />

### 介绍一下技术点：
#### 全屏播放 - 横竖屏切换
	androidmanifest.xml 中依然还是定义竖屏,并定义一个切换横纵屏按钮 btnChange :

		<activity
		  android:name=".MainActivity2"
		  android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
		  android:screenOrientation="portrait"
		  android:theme="@style/Theme.AppCompat.Light.NoActionBar"/>

	按钮监听,手动切换

		   if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
		      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		  } else {
		      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		  }


	设置VideoView布局尺寸

		@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {// 横屏
            setSystemUiHide();// 隐藏最上面那一栏
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);// 设置为全屏

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {// 竖屏
            setSystemUiShow();// 显示最上面那一栏
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtils.dp2px(240));
        }
    }
#### 滑动改变屏幕亮度/音量
    代码中已经注释什么清晰


### License
* 同时希望可以帮助到其他人，欢迎大家star和fork，你的鼓励将是我努力的源泉

====
      Copyright 2017 Werb

      Licensed under the Apache License, Version 2.0 (the "License");
      you may not use this file except in compliance with the License.
      You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.

### Contact Me
* Email: 976918915@qq.com
