package org.foree.duker.ui.activity;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
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
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssProfile;
import org.foree.duker.ui.fragment.ItemListFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity{
    private static final long PROFILE_SETTING = 100000;
    private static final long CATEGORY_INDENTIFIER = 20000;
    private static final long FEED_INDENTIFIER = 30000;
    private static final long OTHER_INDENTIFIER = 40000;

    private static final String TAG = MainActivity.class.getSimpleName();

    private AccountHeader headerResult = null;
    private Drawer result = null;
    AbsApiHelper apiHelper,localApiHelper;
    FloatingActionButton testFloatingButton;
    Map<String, Long> badgeStyleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AbsApiFactory absApiFactory = new ApiFactory();
        apiHelper = absApiFactory.createApiHelper(FeedlyApiHelper.class);
        localApiHelper = absApiFactory.createApiHelper(LocalApiHelper.class);

        if (savedInstanceState == null) {
            Fragment f = ItemListFragment.newInstance("");
            getFragmentManager().beginTransaction().replace(R.id.content_main, f).commit();
        }

        initProfile(savedInstanceState);

        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withAccountHeader(headerResult)
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .build();

        localApiHelper.getCategoriesList("", new NetCallback<List<RssCategory>>() {
            @Override
            public void onSuccess(final List<RssCategory> categoryList) {
                localApiHelper.getSubscriptions("", new NetCallback<List<RssFeed>>() {
                    @Override
                    public void onSuccess(List<RssFeed> feedList) {
                        initBadgeStyle(categoryList,feedList);

                    }
                    @Override
                    public void onFail(String msg) {
                        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                        Log.e(TAG,"getCategoriesList " + msg);
                    }
                });

            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                Log.e(TAG,"getSubscription " + msg);
            }
        });


        // get FloatActionButton
        testFloatingButton = (FloatingActionButton)findViewById(R.id.fab);
        testFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apiHelper.getUnreadCounts("",null);
            }
        });

    }

    private void initBadgeStyle(final List<RssCategory> categoryList, final List<RssFeed> feedList) {
        badgeStyleMap = new HashMap<>();
        apiHelper.getUnreadCounts("", new NetCallback<String>() {
            @Override
            public void onSuccess(String data) {
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    JSONArray unreadArry = jsonObject.getJSONArray("unreadcounts");
                    for (int unread_i = 0; unread_i < unreadArry.length(); unread_i++) {
                        JSONObject id = unreadArry.getJSONObject(unread_i);
                        String identifier = id.getString("id");
                        Log.d(TAG, "identifier = " + identifier + " count = " + id.getLong("count"));
                        badgeStyleMap.put(identifier, id.getLong("count"));
                    }

                    initDrawer(categoryList, feedList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(String msg) {

            }
        });
    }

    private void initProfile(final Bundle savedInstanceState) {

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

    private void initDrawer(final List<RssCategory> categoryList, final List<RssFeed> feedList) {
        BadgeStyle badgeStyle = new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.red);
        // Add Home
        result.addItem(new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIdentifier(1).withBadgeStyle(badgeStyle));

        // Add Category
        for (int cate_i = 0; cate_i < categoryList.size(); cate_i++) {
            ExpandableDrawerItem expandableDrawerItem = new ExpandableDrawerItem().withName(categoryList.get(cate_i).getLabel()).withIdentifier(CATEGORY_INDENTIFIER+cate_i).withSelectable(false);
            result.addItem(expandableDrawerItem);
            for (int feed_i = 0; feed_i < feedList.size(); feed_i++) {
                for (int feed_cate_id = 0; feed_cate_id < feedList.get(feed_i).getCategoryIds().size(); feed_cate_id++) {
                    if( feedList.get(feed_i).getCategoryIds().get(feed_cate_id).equals(categoryList.get(cate_i).getCategoryId())) {
                        expandableDrawerItem.withSubItems(new SecondaryDrawerItem().withName(feedList.get(feed_i).getName())
                                .withBadge(new StringHolder(badgeStyleMap.get(feedList.get(feed_i).getFeedId()) + "")).withIdentifier(FEED_INDENTIFIER + feed_i).withLevel(2).withBadgeStyle(badgeStyle));
                    }
                }
            }
        }
        // Add Settings and OpenSource
        result.addStickyFooterItem(new SecondaryDrawerItem().withName(R.string.drawer_item_settings).withIdentifier(2));
        result.addStickyFooterItem(new SecondaryDrawerItem().withName(R.string.drawer_item_open_source).withIdentifier(3));

        // Set OnClickListener
        result.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                if (drawerItem != null){
                    if( FEED_INDENTIFIER <= drawerItem.getIdentifier() && drawerItem.getIdentifier() < OTHER_INDENTIFIER){
                        Log.d(TAG, "Identifier = " + drawerItem.getIdentifier());
                        Log.d(TAG, "feedId = " + feedList.get((int)(drawerItem.getIdentifier()-FEED_INDENTIFIER)).getFeedId());
                        Fragment f = ItemListFragment.newInstance(feedList.get((int)(drawerItem.getIdentifier()-FEED_INDENTIFIER)).getFeedId());
                        getFragmentManager().beginTransaction().replace(R.id.content_main, f).commit();
                    } else if (drawerItem.getIdentifier() == 1){
                        Fragment f = ItemListFragment.newInstance(FeedlyApiHelper.API_GLOBAL_ALL_URL.replace(":userId", FeedlyApiHelper.USER_ID));
                        getFragmentManager().beginTransaction().replace(R.id.content_main, f).commit();
                    } else if (drawerItem.getIdentifier() == 2) {
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                    } else if (drawerItem.getIdentifier() == 3) {

                    }
                }
                return false;
            }
        });
    }
}
