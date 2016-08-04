package org.foree.duker.rssinfo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by foree on 16-7-19.
 * Rss源
 */
public class RssFeed {

    // FeedId
    @SerializedName("id")
    private String feedId;
    // 订阅号的名称
    private String title = null;
    // 订阅号的rss链接
    private String website = null;
    private String iconUrl = null;
    // 分类ID
    private List<RssCategory> categories;

    public String getFeedId() {
        return feedId;
    }

    public String getName() {
        return title;
    }

    public String getUrl() {
        return website;
    }

    public List<RssCategory> getCategories() {
        return categories;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public void setName(String title) {
        this.title = title;
    }

    public void setUrl(String website) {
        this.website = website;
    }

    public void setCategoryIds(List<RssCategory> categories) {
        this.categories = categories;
    }
}
