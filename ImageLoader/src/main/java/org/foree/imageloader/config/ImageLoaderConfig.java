package org.foree.imageloader.config;

import android.content.Context;

import org.foree.imageloader.cache.BitmapCache;
import org.foree.imageloader.cache.DoubleCache;
import org.foree.imageloader.cache.MemoryCache;
import org.foree.imageloader.policy.LoadPolicy;
import org.foree.imageloader.policy.ReversePolicy;
import org.foree.imageloader.policy.SerialPolicy;

/**
 * Created by foree on 16-9-30.
 * 配置图片Loader的具体策略
 */

public class ImageLoaderConfig {
    public ImageLoaderConfig(){

    }

    /**
     * 上下文对象
     */
    public Context mContext;
    /**
     * 图片缓存策略
     */
    public BitmapCache bitmapCache = DoubleCache.getInstance();

    /**
     * 图片Loading与加载失败的配置对象
     */
    public DisplayConfig displayConfig = new DisplayConfig();

    /**
     * 图片加载策略
     */
    public LoadPolicy loadPolicy = new ReversePolicy();

    /**
     * 线程数量
     */
    public int threadCount = Runtime.getRuntime().availableProcessors() + 1;

    /**
     * 配置线程数量
     */
    public ImageLoaderConfig setThreadCount(int count){
        threadCount = Math.min(threadCount, count);
        return this;
    }

    public int getThreadCount(){
        return threadCount;
    }
    /**
     * 配置缓存机制
     * @return
     */
    public ImageLoaderConfig setCache(BitmapCache cache){
        bitmapCache = cache;
        return this;
    }

    /**
     * 配置加载策略
     */
    public ImageLoaderConfig setPolicy(LoadPolicy policy){
        loadPolicy = policy;
        return this;
    }

    /**
     * 配置显示设置
     */
    public ImageLoaderConfig setFailResId(int resId) {
        displayConfig.setFailResId(resId);
        return this;
    }

    public ImageLoaderConfig setLoadingResId(int resId){
        displayConfig.setLoadingResId(resId);
        return this;
    }

    public ImageLoaderConfig setContext(Context context){
        mContext = context;
        return this;
    }


}
