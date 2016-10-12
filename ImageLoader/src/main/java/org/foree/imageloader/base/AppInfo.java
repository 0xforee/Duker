package org.foree.imageloader.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by foree on 3/7/15.
 * 包含自己app的一些信息：
 * 1）当前网络状况
 * 2）应用程序目录和缓存目录
 * 3)Sdcard路径
 * <p/>
 * 以及启动时需要做的初始化
 */
public class AppInfo extends Application {
    private static final String TAG = "AppInfo";
    private Context mContext;
    /**
     * 应用程序信息
     */
    //应用程序名称
    public static String myApplicationName;
    //应用程序包名
    public static String myApplicationPackageName;
    //应用程序版本名称
    public static String myVersionName;
    //应用程序版本序号(应用程序用来判断是否升级的,例如:17)
    public static int myVersionCode;
    //应用程序版本号(开发者自定义,例如:1.7.3
    public static String myApplicationVersion;
    //应用程序的缓存目录路径
    public static String myCacheDir;

    public AppInfo(Context context) {
        mContext = context;
        initApplicationVersionInfo(mContext);
        initEnv();
    }

    private void initEnv() {

        String cacheFile;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            // /sdcard/Android/data/包名/cache
            cacheFile = mContext.getExternalCacheDir().getPath();
        }else {
            // /data/data/包名/cache
            cacheFile = mContext.getCacheDir().getPath();
        }
            //缓存目录
            File cacheDir = new File(cacheFile);
            if (!cacheDir.exists()) {
                if (!cacheDir.mkdir()) {
                    Log.e(TAG, "create cache dir error");
                }
            }

        myCacheDir = cacheDir.toString();

    }

    //获取应用程序的版本信息
    private void initApplicationVersionInfo(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            //获取当前包的信息
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            //获取应用程序版本号
            myApplicationVersion = packageInfo.versionName;
            //获取版本序号
            myVersionCode = packageInfo.versionCode;
            //获取版本名称
            myVersionName = myApplicationName + " v" + packageInfo.versionName;
            // 应用程序包名
            myApplicationPackageName = mContext.getPackageName();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


}
