package org.foree.imageloader.cache;

import android.graphics.Bitmap;

/**
 * Created by foree on 16-10-11.
 */

public class DoubleCache extends BitmapCache{
    private static DoubleCache mInstance;

    private MemoryCache mMemoryCache;
    private DiskCache mDiskCache;

    private DoubleCache(){
        mMemoryCache = MemoryCache.getInstance();
        mDiskCache = DiskCache.getInstance();
    }

    public static DoubleCache getInstance(){
        if( mInstance == null){
            synchronized (DoubleCache.class){
                if( mInstance == null){
                    mInstance = new DoubleCache();
                }
            }
        }

        return mInstance;
    }

    @Override
    public Bitmap get(String imageUrl) {
        Bitmap bitmap = mMemoryCache.get(imageUrl);

        if( bitmap == null){
            // get bitmap from disk
            bitmap = mDiskCache.get(imageUrl);
            // put bitmap to memory
            if( bitmap != null)
                mMemoryCache.put(imageUrl, bitmap);
        }

        return bitmap;
    }

    @Override
    public void put(String imageUrl, Bitmap bitmap) {
        mMemoryCache.put(imageUrl, bitmap);
        mDiskCache.put(imageUrl, bitmap);
    }
}
