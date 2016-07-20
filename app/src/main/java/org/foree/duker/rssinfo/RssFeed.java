package org.foree.duker.rssinfo;

import java.util.List;

/**
 * Created by foree on 16-7-19.
 * Rss源
 */
public class RssFeed {

    // FeedId
    private String feedId;
    // 订阅号的名称
    private String name = null;
    // 订阅号的rss链接
    private String url = null;
    // 分类ID
    private List<String> categoryIds;

    public String getFeedId() {
        return feedId;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getCategoryIds() {
        return categoryIds;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCategoryIds(List<String> categoryIds) {
        this.categoryIds = categoryIds;
    }
}
