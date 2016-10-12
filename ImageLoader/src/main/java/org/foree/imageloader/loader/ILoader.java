package org.foree.imageloader.loader;

import android.graphics.Bitmap;

import org.foree.imageloader.request.BitMapRequest;

/**
 * Created by foree on 16-10-11.
 */

public interface ILoader {
    Bitmap getBitmap(BitMapRequest request);
}
