package org.foree.imageloader.loader;

import android.graphics.Bitmap;

import org.foree.imageloader.request.BitMapRequest;
import org.foree.imageloader.utils.BitmapUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by foree on 16-9-30.
 * 从网络查找图片
 */

public class NetLoader extends AbsLoader {

    @Override
    public Bitmap getBitmap(BitMapRequest request) {
        Bitmap bitmap = null;
        try {
            URL imageUrl = new URL(request.getImageUri());
            HttpURLConnection connection = (HttpURLConnection)imageUrl.openConnection();
            InputStream in = connection.getInputStream();
            bitmap = BitmapUtils.decodeStreamWithReqSize(in, 100,100);
            in.close();
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
