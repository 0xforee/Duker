package org.foree.duker.provider;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import org.foree.duker.ui.fragment.ItemListFragment;

/**
 * Created by foree on 16-10-17.
 */

public class RssObserver extends ContentObserver{
    private static final String TAG = RssObserver.class.getSimpleName();
    public static final Uri PATH_ENTRY = Uri.parse("content://org.foree.duker/entry");

    private Handler mHandler;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public RssObserver(Handler handler) {
        super(handler);
        mHandler = handler;
    }


    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.d(TAG, "data on Change");
        mHandler.sendEmptyMessage(ItemListFragment.MSG_SYNC_START);
    }

}
