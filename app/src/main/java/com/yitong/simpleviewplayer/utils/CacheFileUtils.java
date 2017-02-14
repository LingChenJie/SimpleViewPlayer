package com.yitong.simpleviewplayer.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * date：2016/11/27
 * des：缓存文件夹和文件的存储
 * Create by suqi
 */

public class CacheFileUtils {

    private static final String TAG = "CacheFileUtils";
    public static final String APP_CACHE_ROOT_PATH = Environment.getExternalStorageDirectory() + File.separator + "simpleVideoPlayer";

    // 测试视频根目录
    public static String empVideoPath = String.format("%s/%s", APP_CACHE_ROOT_PATH, "video");


    public static void initFiles() {
        if(isExistSDCard()){
            String[] paths = new String[]{empVideoPath};
            for (int i = 0,size = paths.length; i < size; i++) {
                File file = new File(paths[i]);
                if(!file.exists()){
                    boolean b = file.mkdirs();
                    Log.e("aaa", "aaa===> " + b);
                }
            }
        } else {
            Log.e(TAG, "创建缓存目录失败");
        }
    }

    /**
     * 检测sd卡是否存在
     *
     * @return
     */
    public static boolean isExistSDCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    /**
     * 读取assets文件转换为字符串
     * @param context  上下文
     * @param filePath  文件路径(包含文件名)
     * @return
     */
    public static String getAssetsFileString(Context context, String filePath){
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filePath)));
            String temp;
            while((temp = reader.readLine()) != null){
                buffer.append(temp);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                reader.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return buffer.toString();
    }

    /**
     * 把assets文件复制到sd卡上
     * @param context 上下文
     * @param sourcePath assets的文件路径
     * @param targetPath sd卡的文件路径
     * @return 是否复制成功
     */
    public static boolean copyAssetsFileToSD(Context context, String sourcePath, String targetPath){
        InputStream in = null;
        FileOutputStream fos = null;
        try {
            in = context.getAssets().open(sourcePath);
            fos = new FileOutputStream(targetPath);
            byte[] buffer = new byte[2048];
            int len = 0;
            while((len=in.read(buffer)) != -1){
                fos.write(buffer, 0, len);
            }
            in.close();
            fos.close();
            in = null;
            fos = null;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try{
                if(in != null){
                    in.close();
                }
                if(fos != null){
                    fos.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 复制assets文件夹下的文件或文件夹到指定的sd目录下
     * @param context 上下文
     * @param sourcePath assets的目录
     * @param targetPath sd的目录
     */
    public static boolean copyAssetsFilesToSD(Context context, String sourcePath, String targetPath) {
        boolean flag = false;
        try {
            String[] sourceArr = context.getAssets().list(sourcePath);
            if(sourceArr.length > 0){// 目录
                File file = new File(targetPath);
                if(!file.exists()){
                    file.mkdirs();
                }
                for(String str : sourceArr) {
                    copyAssetsFilesToSD(context, (sourcePath + File.separator + str), (targetPath + File.separator + str));
                }
            } else {// 文件
                flag = copyAssetsFileToSD(context, sourcePath, targetPath);
                if(!flag) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    /**
     * 复制文件
     * @param sourceFile  源文件
     * @param targetFile  目标文件
     */
    public static boolean copyFile(File sourceFile, File targetFile){
        FileInputStream in = null;
        FileOutputStream fos = null;
        try {
            in = new FileInputStream(sourceFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(in);

            fos = new FileOutputStream(targetFile);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fos);

            byte[] buffer = new byte[1024 * 5];
            int len = 0;
            while((len = bufferedInputStream.read(buffer))!= -1){
                bufferedOutputStream.write(buffer, 0, len);
            }
            bufferedOutputStream.flush();

            bufferedInputStream.close();
            bufferedOutputStream.close();
            in.close();
            fos.close();

            in = null;
            fos = null;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 把一个目录下的文件或文件复制到另一个目录下
     * @param sourcePath  源目录
     * @param targetPath  目标目录
     */
    public static boolean copyFiles(String sourcePath, String targetPath) {
        boolean flag = false;
        File[] files = new File(sourcePath).listFiles();
        for (File file : files){
            if(file.isFile()){// 文件
                flag = copyFile(file, new File(targetPath + File.separator + file.getName()));
                if(!flag){
                    return false;
                }
            } else {// 文件夹
                String sourceDir = sourcePath + File.separator + file.getName();
                String targetDir = targetPath + File.separator + file.getName();
                new File(targetDir).mkdirs();// 创建目标目录
                copyFiles(sourceDir, targetDir);
            }
        }
        return flag;
    }


}
