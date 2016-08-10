package org.foree.duker.api;


import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssItem;
import org.foree.duker.rssinfo.RssProfile;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by foree on 16-7-15.
 * 抽象ApiHelper类
 */
public abstract class AbsApiHelper {
    public abstract void getCategoriesList(String token, NetCallback<List<RssCategory>> netCallback);
    public abstract void getSubscriptions(String token, NetCallback<List<RssFeed>> netCallback);
    public abstract void getStream(String token, String streamId, FeedlyApiArgs args, NetCallback<List<RssItem>> netCallback);
    public abstract void getStreamGlobalAll(String token, FeedlyApiArgs args, NetCallback<List<RssItem>> netCallback);
    public abstract void getProfile(String token, NetCallback<RssProfile> netCallback);
    public abstract void getUnreadCounts(String token, NetCallback<Map<String, Long>> netCallback);
    public abstract void markStream(String token, List<RssItem> items, NetCallback<String> netCallback);
    public abstract void markAsOneRead(String token, RssItem items, NetCallback<String> netCallback);
}
