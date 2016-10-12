package org.foree.imageloader.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.jakewharton.disklrucache.DiskLruCache;

import org.foree.imageloader.base.AppInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by foree on 16-10-10.
 */

public class DiskCache extends BitmapCache {

    private DiskLruCache mDiskLruCache;

    private static DiskCache mInstance;

    private DiskCache(){
        initDiskCache();
    }

    public static DiskCache getInstance(){
        if( mInstance == null ){
            synchronized (MemoryCache.class){
                if( mInstance == null){
                    mInstance = new DiskCache();
                }
            }
        }

        return mInstance;
    }

    private void initDiskCache() {
        File cacheFile = new File(AppInfo.myCacheDir);
        try {
            mDiskLruCache = DiskLruCache.open(cacheFile, AppInfo.myVersionCode, 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Bitmap get(String imageUrl) {
        Bitmap bitmap = null;
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(generateKeyFromUrl(imageUrl));
            if( snapshot != null){
                InputStream in = snapshot.getInputStream(0);
                bitmap = BitmapFactory.decodeStream(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    public void put(String imageUrl, Bitmap bitmap) {
        String cacheKey = generateKeyFromUrl(imageUrl);

        if (get(imageUrl) == null) {
            try {
                DiskLruCache.Editor editor = mDiskLruCache.edit(cacheKey);
                if (editor != null) {
                    OutputStream out = editor.newOutputStream(0);
                    if (bitmap != null) {
                        out.write(bitmapToBytes(bitmap));
                        editor.commit();
                    } else {
                        editor.abort();
                    }
                    mDiskLruCache.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String generateKeyFromUrl(String imageUrl){
        // generate md5 from url
        String cacheKey;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(imageUrl.getBytes());
            cacheKey = bytesToHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(imageUrl.hashCode());
        }

        return cacheKey;

    }

    @NonNull
    private String bytesToHexString(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < digest.length; i++){
            String hex = Integer.toHexString(0xFF & digest[i]);
            if( hex.length() == 1){
                sb.append('0');
            }
            sb.append(hex);
        }

        return sb.toString();
    }

    private byte[] bitmapToBytes(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
