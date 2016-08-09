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
import org.foree.duker.base.BaseApplication;
import org.foree.duker.dao.RssDao;
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssItem;

import java.util.List;

public class StreamReceiverService extends Service {
    private static final String TAG = StreamReceiverService.class.getSimpleName();
    AbsApiHelper apiHelper, localApiHelper;
    private StreamCallBack mCallBack;
    RssDao rssDao;
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
        rssDao = new RssDao(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        // stage 1, sync old data
        syncOldData();

        // stage 2, get new data
        syncNewData();

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

    // sync new data
    private void syncNewData(){
        FeedlyApiArgs args = new FeedlyApiArgs();

        //get continuation from SharedPreferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
        if (!sp.getString("continuation", "").isEmpty()) {
            Log.d(TAG, "get continuation");

            args.setContinuation(sp.getString("continuation", ""));
            args.setCount(500);

            apiHelper.getStream("", FeedlyApiHelper.API_GLOBAL_ALL_URL.replace(":userId", FeedlyApiHelper.USER_ID), args, new NetCallback<List<RssItem>>() {
                @Override
                public void onSuccess(final List<RssItem> data) {
                    // success insert to db
                    rssDao.insert(data);

                    // 如果mCallBack为空，证明还未启动MainActivity，无需update
                    if (mCallBack != null)
                        mCallBack.notifyUpdate();
                }

                @Override
                public void onFail(String msg) {
                }
            });
        }

    }

    // sync data from server
    private void syncOldData(){
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if(!sp.getBoolean("sync_done", false)){
            Log.d(TAG, "syncOldData");
            // start sync old data
            Thread syncThread = new Thread(){
              @Override
                public void run(){
                  FeedlyApiArgs args = new FeedlyApiArgs();
                  for(int count = 100; count < 2000; count++){
                      args.setCount(count);
                      apiHelper.getStream("", FeedlyApiHelper.API_GLOBAL_ALL_URL.replace(":userId", FeedlyApiHelper.USER_ID), args, new NetCallback<List<RssItem>>() {
                          @Override
                          public void onSuccess(List<RssItem> data) {
                              // insert to db
                              rssDao.insert(data);
                          }

                          @Override
                          public void onFail(String msg) {

                          }
                      });
                  }
                  // sync done
                  sp.edit().putBoolean("sync_done", true).apply();
              }
            };
            syncThread.start();

        }
    }

    // mark entries read
    public void markEntriesRead(){
        Thread markEntriesThread = new Thread(){
            @Override
            public void run() {
                // find unread=false items
                final List<RssItem> rssItems = rssDao.find(FeedlyApiHelper.API_GLOBAL_ALL_URL.replace(":userId", FeedlyApiHelper.USER_ID), false);
                if (!rssItems.isEmpty()) {
                    apiHelper.markStream("", rssItems, new NetCallback<String>() {
                        @Override
                        public void onSuccess(String data) {
                            // delete all items
                            rssDao.deleteSome(rssItems);
                        }

                        @Override
                        public void onFail(String msg) {

                        }
                    });
                    super.run();
                }
            }
        };
        markEntriesThread.start();
    }

    // time
    private void timeTrigger(){}

    public interface StreamCallBack {
        // 数据同步结束，更新UI
        void notifyUpdate();
    }
}
