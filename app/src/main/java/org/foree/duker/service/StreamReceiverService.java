package org.foree.duker.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.foree.duker.api.AbsApiFactory;
import org.foree.duker.api.AbsApiHelper;
import org.foree.duker.api.ApiFactory;
import org.foree.duker.api.FeedlyApiArgs;
import org.foree.duker.api.FeedlyApiHelper;
import org.foree.duker.api.LocalApiHelper;
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssItem;

import java.util.List;

public class StreamReceiverService extends Service {
    private static final String TAG = StreamReceiverService.class.getSimpleName();
    AbsApiHelper apiHelper, localApiHelper;
    private StreamCallBack mCallBack;
    private MyBinder mBinder = new MyBinder();

    public StreamReceiverService() {
    }

    public class MyBinder extends Binder {
        public StreamReceiverService getService(){
            return StreamReceiverService.this;
        }
    }
    public void registerCallBack(StreamCallBack callback){
        mCallBack = callback;
    }

    public void unregisterCallBack(){
        mCallBack = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        AbsApiFactory absApiFactory = new ApiFactory();
        apiHelper = absApiFactory.createApiHelper(FeedlyApiHelper.class);
        localApiHelper = absApiFactory.createApiHelper(LocalApiHelper.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        // stage 1, sync old data
        syncOldData();

        FeedlyApiArgs args = new FeedlyApiArgs();
        args.setCount(500);

        // stage 2, get new data
        localApiHelper.getStream("", FeedlyApiHelper.API_GLOBAL_ALL_URL.replace(":userId", FeedlyApiHelper.USER_ID), args, new NetCallback<List<RssItem>>() {
            @Override
            public void onSuccess(final List<RssItem> data) {
                // success insert
                // 如果mCallBack为空，证明还未启动MainActivity，无需update
                if (mCallBack != null)
                    mCallBack.notifyUpdate();
            }

            @Override
            public void onFail(String msg) {
            }
        });

        // stage 3, time trigger
        timeTrigger();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // sync data from server
    private void syncOldData(){
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if(!sp.getBoolean("sync_done", false)){
            // start sync old data
            Thread syncThread = new Thread(){
              @Override
                public void run(){
                  FeedlyApiArgs args = new FeedlyApiArgs();
                  for(int count = 100; count < 2000; count++){
                      args.setCount(count);
                      localApiHelper.getStream("", FeedlyApiHelper.API_GLOBAL_ALL_URL.replace(":userId", FeedlyApiHelper.USER_ID), args, null);
                  }
                  // sync done
                  sp.edit().putBoolean("sync_done", true).apply();
              }
            };
            syncThread.start();

        }
    }


    // time
    private void timeTrigger(){}

    public interface StreamCallBack {
        // 数据同步结束，更新UI
        void notifyUpdate();
    }
}
