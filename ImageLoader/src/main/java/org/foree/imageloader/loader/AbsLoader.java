package org.foree.imageloader.loader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import org.foree.imageloader.cache.BitmapCache;
import org.foree.imageloader.config.ImageLoaderConfig;
import org.foree.imageloader.core.MainImageLoader;
import org.foree.imageloader.request.BitMapRequest;

/**
 * Created by foree on 16-9-30.
 * 获取图片
 */

public abstract class AbsLoader implements ILoader{
    private static final String TAG = AbsLoader.class.getSimpleName();

    protected ImageLoaderConfig mConfig;
    private BitmapCache mBitmapCache;


    public AbsLoader(){
        mConfig = MainImageLoader.getInstance().getConfig();
        mBitmapCache = mConfig.bitmapCache;
    }

    public void loadImage(BitMapRequest request){
        // showLoadingImage
        showImage(request, BitmapFactory.decodeResource(
                mConfig.mContext.getResources(),
                mConfig.displayConfig.getLoadingResId())
        );

        // check memoryCache
        Bitmap bitmap = mBitmapCache.get(request.getImageUri());
        if( bitmap == null){

            // getImage
            bitmap = getBitmap(request);

            if( bitmap != null) {
                mBitmapCache.put(request.getImageUri(), bitmap);
            }
        }

        Log.d(TAG, "url = " + request.getImageUri());

        showImage(request, bitmap);
    }


    private void showImage(final BitMapRequest request, final Bitmap bitmap){
        final ImageView imageView = request.getImageView();
        // 分情况，
        // 1. 正在加载
        // 2. 已经加载成功
        // 3. 加载失败
        imageView.post(new Runnable() {
            @Override
            public void run() {
                if( bitmap == null){
                    // get Bitmap fail
                    imageView.setImageResource(mConfig.displayConfig.getFailResId());
                }else {
                    // get Bitmap success
                    if(imageView.getTag().equals(request.getImageUri()))
                        imageView.setImageBitmap(bitmap);
                }
            }
        });
    }
}
