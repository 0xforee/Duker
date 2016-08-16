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

import java.util.ArrayList;
import java.util.List;

public class StreamReceiverService extends Service {
    private static final String TAG = StreamReceiverService.class.getSimpleName();
    private final int MSG_SYNC_OLD_DATA = 0;
    private final int MSG_SYNC_NEW_DATA = 1;
    AbsApiHelper localApiHelper, feedlyApiHelper;
    private List<StreamCallBack> mCallBacks = new ArrayList<>();
    RssDao rssDao;
    Handler myHandler;
    Thread timeTriggerThread;
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
        if(!mCallBacks.contains(callback))
            mCallBacks.add(callback);
    }

    public void unregisterCallBack(StreamCallBack callback){
        mCallBacks.remove(callback);
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
                        syncOldData();
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

        if (sp.getBoolean("start_sync", false)) {
            syncNewData();
        }

        timeTrigger();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        timeTriggerThread.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // sync new data
    public void syncNewData() {
        Log.d(TAG, "syncNewData");

        FeedlyApiArgs args = new FeedlyApiArgs();
        args.setCount(100);

        if (sp.getLong("updated", 0) > 0) {
            args.setNewerThan(sp.getLong("updated", 0));
        }

        feedlyApiHelper.getStreamGlobalAll("", args, new NetCallback<List<RssItem>>() {
            @Override
            public void onSuccess(final List<RssItem> data) {
                // success insertEntries to db
                rssDao.insertEntries(data);

                myHandler.sendEmptyMessage(MSG_SYNC_OLD_DATA);

                notifyCallbackUpdate();
            }

            @Override
            public void onFail(String msg) {
            }
        });

    }

    // sync data from server
    private void syncOldData() {
        // 100 continuation
        if (!sp.getBoolean("sync_done", false)) {
            Log.d(TAG, "syncOldData");
            // start sync old data
            Thread syncThread = new Thread() {
                @Override
                public void run() {
                    if (!sp.getString("continuation", "").isEmpty()) {
                        Log.d(TAG, "get continuation");
                        FeedlyApiArgs args = new FeedlyApiArgs();
                        args.setCount(100);
                        args.setContinuation(sp.getString("continuation", ""));
                        feedlyApiHelper.getStreamGlobalAll("", args, new NetCallback<List<RssItem>>() {
                            @Override
                            public void onSuccess(List<RssItem> data) {
                                // insertEntries to db
                                rssDao.insertEntries(data);

                                myHandler.sendEmptyMessage(MSG_SYNC_OLD_DATA);

                                // updateUI
                                notifyCallbackUpdate();
                            }

                            @Override
                            public void onFail(String msg) {

                            }
                        });
                    }else{
                        sp.edit().putBoolean("sync_done", true).apply();
                    }

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
                // findUnreadByFeedId unread=false items
                final List<RssItem> rssItems = rssDao.findUnreadByFeedId(FeedlyApiUtils.getApiGlobalAllUrl(), false);
                if (!rssItems.isEmpty()) {
                    feedlyApiHelper.markStream("", rssItems, new NetCallback<String>() {
                        @Override
                        public void onSuccess(String data) {
                            // delete all items
                            rssDao.deleteEntries(rssItems);
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
        if(timeTriggerThread == null) {
            Log.d(TAG, "create timeTrigger Thread");
            timeTriggerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            wait(1000 * 60 * 60);
                            myHandler.sendEmptyMessage(MSG_SYNC_NEW_DATA);
                            timeTrigger();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        if( timeTriggerThread.getState().equals(Thread.State.TERMINATED))
            timeTriggerThread.start();
    }

    private void notifyCallbackUpdate(){
        // 如果mCallBack为空，证明还未启动MainActivity，无需update
        if(mCallBacks != null && mCallBacks.size() > 0) {
            for (StreamCallBack callBack : mCallBacks) {
                callBack.notifyUpdate();
            }
        }
    }

    public interface StreamCallBack {
        // 数据同步结束，更新UI
        void notifyUpdate();
    }
}
