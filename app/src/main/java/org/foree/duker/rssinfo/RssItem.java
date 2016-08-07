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
    // Item 链接
    private String url;
    // Item categories
    private List<RssCategory> categories;
    // Item unread;
    private boolean unread;

    public RssItem (){}

    public RssItem (String entryId, String title, String url, boolean unread, long published){
        this.entryId = entryId;
        this.title = title;
        this.url = url;
        this.unread = unread;
        this.published = published;
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
