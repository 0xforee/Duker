package org.foree.duker.rssinfo;

/**
 * Created by foree on 16-7-19.
 * Rss分类
 */
public class RssCategory {

    private String categoryId;
    private String label;
    private String description;

    public String getCategoryId() {
        return categoryId;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setLable(String label) {
        this.label = label;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
