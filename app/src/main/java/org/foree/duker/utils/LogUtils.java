package org.foree.duker.utils;

import android.util.Log;

import org.foree.duker.base.MyApplication;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by foree on 16-8-21.
 * Log工具类
 */
public class LogUtils {
    private static final String TAG = LogUtils.class.getSimpleName();

    public static void log(String logMsg){
        File logFile = new File(MyApplication.myApplicationDirPath + File.separator + MyApplication.myApplicationCacheName + File.separator + "logfile");
        String currentTime = new Date(System.currentTimeMillis()).toString();
        FileUtils.appendFile(logFile, currentTime + ":" + logMsg);
        Log.d(TAG, "write log to " + logFile.toString());
    }
}
