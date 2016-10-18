package org.foree.duker.provider;

import android.net.Uri;
import android.os.Handler;

import org.foree.duker.ui.fragment.ItemListFragment;

/**
 * Created by foree on 16-10-18.
 */

public class ItemListObserver extends RssObserver {
    private static final String TAG = ItemListObserver.class.getSimpleName();

    private Handler mHandler;

    public ItemListObserver(Handler handler) {
        super(handler);
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        mHandler.sendEmptyMessage(ItemListFragment.MSG_SYNC_START);
    }
}
