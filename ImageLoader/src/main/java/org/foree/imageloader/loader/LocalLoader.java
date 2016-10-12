package org.foree.imageloader.loader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.foree.imageloader.R;
import org.foree.imageloader.request.BitMapRequest;

/**
 * Created by foree on 16-9-30.
 * 从Local查找文件
 */

public class LocalLoader extends AbsLoader {

    @Override
    public Bitmap getBitmap(BitMapRequest request) {
        return BitmapFactory.decodeResource(mConfig.mContext.getResources(), R.drawable.pubuliu);
    }
}
