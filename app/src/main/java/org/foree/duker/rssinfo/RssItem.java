package org.foree.duker.rssinfo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by foree on 16-7-19.
 * Rss Stream中的entry
 */
public class RssItem implements Serializable{
    // Item 标题
    private String title;
    // Item 概览
    private String summary;
    // Item 发布时间
    private long published;
    // Item ID
    private String entryId;
    // Item feedId
    private String feedId;

    public String getVisual() {
        return visual;
    }

    public void setVisual(String visual) {
        this.visual = visual;
    }

    // Item visual
    private String visual;
    // Item 链接
    private String url;
    // Item categories
    private List<RssCategory> categories;

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    // Item feedName
    private String feedName;
    // Item unread;
    private boolean unread;

    public RssItem (){}

    public RssItem (String entryId, String title, String url, String feedName, boolean unread, long published){
        this.entryId = entryId;
        this.title = title;
        this.url = url;
        this.unread = unread;
        this.published = published;
        this.feedName = feedName;
    }


    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public long getPublished() {
        return published;
    }

    public String getEntryId() {
        return entryId;
    }

    public String getUrl() {
        return url;
    }

    public List<RssCategory> getCategories() {
        return categories;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setPublished(long published) {
        this.published = published;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCategories(List<RssCategory> categoryList){
        categories = categoryList;
    }
}
