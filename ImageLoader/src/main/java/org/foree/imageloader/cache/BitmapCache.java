package org.foree.imageloader.cache;

import android.graphics.Bitmap;

/**
 * Created by foree on 16-9-30.
 */

public abstract class BitmapCache {
    public abstract Bitmap get(String imageUrl);
    public abstract void put(String imageUrl, Bitmap bitmap);
}
