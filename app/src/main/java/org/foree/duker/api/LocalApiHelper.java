package org.foree.duker.api;

import org.foree.duker.base.BaseApplication;
import org.foree.duker.base.MyApplication;
import org.foree.duker.dao.RssDaoHelper;
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssItem;
import org.foree.duker.rssinfo.RssProfile;
import org.foree.duker.utils.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 16-7-25.
 * 缓存操作类，包括数据库与本地文件缓存
 */
public class LocalApiHelper extends FeedlyApiHelper {

    private static final String TAG = LocalApiHelper.class.getSimpleName();

    @Override
    public void getCategoriesList(String token, final NetCallback<List<RssCategory>> netCallback) {

        String localCategories = "";

        final File category_json = new File(MyApplication.myApplicationDirPath + File.separator + MyApplication.myApplicationDataName + File.separator + "categories.json");

        localCategories = FileUtils.readFile(category_json);

        if(netCallback != null){
            if( !localCategories.isEmpty())
                netCallback.onSuccess(parseCategories(localCategories));
            else
                netCallback.onFail("rssCategories is empty");
        }
    }

    @Override
    public void getProfile(String token, final NetCallback<RssProfile> netCallback) {

        RssDaoHelper rssDaoHelper = new RssDaoHelper(BaseApplication.getInstance().getApplicationContext());
        RssProfile rssProfile = rssDaoHelper.readProfile();

        if(netCallback != null){
            if( rssProfile != null)
                netCallback.onSuccess(rssProfile);
            else
                netCallback.onFail("rssProfile is null from db");
        }

    }

    @Override
    public void getSubscriptions(String token, final NetCallback<List<RssFeed>> netCallback) {

        String localSubscriptions = "";

        final File subscriptions_json = new File(MyApplication.myApplicationDirPath + File.separator + MyApplication.myApplicationDataName + File.separator + "subscriptions.json");

        localSubscriptions = FileUtils.readFile(subscriptions_json);

        if (netCallback != null){
            if( !localSubscriptions.isEmpty()){
                netCallback.onSuccess(parseSubscriptions(localSubscriptions));
            }else{
                netCallback.onFail("localSubscription is empty");
            }
        }
    }

    @Override
    public void getStream(String token, String streamId, FeedlyApiArgs args, final NetCallback<List<RssItem>> netCallback) {

        // only get data from db
        final RssDaoHelper rssDaoHelper = new RssDaoHelper(BaseApplication.getInstance().getApplicationContext());
        List<RssItem> rssItemList = rssDaoHelper.findUnreadEntriesByFeedId(streamId, true);

        if( netCallback != null) {
            if (!rssItemList.isEmpty()) {
                netCallback.onSuccess(rssItemList);
            } else {
                netCallback.onFail("rssItemList null");
            }
        }
    }

    @Override
    public void getFeedCate(String token, NetCallback<Map<RssCategory, List<RssFeed>>> netCallback) {
        // only get data from db
        final RssDaoHelper rssDaoHelper = new RssDaoHelper(BaseApplication.getInstance().getApplicationContext());
        Map<RssCategory, List<RssFeed>> feedCateMap = rssDaoHelper.readFeedCate();

        if( netCallback != null) {
            if (!feedCateMap.isEmpty()) {
                netCallback.onSuccess(feedCateMap);
            } else {
                netCallback.onFail("feedCate is empty!");
            }
        }
    }

    @Override
    public void getUnreadCounts(String token, final NetCallback<Map<String, Long>> netCallback) {
        // only get data from db
        final RssDaoHelper rssDaoHelper = new RssDaoHelper(BaseApplication.getInstance().getApplicationContext());
        Map<String, Long> unReadCountsMap = rssDaoHelper.getUnreadCounts();

        if( netCallback != null) {
            if (!unReadCountsMap.isEmpty()) {
                netCallback.onSuccess(unReadCountsMap);
            } else {
                netCallback.onFail("read counts map is empty!");
            }
        }
    }

}
