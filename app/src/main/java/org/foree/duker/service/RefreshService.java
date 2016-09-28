package org.foree.duker.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import org.foree.duker.api.AbsApiFactory;
import org.foree.duker.api.AbsApiHelper;
import org.foree.duker.api.ApiFactory;
import org.foree.duker.api.FeedlyApiArgs;
import org.foree.duker.api.FeedlyApiHelper;
import org.foree.duker.base.BaseApplication;
import org.foree.duker.dao.RssDao;
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssItem;
import org.foree.duker.rssinfo.RssProfile;
import org.foree.duker.ui.activity.MainActivity;
import org.foree.duker.ui.activity.SettingsActivity;
import org.foree.duker.utils.FeedlyApiUtils;

import java.util.List;

/**
 * Service的职责是负责数据从网络到本地数据库的流向，以及通知activity更新UI
 * 与此相关联的activity的职责是负责数据从本地数据库到View的流向
 */
public class RefreshService extends Service {
    private static final String TAG = RefreshService.class.getSimpleName();

    private final int MSG_SYNC_ENTRIES_INTERNAL = 0;
    private final int MSG_REFRESH_ENTRIES = 1;
    private final int MSG_SYNC_SUBSCRIPTION = 2;

    AbsApiHelper feedlyApiHelper;

    RssDao rssDao;

    Handler mHandler;
    Messenger mainActivityMessenger;

    Thread timeTriggerThread;
    Thread syncEntriesThread;

    SharedPreferences sp;

    private MyBinder mBinder = new MyBinder();

    public RefreshService() {
    }

    public class MyBinder extends Binder {
        public RefreshService getService(){
            return RefreshService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        AbsApiFactory absApiFactory = new ApiFactory();
        feedlyApiHelper = absApiFactory.createApiHelper(FeedlyApiHelper.class);

        rssDao = new RssDao(this);

        sp = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_SYNC_ENTRIES_INTERNAL:
                        if (!sp.getString("continuation", "").isEmpty()) {
                            syncEntriesInternal();
                        }else{
                            sendToMainActivityEmptyMessage(MainActivity.MSG_SYNC_ENTRIES_COMPLETE);
                        }
                        break;
                    case MSG_REFRESH_ENTRIES:
                        syncEntries();
                        break;
                    case MSG_SYNC_SUBSCRIPTION:
                        syncFeeds();
                        break;

                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        // sync profile
        //syncProfile();

        // sync category
        syncCategory();

        timeTrigger();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        timeTriggerThread.stop();
        syncEntriesThread.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        mainActivityMessenger = (Messenger)intent.getExtras().get("handler");
        return mBinder;
    }

    // sync profile
    private void syncProfile(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                feedlyApiHelper.getProfile("", new NetCallback<RssProfile>() {
                    @Override
                    public void onSuccess(RssProfile data) {
                        rssDao.insertProfile(data);
                        sendToMainActivityEmptyMessage(MainActivity.MSG_UPDATE_PROFILE);
                    }

                    @Override
                    public void onFail(String msg) {
                        Log.e(TAG, "getProfileFail: " + msg);
                    }
                });
            }
        }.start();
    }

    // sync category
    private void syncCategory(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                feedlyApiHelper.getCategoriesList("", new NetCallback<List<RssCategory>>() {
                    @Override
                    public void onSuccess(List<RssCategory> data) {
                        rssDao.insertCategory(data);
                        mHandler.sendEmptyMessage(MSG_SYNC_SUBSCRIPTION);
                    }

                    @Override
                    public void onFail(String msg) {
                        Log.e(TAG, "getCategory Error: " + msg);
                    }
                });
            }
        }.start();
    }

    // sync feeds
    private void syncFeeds(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                feedlyApiHelper.getSubscriptions("", new NetCallback<List<RssFeed>>() {
                    @Override
                    public void onSuccess(List<RssFeed> data) {
                        rssDao.insertFeedAndSubCate(data);
                        sendToMainActivityEmptyMessage(MainActivity.MSG_UPDATE_SUBSCRIPTIONS);
                    }

                    @Override
                    public void onFail(String msg) {
                        Log.e(TAG, "getSubscription Error: " + msg);
                    }
                });
            }
        }.start();
    }

    // sync new data
    public void syncEntries() {
        Log.d(TAG, "syncEntries");

        FeedlyApiArgs args = new FeedlyApiArgs();
        args.setCount(100);

        if (sp.getLong("updated", 0) > 0) {
            args.setNewerThan(sp.getLong("updated", 0));
        }

        syncEntriesInternal(args);

    }

    private void syncEntriesInternal(){
        FeedlyApiArgs apiArgs = new FeedlyApiArgs();
        apiArgs.setCount(100);
        syncEntriesInternal(apiArgs);
    }

    // first import data from server
    private void syncEntriesInternal(final FeedlyApiArgs args) {
        // start sync old data
        syncEntriesThread = new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "get continuation");
                FeedlyApiArgs localArgs = args;
                localArgs.setContinuation(sp.getString("continuation", ""));
                feedlyApiHelper.getStreamGlobalAll("", localArgs, new NetCallback<List<RssItem>>() {
                    @Override
                    public void onSuccess(List<RssItem> data) {
                        // insertEntries to db
                        rssDao.insertEntries(data);

                        mHandler.sendEmptyMessage(MSG_SYNC_ENTRIES_INTERNAL);

                        sendToMainActivityEmptyMessage(MainActivity.MSG_UPDATE_ENTRIES);
                    }

                    @Override
                    public void onFail(String msg) {
                        sendToMainActivityEmptyMessage(MainActivity.MSG_SYNC_ENTRIES_COMPLETE);
                    }
                });

            }
        };
        syncEntriesThread.start();
    }

    // mark entries read
    public void markEntriesAsRead(){
        Thread markEntriesThread = new Thread(){
            @Override
            public void run() {
                // findUnreadEntriesByFeedId unread=false items
                final List<RssItem> rssItems = rssDao.findUnreadEntriesByFeedId(FeedlyApiUtils.getApiGlobalAllUrl(), false);
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
                            mHandler.sendEmptyMessage(MSG_REFRESH_ENTRIES);
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

    // sendToMainActivityEmptyMessage
    private void sendToMainActivityEmptyMessage(int what){
        Message msg = new Message();
        msg.what = what;
        if( mainActivityMessenger != null) {
            try {
                mainActivityMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
