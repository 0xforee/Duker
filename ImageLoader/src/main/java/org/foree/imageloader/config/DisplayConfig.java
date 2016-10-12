package org.foree.imageloader.config;

/**
 * Created by foree on 16-9-30.
 * 图片加载失败和loading时的配置
 */

public class DisplayConfig {
    private int loadingResId;
    private int failResId;

    public void setLoadingResId(int loadingResId) {
        this.loadingResId = loadingResId;
    }

    public void setFailResId(int failResId) {
        this.failResId = failResId;
    }

    public int getLoadingResId() {
        return loadingResId;
    }

    public int getFailResId() {
        return failResId;
    }
}
