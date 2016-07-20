package org.foree.duker.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.foree.duker.R;
import org.foree.duker.api.AbsApiFactory;
import org.foree.duker.api.AbsApiHelper;
import org.foree.duker.api.ApiFactory;
import org.foree.duker.api.FeedlyApiHelper;
import org.foree.duker.base.BaseActivity;
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;

import java.util.List;

public class MainActivity extends BaseActivity{
    private static final int PROFILE_SETTING = 100000;
    private static final int CATEGORY_INDENTIFIER = 20000;
    private static final int FEED_INDENTIFIER = 30000;
    private static final String TAG = MainActivity.class.getSimpleName();

    private AccountHeader headerResult = null;
    private Drawer result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create a few sample profile
        // NOTE you have to define the loader logic too. See the CustomApplication for more details
        final IProfile profile = new ProfileDrawerItem().withName("Mike Penz").withEmail("mikepenz@gmail.com").withIcon("https://avatars3.githubusercontent.com/u/1476232?v=3&s=460").withIdentifier(100);

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
                .build();

        AbsApiFactory absApiFactory = new ApiFactory();
        final AbsApiHelper apiHelper = absApiFactory.createApiHelper(FeedlyApiHelper.class);

        apiHelper.getCategoriesList("", new NetCallback<RssCategory>() {
            @Override
            public void onSuccess(List<RssCategory> data) {
                final List<RssCategory> categoryList = data;
                apiHelper.getSubscriptions("", new NetCallback<RssFeed>() {
                    @Override
                    public void onSuccess(List<RssFeed> data) {
                        for (int cate_i = 0; cate_i < categoryList.size(); cate_i++) {
                            ExpandableDrawerItem expandableDrawerItem = new ExpandableDrawerItem().withName(categoryList.get(cate_i).getLabel()).withIdentifier(CATEGORY_INDENTIFIER+cate_i).withSelectable(false);
                            result.addItem(expandableDrawerItem);
                            for (int feed_i = 0; feed_i < data.size(); feed_i++) {
                                for (int feed_cate_id = 0; feed_cate_id < data.get(feed_i).getCategoryIds().size(); feed_cate_id++) {
                                    if( data.get(feed_i).getCategoryIds().get(feed_cate_id).equals(categoryList.get(cate_i).getCategoryId()))
                                    expandableDrawerItem.withSubItems(new SecondaryDrawerItem().withName(data.get(feed_i).getName()).withIdentifier(FEED_INDENTIFIER+feed_i));
                                }
                            }
                        }
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

    }
}
