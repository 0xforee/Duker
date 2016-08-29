package org.foree.duker.api;

import org.foree.duker.base.BaseApplication;
import org.foree.duker.base.MyApplication;
import org.foree.duker.dao.RssDao;
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssItem;
import org.foree.duker.rssinfo.RssProfile;
import org.foree.duker.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

        try {
            localCategories = FileUtils.readFile(category_json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(netCallback != null){
            if( !localCategories.isEmpty())
                netCallback.onSuccess(parseCategories(localCategories));
            else
                netCallback.onFail("rssCategories is empty");
        }
    }

    @Override
    public void getProfile(String token, final NetCallback<RssProfile> netCallback) {

        RssDao rssDao = new RssDao(BaseApplication.getInstance().getApplicationContext());
        RssProfile rssProfile = rssDao.readProfile();

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

        try {
            localSubscriptions = FileUtils.readFile(subscriptions_json);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        final RssDao rssDao = new RssDao(BaseApplication.getInstance().getApplicationContext());
        List<RssItem> rssItemList = rssDao.findUnreadByFeedId(streamId, true);

        if( netCallback != null) {
            if (!rssItemList.isEmpty()) {
                netCallback.onSuccess(rssItemList);
            } else {
                netCallback.onFail("rssItemList null");
            }
        }
    }

}
