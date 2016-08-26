package org.foree.duker.rssinfo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by foree on 16-7-19.
 * Rss分类
 */
public class RssCategory implements Serializable{

    @SerializedName("id")
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

    public RssCategory(String categoryId, String label, String description) {
        this.categoryId = categoryId;
        this.label = label;
        this.description = description;
    }

    public RssCategory(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RssCategory)) return false;

        RssCategory that = (RssCategory) o;

        if (categoryId != null ? !categoryId.equals(that.categoryId) : that.categoryId != null)
            return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        return description != null ? description.equals(that.description) : that.description == null;

    }
}
