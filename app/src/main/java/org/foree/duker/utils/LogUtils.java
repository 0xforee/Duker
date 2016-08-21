package org.foree.duker.utils;

import android.util.Log;

import org.foree.duker.base.MyApplication;

import java.io.File;
import java.io.IOException;

/**
 * Created by foree on 16-8-21.
 * Log工具类
 */
public class LogUtils {
    private static final String TAG = LogUtils.class.getSimpleName();

    public static void log(String logMsg){
        File logFile = new File(MyApplication.myApplicationDirPath + File.separator + MyApplication.myApplicationCacheName + File.separator + "logfile");
        try {
            FileUtils.appendFile(logFile, logMsg);
            Log.d(TAG, "write log to " + logFile.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
