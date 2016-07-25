package org.foree.duker.base;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.foree.duker.R;

import java.io.File;

/**
 * Created by foree on 16-7-25.
 * 自身应用程序信息
 */
public class MyApplication extends BaseApplication{
    private static final String TAG = MyApplication.class.getSimpleName();
    private Context mContext;

    /**
     * SD卡信息
     */
    public static final String SDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    /**
     * 应用程序信息
     */
    // 应用程序名称
    public static final String myApplicationName = getInstance().getResources().getString(R.string.app_name);
    // 应用程序包名
    public static final String myApplicationPackageName = getInstance().getPackageName();
    // 应用程序sdcard绝对路径
    public static final String myApplicationDirPath = SDCardPath + File.separator + myApplicationName;
    // 应用程序数据目录名
    public static final String myApplicationDataName = "data";
    // 应用程序缓存目录名
    public static final String myApplicationCacheName = "cache";

    public void initApplicationDir() {

        mContext = BaseApplication.getInstance().getApplicationContext();

        //如果当前Sdcard已经挂载，应用程序目录与缓存目录是否建立完成
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //应用程序目录
            File myDateDir = new File(myApplicationDirPath);
            if (!myDateDir.exists())
                if (!myDateDir.mkdir()) {
                    Log.e(TAG, "创建应用程序目录失败");
                }


            //缓存目录
            File myCacheDir = new File(myApplicationDirPath + File.separator + myApplicationDataName);
            if (!myCacheDir.exists())
                if (!myCacheDir.mkdir()) {
                    Log.e(TAG, "创建cache目录失败");
                }
            //source目录
            File mySourceDir = new File(myApplicationDirPath + File.separator + myApplicationCacheName);
            if (!mySourceDir.exists())
                if (!mySourceDir.mkdir()) {
                    Log.e(TAG, "创建data目录失败");
                }
        }
    Log.v(TAG, "环境变量初始化成功");

    }
}
