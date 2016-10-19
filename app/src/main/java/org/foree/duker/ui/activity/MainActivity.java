package org.foree.duker.ui.activity;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.fastadapter.IExpandable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.foree.duker.R;
import org.foree.duker.base.BaseActivity;
import org.foree.duker.base.BaseApplication;
import org.foree.duker.provider.MainObserver;
import org.foree.duker.provider.RssObserver;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssProfile;
import org.foree.duker.service.RefreshService;
import org.foree.duker.ui.fragment.ItemListFragment;
import org.foree.duker.ui.presenter.IMainPresenter;
import org.foree.duker.ui.presenter.MainPresenterCompl;
import org.foree.duker.ui.view.IMainView;
import org.foree.duker.utils.FeedlyApiUtils;
import org.foree.imageloader.config.ImageLoaderConfig;
import org.foree.imageloader.core.MainImageLoader;

import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity implements OnDrawerItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, Drawer.OnDrawerListener, IMainView {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Drawer
    private static final long DRAW_ITEM_HOME = 1;
    private static final long DRAW_ITEM_OPEN_SOURCE = 2;
    private static final long PROFILE_SETTING = 100000;
    private static final long CATEGORY_IDENTIFIER = 20000;
    private static final long FEED_IDENTIFIER = 30000;
    private static final long OTHER_IDENTIFIER = 40000;
    private Drawer result = null;
    private AccountHeader headerResult = null;
    BadgeStyle badgeStyle;

    private Fragment f;

    private RefreshService.MyBinder mBinder;
    private RefreshService mStreamService;
    private ServiceConnection mServiceConnect = new MyServiceConnection();

    private Handler mHandler = new H();
    public static final int MSG_UPDATE_UNREAD = 0;
    public static final int MSG_UPDATE_SUBSCRIPTIONS = 1;
    public static final int MSG_UPDATE_PROFILE = 2;
    public static final int MSG_SYNC_ENTRIES_START = 4;
    public static final int MSG_SYNC_ENTRIES_SUCCESS = 5;
    public static final int MSG_SYNC_ENTRIES_FAIL = 6;
    
    private class H extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_UPDATE_UNREAD:
                    mainPresenter.getUnreadCounts();
                    break;
                case MSG_UPDATE_SUBSCRIPTIONS:
                    mainPresenter.getSubscriptions();
                    break;
                case MSG_UPDATE_PROFILE:
                    mainPresenter.getProfile();
                    break;
                case MSG_SYNC_ENTRIES_START:
                    break;
                case MSG_SYNC_ENTRIES_SUCCESS:
                    Log.d(TAG, "sync entries success");
                    resetRefresh();
                    break;
                case MSG_SYNC_ENTRIES_FAIL:
                    Log.d(TAG, "sync entries fail");
                    resetRefresh();
                    Snackbar.make(mSwipeRefreshLayout,(String)msg.obj, Snackbar.LENGTH_SHORT).show();
                    break;

            }
        }
    }

    FloatingActionButton testFloatingButton;
    Map<RssCategory, List<RssFeed>> feedCateMap;
    Toolbar toolbar;
    SwipeRefreshLayout mSwipeRefreshLayout;
    SharedPreferences sp;
    IMainPresenter mainPresenter;
    ContentResolver mContentResolver;
    RssObserver mObserver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_ly);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        sp = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());

        if (savedInstanceState == null) {
            f = ItemListFragment.newInstance(FeedlyApiUtils.getApiGlobalAllUrl());
            getFragmentManager().beginTransaction().replace(R.id.content_main, f).commit();
        }

        // bind service
        Intent refreshService = new Intent(this, RefreshService.class);
        refreshService.putExtra("handler", new Messenger(mHandler));
        bindService(refreshService, mServiceConnect, BIND_AUTO_CREATE);

        // MainPresenter init
        mainPresenter = new MainPresenterCompl(this);

        initImageLoader();

        initDraw(savedInstanceState);
        initUserData();

        // get FloatActionButton
        testFloatingButton = (FloatingActionButton)findViewById(R.id.fab);
        testFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mObserver = new MainObserver(mHandler);
        getContentResolver().registerContentObserver(RssObserver.URI_ENTRY, true, mObserver);
        getContentResolver().registerContentObserver(RssObserver.URI_PROFILE, true, mObserver);
        getContentResolver().registerContentObserver(RssObserver.URI_CATEGORY, true, mObserver);
        getContentResolver().registerContentObserver(RssObserver.URI_FEED, true, mObserver);
        getContentResolver().registerContentObserver(RssObserver.URI_SUB_CATE, true, mObserver);

    }

    private void initImageLoader() {
        ImageLoaderConfig config = new ImageLoaderConfig();
        config.setContext(this)
                .setThreadCount(4)
                .setFailResId(R.drawable.error)
                .setLoadingResId(R.drawable.placeholder);

        MainImageLoader.getInstance().init(config);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mStreamService.markEntriesAsRead();
        unbindService(mServiceConnect);
        getContentResolver().unregisterContentObserver(mObserver);
    }

    private void initDraw(Bundle savedInstanceState){

        final IProfile profile = new ProfileDrawerItem().withName("").withEmail("").withIcon("").withIdentifier(100);

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
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
                .withOnDrawerListener(this)
                .build();


        badgeStyle = new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.red);

    }

    private void initUserData(){
        mainPresenter.getProfile();
        mainPresenter.getSubscriptions();
        mainPresenter.getUnreadCounts();
    }

    @Override
    public void updateProfile(RssProfile rssProfile) {
        final IProfile profile2 = new ProfileDrawerItem().withName(rssProfile.getFullName()).withEmail(rssProfile.getEmail()).withIcon(rssProfile.getPicture()).withIdentifier(100);
        headerResult.updateProfile(profile2);
    }

    @Override
    public void updateSubscriptions(Map<RssCategory, List<RssFeed>> data) {
        feedCateMap = data;
        updateDrawItems();
    }

    @Override
    public void updateUnreadCounts(Map<String, Long> unReadCountsMap) {

        if( result.getDrawerItem(DRAW_ITEM_HOME) != null) {
            // update All unreadCounts
            ((PrimaryDrawerItem) result.getDrawerItem(DRAW_ITEM_HOME)).withBadge(new StringHolder(unReadCountsMap.get(result.getDrawerItem(DRAW_ITEM_HOME).getTag() + "") + ""));
            result.getAdapter().notifyAdapterItemChanged(result.getPosition(DRAW_ITEM_HOME));

            // update feed unreadCounts
            if (feedCateMap != null) {
                for (RssCategory rss : feedCateMap.keySet()) {
                    List<SecondaryDrawerItem> secondaryDrawerItems = ((IExpandable) result.getDrawerItem(rss.getCategoryId())).getSubItems();
                    for (SecondaryDrawerItem secondaryDrawerItem : secondaryDrawerItems) {
                        secondaryDrawerItem.withBadge(new StringHolder(unReadCountsMap.get(secondaryDrawerItem.getTag() + "") + ""));
                    }
                    result.getAdapter().notifyAdapterSubItemsChanged(result.getPosition(CATEGORY_IDENTIFIER));
                }
            }
        }
    }

    /**
     * remove all Item first, and add again
     */
    private void updateDrawItems() {
        // Remove all Items
        result.removeAllItems();

        // Add Home
        result.addItem(new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIdentifier(DRAW_ITEM_HOME)
                .withBadgeStyle(badgeStyle).withTag(FeedlyApiUtils.getApiGlobalAllUrl()));

        // Add feeds
        if (feedCateMap != null) {
            for (RssCategory rssCategory : feedCateMap.keySet()) {
                ExpandableDrawerItem expandableDrawerItem = new ExpandableDrawerItem()
                        .withName(rssCategory.getLabel())
                        .withSelectable(false)
                        .withTag(rssCategory.getCategoryId());

                result.addItem(expandableDrawerItem);

                for (RssFeed rssFeed : feedCateMap.get(rssCategory)) {
                    expandableDrawerItem.withSubItems(new SecondaryDrawerItem().withName(rssFeed.getName())
                            .withLevel(2)
                            .withBadgeStyle(badgeStyle)
                            .withTag(rssFeed.getFeedId()));
                }
            }
        }

        // Add openSource
        result.addItem(new DividerDrawerItem());
        result.addItem(new PrimaryDrawerItem().withIdentifier(DRAW_ITEM_OPEN_SOURCE).withName(R.string.drawer_item_open_source));
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        if (drawerItem != null){
            if (drawerItem.getIdentifier() == DRAW_ITEM_HOME){
                f = ItemListFragment.newInstance(FeedlyApiUtils.getApiGlobalAllUrl());
                getFragmentManager().beginTransaction().replace(R.id.content_main, f).commit();
            } else if (drawerItem.getIdentifier() == DRAW_ITEM_OPEN_SOURCE) {
                Uri uri = Uri.parse(getString(R.string.github_address));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                // feedItem filter
            } else if (drawerItem.getTag().toString().contains("feed")) {
                Log.d(TAG, "feedId = " + drawerItem.getTag());
                f = ItemListFragment.newInstance((String) drawerItem.getTag());
                getFragmentManager().beginTransaction().replace(R.id.content_main, f).commit();
            }
        }
        return false;
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        // 打开侧边栏的时候同步更新未读数量
        mainPresenter.getUnreadCounts();

    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if( mSwipeRefreshLayout.isRefreshing() ) {
            menu.findItem(R.id.action_stop_refresh).setVisible(true);
            menu.findItem(R.id.action_refresh).setVisible(false);
        } else{
            menu.findItem(R.id.action_stop_refresh).setVisible(false);
            menu.findItem(R.id.action_refresh).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_refresh:
                refresh();
                break;
            case R.id.action_stop_refresh:
                resetRefresh();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(!mSwipeRefreshLayout.isRefreshing()) {
                    Log.d(TAG, "onRefreshing");
                    mSwipeRefreshLayout.setRefreshing(true);
                    onRefresh();
                }
            }
        });

    }

    private void resetRefresh(){
        if( mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
            if( mStreamService != null)
                mStreamService.stopSync();
        }
    }

    @Override
    public void onRefresh() {
        if ( mStreamService != null){
            mStreamService.syncEntries();
        }
    }

    private class MyServiceConnection implements ServiceConnection {
        private final String TAG = MyServiceConnection.class.getSimpleName();

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected");
            mBinder = (RefreshService.MyBinder) iBinder;
            mStreamService = mBinder.getService();

            // start sync entry
            if (sp.getBoolean(SettingsActivity.KEY_REFRESH_ON_LAUNCH, true)) {
                refresh();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected");

        }
    }

}
