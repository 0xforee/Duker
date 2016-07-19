package org.foree.duker.api;


import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssItem;

/**
 * Created by foree on 16-7-15.
 * 抽象ApiHelper类
 */
public abstract class AbsApiHelper {
    public abstract void getCategoriesList(String token, NetCallback<RssCategory> netCallback);
    public abstract void getSubscriptions(String token, NetCallback<RssFeed> netCallback);
    public abstract void getStream(String token, String streamId, NetCallback<RssItem> netCallback);
}
