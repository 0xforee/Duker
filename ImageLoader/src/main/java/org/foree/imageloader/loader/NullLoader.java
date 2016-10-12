package org.foree.imageloader.loader;

import android.graphics.Bitmap;

import org.foree.imageloader.request.BitMapRequest;

/**
 * Created by foree on 16-9-30.
 * Null Object设计模式，提供空实现
 */

public class NullLoader extends AbsLoader {

    @Override
    public Bitmap getBitmap(BitMapRequest request) {
        return null;
    }
}
