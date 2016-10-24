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
import org.foree.duker.dao.RssDaoHelper;
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssItem;
import org.foree.duker.rssinfo.RssProfile;
import org.foree.duker.ui.activity.MainActivity;
import org.foree.duker.utils.FeedlyApiUtils;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    RssDaoHelper rssDaoHelper;

    Handler mHandler;
    Messenger mainActivityMessenger;

    SharedPreferences sp;

    ScheduledExecutorService scheduleExecutor = Executors.newScheduledThreadPool(4);
    int scheduleTime;

    Runnable entryRunnable;

    private MyBinder mBinder = new MyBinder();

    public RefreshService() {
    }

    public void stopSync() {
        if(!scheduleExecutor.isShutdown())
            scheduleExecutor.shutdown();
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

        rssDaoHelper = new RssDaoHelper(this);

        sp = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_SYNC_ENTRIES_INTERNAL:
                        if (!sp.getString("continuation", "").isEmpty()) {
                            syncEntriesInternal();
                        }else{
                            sendMainActivityEmptyMessage(MainActivity.MSG_SYNC_ENTRIES_SUCCESS);
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

    private void resetTimeTrigger() {
        // add entry sync time trigger
        if(entryRunnable == null) {
            entryRunnable = new Runnable() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(MSG_REFRESH_ENTRIES);

                }
            };

            scheduleExecutor.scheduleAtFixedRate(entryRunnable, 1, scheduleTime, TimeUnit.SECONDS);

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        // getScheduleTime from settings
        scheduleTime = 5;

        resetTimeTrigger();

        // sync profile
        //syncProfile();

        // sync category
        syncCategory();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        mainActivityMessenger = (Messenger)intent.getExtras().get("handler");
        return mBinder;
    }

    // sync profile
    private void syncProfile(){
        scheduleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                feedlyApiHelper.getProfile("", new NetCallback<RssProfile>() {
                    @Override
                    public void onSuccess(RssProfile data) {
                        rssDaoHelper.insertProfile(data);
                    }

                    @Override
                    public void onFail(String msg) {
                        Log.e(TAG, "getProfileFail: " + msg);
                    }
                });
            }
        });

    }

    // sync category
    private void syncCategory(){
        scheduleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                feedlyApiHelper.getCategoriesList("", new NetCallback<List<RssCategory>>() {
                    @Override
                    public void onSuccess(List<RssCategory> data) {
                        rssDaoHelper.insertCategory(data);
                    }

                    @Override
                    public void onFail(String msg) {
                        Log.e(TAG, "getCategory Error: " + msg);
                    }
                });
            }
        });

    }

    // sync feeds
    private void syncFeeds(){
        scheduleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                feedlyApiHelper.getSubscriptions("", new NetCallback<List<RssFeed>>() {
                    @Override
                    public void onSuccess(List<RssFeed> data) {
                        rssDaoHelper.insertFeedAndSubCate(data);
                    }

                    @Override
                    public void onFail(String msg) {
                        Log.e(TAG, "getSubscription Error: " + msg);
                    }
                });
            }
        });

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
        scheduleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "get continuation");
                FeedlyApiArgs localArgs = args;
                localArgs.setContinuation(sp.getString("continuation", ""));
                feedlyApiHelper.getStreamGlobalAll("", localArgs, new NetCallback<List<RssItem>>() {
                    @Override
                    public void onSuccess(List<RssItem> data) {
                        // insertEntries to db
                        //rssDao.insertEntries(data);
                        rssDaoHelper.insertEntryData(data);

                        mHandler.sendEmptyMessage(MSG_SYNC_ENTRIES_INTERNAL);

                    }

                    @Override
                    public void onFail(String msg) {
                        sendMainActivityMessage(MainActivity.MSG_SYNC_ENTRIES_FAIL, msg);
                    }
                });
            }
        });

    }

    // mark entries read
    public void markEntriesAsRead(){
        scheduleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                // findUnreadEntriesByFeedId unread=false itemList.get(i)s
                final List<RssItem> rssItems = rssDaoHelper.findUnreadEntriesByFeedId(FeedlyApiUtils.getApiGlobalAllUrl(), false);
                if (!rssItems.isEmpty()) {
                    feedlyApiHelper.markStream("", rssItems, new NetCallback<String>() {
                        @Override
                        public void onSuccess(String data) {
                            // delete all itemList.get(i)s
                            rssDaoHelper.deleteEntries(rssItems);
                        }

                        @Override
                        public void onFail(String msg) {
                        }
                    });
                }
            }
        });

    }

    // sendMainActivityEmptyMessage
    private void sendMainActivityEmptyMessage(int what){
        sendMainActivityMessage(what, null);
    }

    private void sendMainActivityMessage(int what, Object object){
        Message msg = new Message();
        msg.what = what;
        msg.obj = object;
        if( mainActivityMessenger != null) {
            try {
                mainActivityMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}
