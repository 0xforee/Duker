package org.foree.duker.rssinfo;

import java.util.Date;

/**
 * Created by foree on 16-7-19.
 * Rss Stream中的entry
 */
public class RssItem {
    // Item 标题
    private String title;
    // Item 概览
    private String summary;
    // Item 发布时间
    private Date pubDate;
    // Item ID
    private String entryId;
    // Itme 链接
    private String url;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
