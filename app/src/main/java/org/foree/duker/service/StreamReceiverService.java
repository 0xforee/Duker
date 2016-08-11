package org.foree.duker.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import org.foree.duker.utils.FeedlyApiUtils;

import java.util.List;

public class StreamReceiverService extends Service {
    private static final String TAG = StreamReceiverService.class.getSimpleName();
    private final int MSG_SYNC_OLD_DATA = 0;
    private final int MSG_SYNC_NEW_DATA = 1;
    AbsApiHelper localApiHelper, feedlyApiHelper;
    private StreamCallBack mCallBack;
    RssDao rssDao;
    Handler myHandler;
    SharedPreferences sp;
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
        feedlyApiHelper = absApiFactory.createApiHelper(FeedlyApiHelper.class);
        localApiHelper = absApiFactory.createApiHelper(LocalApiHelper.class);
        rssDao = new RssDao(this);
        sp = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());

        myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_SYNC_OLD_DATA:
                        if( msg.arg1 < 2000 ){
                            syncOldData(msg.arg1 + 500);
                        } else{
                            // sync done
                            sp.edit().putBoolean("sync_done", true).apply();
                            myHandler.sendEmptyMessage(MSG_SYNC_NEW_DATA);
                        }
                        break;
                    case MSG_SYNC_NEW_DATA:
                        syncNewData();
                        break;

                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        // stage 1, sync old data
        syncOldData(100);

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
        Log.d(TAG, "syncNewData");
        FeedlyApiArgs args = new FeedlyApiArgs();

        //get continuation from SharedPreferences
        if (!sp.getString("continuation", "").isEmpty()) {
            Log.d(TAG, "get continuation");

            args.setContinuation(sp.getString("continuation", ""));
            //args.setCount(500);

            feedlyApiHelper.getStreamGlobalAll("", args, new NetCallback<List<RssItem>>() {
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
    private void syncOldData(final int counts) {
        if (!sp.getBoolean("sync_done", false)) {
            Log.d(TAG, "syncOldData");
            // start sync old data
            Thread syncThread = new Thread() {
                @Override
                public void run() {
                    FeedlyApiArgs args = new FeedlyApiArgs();
                    args.setCount(counts);
                    feedlyApiHelper.getStreamGlobalAll("", args, new NetCallback<List<RssItem>>() {
                        @Override
                        public void onSuccess(List<RssItem> data) {
                            // insert to db
                            rssDao.insert(data);

                            // post sync done
                            Message msg = new Message();
                            msg.what = MSG_SYNC_OLD_DATA;
                            msg.arg1 = counts;

                            myHandler.sendMessage(msg);

                            // updateUI
                            // 如果mCallBack为空，证明还未启动MainActivity，无需update
                            if (mCallBack != null)
                                mCallBack.notifyUpdate();
                        }

                        @Override
                        public void onFail(String msg) {

                        }
                    });
                }
            };
            syncThread.start();
        } else {
            syncNewData();
        }
    }

    // mark entries read
    public void markEntriesRead(){
        Thread markEntriesThread = new Thread(){
            @Override
            public void run() {
                // find unread=false items
                final List<RssItem> rssItems = rssDao.find(FeedlyApiUtils.getApiGlobalAllUrl(), false);
                if (!rssItems.isEmpty()) {
                    feedlyApiHelper.markStream("", rssItems, new NetCallback<String>() {
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
    private void timeTrigger(){
        Log.d(TAG, "timeTrigger");
    }

    public interface StreamCallBack {
        // 数据同步结束，更新UI
        void notifyUpdate();
    }
}
