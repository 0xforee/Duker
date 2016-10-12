package org.foree.imageloader.core;

import android.widget.ImageView;

import org.foree.imageloader.base.AppInfo;
import org.foree.imageloader.cache.DoubleCache;
import org.foree.imageloader.config.ImageLoaderConfig;
import org.foree.imageloader.request.BitMapRequest;

/**
 * Created by foree on 16-9-30.
 * 负责ImageLoader的配置与加载图片,入口
 */

public class MainImageLoader {

    private static MainImageLoader mInstance;
    private static final Object syncObject = new Object();

    private ImageLoaderConfig imageLoaderConfig;
    private RequestQueue mRequestQueue;

    private MainImageLoader(){

    }

    public static MainImageLoader getInstance(){
        // double check
        if( mInstance == null){
            synchronized (syncObject){
                if( mInstance == null){
                    // lazy initialization
                    mInstance = new MainImageLoader();
                }
            }
        }

        return mInstance;
    }

    /**
     * 初始化
     */
    public void init(ImageLoaderConfig config){
        imageLoaderConfig = config;

        new AppInfo(imageLoaderConfig.mContext);

        checkConfig();

        mRequestQueue = new RequestQueue(imageLoaderConfig.getThreadCount());
        mRequestQueue.start();
    }

    private void checkConfig() {
        // 必须设置上下文
        if (imageLoaderConfig == null){
            throw new RuntimeException("The config of MainImageLoader is Null, please call the init(ImageLoaderConfig config) method to initialize");
        }else{
            if( imageLoaderConfig.mContext == null){
                throw new RuntimeException("The context of MainImageLoader is Null, please call the setContext(context) to initialize");
            }
            if (imageLoaderConfig.bitmapCache == null){
                imageLoaderConfig.bitmapCache = DoubleCache.getInstance();
            }
        }
    }

    /**
     * 根据schema显示图片到ImageView
     */
    public void displayImage(ImageView imageView, String uri){
        BitMapRequest request = new BitMapRequest(imageView, uri);
        mRequestQueue.add(request);
    }

    public ImageLoaderConfig getConfig(){
        return imageLoaderConfig;
    }
}
