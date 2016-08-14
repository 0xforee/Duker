package org.foree.duker.ui.activity;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssProfile;
import org.foree.duker.service.StreamReceiverService;
import org.foree.duker.ui.fragment.ItemListFragment;
import org.foree.duker.utils.FeedlyApiUtils;

import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity implements OnDrawerItemClickListener, StreamReceiverService.StreamCallBack {
    private static final long PROFILE_SETTING = 100000;
    private static final long CATEGORY_IDENTIFIER = 20000;
    private static final long FEED_IDENTIFIER = 30000;
    private static final long OTHER_IDENTIFIER = 40000;
    private static final long DRAWITEM_HOME = 1;
    private static final long DRAWITEM_SETTINGS = 2;
    private static final long DRAWITEM_OPEN_SOURCE = 3;

    private static final String TAG = MainActivity.class.getSimpleName();

    private AccountHeader headerResult = null;
    private StreamReceiverService.MyBinder mBinder;
    private StreamReceiverService mStreamService;
    private ServiceConnection mServiceConnect = new MyServiceConnection();
    private Drawer result = null;

    AbsApiHelper localApiHelper, feedlyApiHelper;
    FloatingActionButton testFloatingButton;
    List<RssCategory> categoryList;
    List<RssFeed> feedList;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AbsApiFactory absApiFactory = new ApiFactory();
        feedlyApiHelper = absApiFactory.createApiHelper(FeedlyApiHelper.class);
        localApiHelper = absApiFactory.createApiHelper(LocalApiHelper.class);

        if (savedInstanceState == null) {
            Fragment f = ItemListFragment.newInstance(FeedlyApiUtils.getApiGlobalAllUrl());
            getFragmentManager().beginTransaction().replace(R.id.content_main, f).commit();
        }

        // bind service
        Intent intent = new Intent(this, StreamReceiverService.class);
        bindService(intent, mServiceConnect, BIND_AUTO_CREATE);

        setUpDrawerLayout(savedInstanceState);
        updateSubscriptions();

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
        unbindService(mServiceConnect);
        mStreamService.unregisterCallBack();
        mStreamService.markEntriesRead();
    }

    private void setUpDrawerLayout(Bundle savedInstanceState){

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

    private void updateSubscriptions(){

        updateProfile();
        updateCategories();
    }

    private void updateCategories() {
        localApiHelper.getCategoriesList("", new NetCallback<List<RssCategory>>() {
            @Override
            public void onSuccess(final List<RssCategory> data) {
                categoryList = data;
                updateFeeds();
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                Log.e(TAG,"getSubscription " + msg);
            }
        });
    }

    private void updateFeeds() {

        localApiHelper.getSubscriptions("", new NetCallback<List<RssFeed>>() {
            @Override
            public void onSuccess(List<RssFeed> data) {
                feedList = data;
                updateUnreadCounts();

            }
            @Override
            public void onFail(String msg) {
                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                Log.e(TAG,"getCategoriesList " + msg);
            }
        });
    }

    //TODO:解耦unread状态更新
    private void updateUnreadCounts() {

        feedlyApiHelper.getUnreadCounts("", new NetCallback<Map<String, Long>>() {
            @Override
            public void onSuccess(Map<String, Long> unReadCountsMap) {
                initDrawer(unReadCountsMap);
            }
            @Override
            public void onFail(String msg) {
                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                Log.e(TAG,"getUnreadCounts " + msg);
            }
        });
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

    private void initDrawer(Map<String, Long> unReadCountsMap) {

        BadgeStyle badgeStyle = new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.red);

        // Add Home
        result.addItem(new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIdentifier(DRAWITEM_HOME)
                .withBadge(new StringHolder(unReadCountsMap.get(FeedlyApiUtils.getApiGlobalAllUrl()) + "")).withBadgeStyle(badgeStyle));


        // Add Category
        for (int cate_i = 0; cate_i < categoryList.size(); cate_i++) {
            ExpandableDrawerItem expandableDrawerItem = new ExpandableDrawerItem()
                    .withName(categoryList.get(cate_i).getLabel()).withIdentifier(CATEGORY_IDENTIFIER +cate_i).withSelectable(false);
            result.addItem(expandableDrawerItem);

            for (int feed_i = 0; feed_i < feedList.size(); feed_i++) {
                for (int feed_cate_id = 0; feed_cate_id < feedList.get(feed_i).getCategories().size(); feed_cate_id++) {

                    if( feedList.get(feed_i).getCategories().get(feed_cate_id).equals(categoryList.get(cate_i))) {
                        expandableDrawerItem.withSubItems(new SecondaryDrawerItem().withName(feedList.get(feed_i).getName())
                                .withBadge(new StringHolder(unReadCountsMap.get(feedList.get(feed_i).getFeedId()) + "")).withIdentifier(FEED_IDENTIFIER + feed_i).withLevel(2).withBadgeStyle(badgeStyle));
                    }
                }
            }
        }

        // Add Settings and OpenSource
        result.addStickyFooterItem(new SecondaryDrawerItem().withName(R.string.drawer_item_settings).withIdentifier(DRAWITEM_SETTINGS));
        result.addStickyFooterItem(new SecondaryDrawerItem().withName(R.string.drawer_item_open_source).withIdentifier(DRAWITEM_OPEN_SOURCE));

    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        if (drawerItem != null){

            if( FEED_IDENTIFIER <= drawerItem.getIdentifier() && drawerItem.getIdentifier() < OTHER_IDENTIFIER){
                Log.d(TAG, "feedId = " + feedList.get((int)(drawerItem.getIdentifier()- FEED_IDENTIFIER)).getFeedId());
                Fragment f = ItemListFragment.newInstance(feedList.get((int)(drawerItem.getIdentifier()- FEED_IDENTIFIER)).getFeedId());
                getFragmentManager().beginTransaction().replace(R.id.content_main, f).commit();
            } else if (drawerItem.getIdentifier() == DRAWITEM_HOME){
                Fragment f = ItemListFragment.newInstance(FeedlyApiUtils.getApiGlobalAllUrl());
                getFragmentManager().beginTransaction().replace(R.id.content_main, f).commit();
            } else if (drawerItem.getIdentifier() == DRAWITEM_SETTINGS) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            } else if (drawerItem.getIdentifier() == DRAWITEM_OPEN_SOURCE) {

            }
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        Log.d(TAG, "updateUI");
    }

    private class MyServiceConnection implements ServiceConnection {
        private final String TAG = MyApplication.class.getSimpleName();

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected");
            mBinder = (StreamReceiverService.MyBinder) iBinder;
            mStreamService = mBinder.getService();
            mStreamService.registerCallBack(MainActivity.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected");

        }
    }
}
