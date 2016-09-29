package org.foree.duker.ui.activity;

import android.app.Fragment;
import android.content.ComponentName;
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
import android.widget.Toast;

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
import org.foree.duker.api.AbsApiFactory;
import org.foree.duker.api.AbsApiHelper;
import org.foree.duker.api.ApiFactory;
import org.foree.duker.api.LocalApiHelper;
import org.foree.duker.base.BaseActivity;
import org.foree.duker.base.BaseApplication;
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

public class MainActivity extends BaseActivity implements OnDrawerItemClickListener, SwipeRefreshLayout.OnRefreshListener, Drawer.OnDrawerListener {
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
    public static final int MSG_UPDATE_ENTRIES = 3;
    public static final int MSG_SYNC_ENTRIES_START = 4;
    public static final int MSG_SYNC_ENTRIES_SUCCESS = 5;
    public static final int MSG_SYNC_ENTRIES_FAIL = 6;

    @Override
    public void onDrawerOpened(View drawerView) {
        // 打开侧边栏的时候同步更新未读数量
        startSyncUnreadCounts();

    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    private class H extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_UPDATE_UNREAD:
                    startSyncUnreadCounts();
                    break;
                case MSG_UPDATE_SUBSCRIPTIONS:
                    updateSubscriptions();
                    break;
                case MSG_UPDATE_PROFILE:
                    updateProfile();
                    break;
                case MSG_UPDATE_ENTRIES:
                    Log.d(TAG, "update UI");
                    if( f != null)
                        ((SyncState)f).updateUI();
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

    AbsApiHelper localApiHelper;
    FloatingActionButton testFloatingButton;
    Map<RssCategory, List<RssFeed>> feedCateMap;
    Toolbar toolbar;
    SwipeRefreshLayout mSwipeRefreshLayout;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_ly);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        sp = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());

        AbsApiFactory absApiFactory = new ApiFactory();
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

    private void initSubscriptions(){
        updateProfile();
        updateSubscriptions();
    }

    private void updateSubscriptions() {
        localApiHelper.getFeedCate("", new NetCallback<Map<RssCategory, List<RssFeed>>>() {
            @Override
            public void onSuccess(final Map<RssCategory, List<RssFeed>> data) {
                feedCateMap = data;
                updateDrawItems();
                startSyncUnreadCounts();
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                Log.e(TAG,"getSubscription " + msg);
            }
        });
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

    private void startSyncUnreadCounts() {

        localApiHelper.getUnreadCounts("", new NetCallback<Map<String, Long>>() {
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
        if( feedCateMap != null) {
            for (RssCategory rss : feedCateMap.keySet()) {
                List<SecondaryDrawerItem> secondaryDrawerItems = ((IExpandable) result.getDrawerItem(rss.getCategoryId())).getSubItems();
                for (SecondaryDrawerItem secondaryDrawerItem : secondaryDrawerItems) {
                    secondaryDrawerItem.withBadge(new StringHolder(unReadCountsMap.get(secondaryDrawerItem.getTag() + "") + ""));
                }
                result.getAdapter().notifyAdapterSubItemsChanged(result.getPosition(CATEGORY_IDENTIFIER));
            }
        }
    }

    private void updateProfile() {

        // getProfile from db
        localApiHelper.getProfile("", new NetCallback<RssProfile>() {
            @Override
            public void onSuccess(RssProfile data) {
                final IProfile profile2 = new ProfileDrawerItem().withName(data.getFullName()).withEmail(data.getEmail()).withIcon(data.getPicture()).withIdentifier(100);
                headerResult.updateProfile(profile2);
            }
            @Override
            public void onFail(String msg) {
                Log.e(TAG, "updateProfile:" + msg);
            }
        });
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
            // feedItem and CategoryItem
            } else {
                Log.d(TAG, "feedId = " + drawerItem.getTag());
                f = ItemListFragment.newInstance((String) drawerItem.getTag());
                getFragmentManager().beginTransaction().replace(R.id.content_main, f).commit();
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        if( mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);
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
