package org.foree.imageloader.cache;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by foree on 16-9-30.
 * 内存缓存
 */

public class MemoryCache extends BitmapCache {

    private static MemoryCache mInstance;

    private LruCache<String, Bitmap> mMemoryCache;

    private MemoryCache(){
        // 设置LruCache的可用内存
        int memorySize = (int)Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = memorySize/8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                // 重写此方法来衡量每张图片的大小
                return value.getByteCount() / 1024;
            }
        };
    }

    public static MemoryCache getInstance(){
        if( mInstance == null ){
            synchronized (MemoryCache.class){
                if( mInstance == null){
                    mInstance = new MemoryCache();
                }
            }
        }

        return mInstance;
    }

    @Override
    public Bitmap get(String imageUrl){
        return mMemoryCache.get(imageUrl);
    }

    @Override
    public void put(String imageUrl, Bitmap bitmap){
        if( get(imageUrl) == null){
            mMemoryCache.put(imageUrl, bitmap);
        }
    }
}
