package org.foree.duker.provider;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * Created by foree on 16-10-17.
 */

public class RssObserver extends ContentObserver{

    private static final String TAG = RssObserver.class.getSimpleName();

    private static final String URI_AUTHOR = "content://org.foree.duker";
    public static final Uri URI_ENTRY = Uri.parse(URI_AUTHOR + "/entry");
    public static final Uri URI_PROFILE = Uri.parse(URI_AUTHOR + "/profile");
    public static final Uri URI_CATEGORY = Uri.parse(URI_AUTHOR + "/category");
    public static final Uri URI_FEED = Uri.parse(URI_AUTHOR + "/feed");
    public static final Uri URI_SUB_CATE = Uri.parse(URI_AUTHOR + "/feed_category");

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public RssObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        Log.d(TAG, "uri = " + uri.toString() + " changed was observed");
    }
}
