package org.foree.imageloader.request;

import android.widget.ImageView;

import org.foree.imageloader.config.ImageLoaderConfig;
import org.foree.imageloader.core.MainImageLoader;

/**
 * Created by foree on 16-9-30.
 * 封装加载图片的请求
 */

public class BitMapRequest implements Comparable{
    private ImageView mImageView;
    private String mUri;
    private int mSerialNum;
    private ImageLoaderConfig mConfig;

    public BitMapRequest(ImageView imageView, String uri){
        mImageView = imageView;
        mUri = uri;
        mConfig = MainImageLoader.getInstance().getConfig();
        mImageView.setTag(mUri);
    }

    public ImageView getImageView() {
        return mImageView;
    }

    public void setSerialNum(int mSerialNum) {
        this.mSerialNum = mSerialNum;
    }

    public int getSerialNum(){
        return mSerialNum;
    }

    public String getImageUri() {
        return mUri;
    }

    @Override
    public int compareTo(Object o) {
        return mConfig.loadPolicy.compare(this, (BitMapRequest)o);
    }
}
