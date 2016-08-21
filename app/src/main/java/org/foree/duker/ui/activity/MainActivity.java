package org.foree.duker.ui.activity;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.fastadapter.IExpandable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.foree.duker.R;
import org.foree.duker.api.AbsApiFactory;
import org.foree.duker.api.AbsApiHelper;
import org.foree.duker.api.ApiFactory;
import org.foree.duker.api.FeedlyApiHelper;
import org.foree.duker.api.LocalApiHelper;
import org.foree.duker.base.BaseActivity;
import org.foree.duker.base.MyApplication;
import org.foree.duker.net.NetCallback;
import org.foree.duker.net.SyncState;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssProfile;
import org.foree.duker.service.RefreshService;
import org.foree.duker.ui.fragment.ItemListFragment;
import org.foree.duker.utils.FeedlyApiUtils;

import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity implements OnDrawerItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final long PROFILE_SETTING = 100000;
    private static final long CATEGORY_IDENTIFIER = 20000;
    private static final long FEED_IDENTIFIER = 30000;
    private static final long OTHER_IDENTIFIER = 40000;
    private static final long DRAW_ITEM_HOME = 1;
    private static final long DRAW_ITEM_SETTINGS = 2;
    private static final long DRAW_ITEM_OPEN_SOURCE = 3;

    private static final String TAG = MainActivity.class.getSimpleName();

    private AccountHeader headerResult = null;
    private RefreshService.MyBinder mBinder;
    private RefreshService mStreamService;
    private ServiceConnection mServiceConnect = new MyServiceConnection();
    private Drawer result = null;
    private Fragment f;
    private Handler mHandler = new H();

    public static final int MSG_START_SYNC_UNREAD = 0;
    public static final int MSG_START_SYNC_FEEDS = 1;
    public static final int MSG_SYNC_COMPLETE = 2;

    private class H extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_START_SYNC_UNREAD:
                    startSyncUnreadCounts();
                    break;
                case MSG_START_SYNC_FEEDS:
                    break;
                case MSG_SYNC_COMPLETE:
                    Log.d(TAG, "sync done, update UI");
                    mSwipeRefreshLayout.setRefreshing(false);
                    ((SyncState)f).updateUI();
                    break;

            }
            super.handleMessage(msg);
        }
    }

    AbsApiHelper localApiHelper, feedlyApiHelper;
    FloatingActionButton testFloatingButton;
    List<RssCategory> categoryList;
    List<RssFeed> feedList;
    Toolbar toolbar;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_ly);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        AbsApiFactory absApiFactory = new ApiFactory();
        feedlyApiHelper = absApiFactory.createApiHelper(FeedlyApiHelper.class);
        localApiHelper = absApiFactory.createApiHelper(LocalApiHelper.class);

        if (savedInstanceState == null) {
            f = ItemListFragment.newInstance(FeedlyApiUtils.getApiGlobalAllUrl());
            getFragmentManager().beginTransaction().replace(R.id.content_main, f).commit();
        }

        // bind service
        Intent refreshService = new Intent(this, RefreshService.class);
        refreshService.putExtra("handler", new Messenger(mHandler));
        bindService(refreshService, mServiceConnect, BIND_AUTO_CREATE);

        initDraw(savedInstanceState);
        initSubscriptions();

        // get FloatActionButton
        testFloatingButton = (FloatingActionButton)findViewById(R.id.fab);
        testFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mStreamService.markEntriesAsRead();
        unbindService(mServiceConnect);
    }

    private void initDraw(Bundle savedInstanceState){

        // Create a few sample profile
        // NOTE you have to define the loader logic too. See the CustomApplication for more details
        final IProfile profile = new ProfileDrawerItem().withName("").withEmail("").withIcon("").withIdentifier(100);

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(MainActivity.this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        profile,
                        //don't ask but google uses 14dp for the add account icon in gmail but 20dp for the normal icons (like manage account)
                        new ProfileSettingDrawerItem().withName("Add Account").withDescription("Add new GitHub Account").withIdentifier(PROFILE_SETTING),
                        new ProfileSettingDrawerItem().withName("Manage Account").withIdentifier(100001)
                )
                .withSavedInstance(savedInstanceState)
                .build();

        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withAccountHeader(headerResult)
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .withOnDrawerItemClickListener(this)
                .build();

    }

    private void initSubscriptions(){

        updateProfile();
        initCategories();
    }

    private void initCategories() {
        localApiHelper.getCategoriesList("", new NetCallback<List<RssCategory>>() {
            @Override
            public void onSuccess(final List<RssCategory> data) {
                categoryList = data;
                initFeeds();
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                Log.e(TAG,"getSubscription " + msg);
            }
        });
    }

    private void initFeeds() {

        localApiHelper.getSubscriptions("", new NetCallback<List<RssFeed>>() {
            @Override
            public void onSuccess(List<RssFeed> data) {
                feedList = data;
                addDrawItems();
                mHandler.sendEmptyMessage(MSG_START_SYNC_UNREAD);
            }
            @Override
            public void onFail(String msg) {
                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                Log.e(TAG,"getCategoriesList " + msg);
            }
        });
    }

    private void startSyncUnreadCounts() {

        feedlyApiHelper.getUnreadCounts("", new NetCallback<Map<String, Long>>() {
            @Override
            public void onSuccess(Map<String, Long> unReadCountsMap) {
                updateDrawUnreadCounts(unReadCountsMap);
            }
            @Override
            public void onFail(String msg) {
                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                Log.e(TAG,"getUnreadCounts " + msg);
            }
        });
    }

    private void updateDrawUnreadCounts(Map<String, Long> unReadCountsMap) {
        // update All unreadCounts
        ((PrimaryDrawerItem)result.getDrawerItem(DRAW_ITEM_HOME)).withBadge(new StringHolder(unReadCountsMap.get(result.getDrawerItem(DRAW_ITEM_HOME).getTag() + "") + ""));
        result.getAdapter().notifyAdapterItemChanged(result.getPosition(DRAW_ITEM_HOME));

        // update feed unreadCounts
        for (int i = 0; i < categoryList.size(); i++) {
            List<SecondaryDrawerItem> secondaryDrawerItems = ((IExpandable) result.getDrawerItem(CATEGORY_IDENTIFIER+i)).getSubItems();
            for (SecondaryDrawerItem secondaryDrawerItem : secondaryDrawerItems) {
                secondaryDrawerItem.withBadge(new StringHolder(unReadCountsMap.get(secondaryDrawerItem.getTag() + "") + ""));
            }
            result.getAdapter().notifyAdapterSubItemsChanged(result.getPosition(CATEGORY_IDENTIFIER));
        }
    }

    private void updateProfile() {

        // getProfile from Network
        localApiHelper.getProfile("", new NetCallback<RssProfile>() {
            @Override
            public void onSuccess(RssProfile data) {
                final IProfile profile2 = new ProfileDrawerItem().withName(data.getFullName()).withEmail(data.getEmail()).withIcon(data.getPicture()).withIdentifier(100);
                headerResult.updateProfile(profile2);
            }
            @Override
            public void onFail(String msg) {

            }
        });
    }

    private void addDrawItems() {

        BadgeStyle badgeStyle = new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.red);

        // Add Home
        result.addItem(new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIdentifier(DRAW_ITEM_HOME)
                .withBadgeStyle(badgeStyle).withTag(FeedlyApiUtils.getApiGlobalAllUrl()));

        // Add Category
        for (int cate_i = 0; cate_i < categoryList.size(); cate_i++) {
            ExpandableDrawerItem expandableDrawerItem = new ExpandableDrawerItem()
                    .withName(categoryList.get(cate_i).getLabel()).withIdentifier(CATEGORY_IDENTIFIER +cate_i).withSelectable(false).withTag(categoryList.get(cate_i).getCategoryId());
            result.addItem(expandableDrawerItem);

            for (int feed_i = 0; feed_i < feedList.size(); feed_i++) {
                for (int feed_cate_id = 0; feed_cate_id < feedList.get(feed_i).getCategories().size(); feed_cate_id++) {

                    if( feedList.get(feed_i).getCategories().get(feed_cate_id).equals(categoryList.get(cate_i))) {
                        expandableDrawerItem.withSubItems(new SecondaryDrawerItem().withName(feedList.get(feed_i).getName())
                                .withIdentifier(FEED_IDENTIFIER + feed_i).withLevel(2).withBadgeStyle(badgeStyle).withTag(feedList.get(feed_i).getFeedId()));
                    }
                }
            }
        }

        // Add Settings and OpenSource
        result.addStickyFooterItem(new SecondaryDrawerItem().withName(R.string.drawer_item_settings).withIdentifier(DRAW_ITEM_SETTINGS));
        result.addStickyFooterItem(new SecondaryDrawerItem().withName(R.string.drawer_item_open_source).withIdentifier(DRAW_ITEM_OPEN_SOURCE));

    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        if (drawerItem != null){

            if( FEED_IDENTIFIER <= drawerItem.getIdentifier() && drawerItem.getIdentifier() < OTHER_IDENTIFIER){
                Log.d(TAG, "feedId = " + feedList.get((int)(drawerItem.getIdentifier()- FEED_IDENTIFIER)).getFeedId());
                f = ItemListFragment.newInstance(feedList.get((int)(drawerItem.getIdentifier()- FEED_IDENTIFIER)).getFeedId());
                getFragmentManager().beginTransaction().replace(R.id.content_main, f).commit();
            } else if (drawerItem.getIdentifier() == DRAW_ITEM_HOME){
                f = ItemListFragment.newInstance(FeedlyApiUtils.getApiGlobalAllUrl());
                getFragmentManager().beginTransaction().replace(R.id.content_main, f).commit();
            } else if (drawerItem.getIdentifier() == DRAW_ITEM_SETTINGS) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            } else if (drawerItem.getIdentifier() == DRAW_ITEM_OPEN_SOURCE) {

            }
        }
        return false;
    }

    @Override
    public void onRefresh() {
        if ( mStreamService != null){
            mStreamService.syncSubscriptions();
        }
    }
    private class MyServiceConnection implements ServiceConnection {
        private final String TAG = MyApplication.class.getSimpleName();

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected");
            mBinder = (RefreshService.MyBinder) iBinder;
            mStreamService = mBinder.getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected");

        }
    }

}
