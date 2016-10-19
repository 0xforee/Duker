package org.foree.duker.provider;

import android.net.Uri;
import android.os.Handler;

import org.foree.duker.ui.activity.MainActivity;

/**
 * Created by foree on 16-10-18.
 */

public class MainObserver extends RssObserver {
    private Handler mHandler;
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public MainObserver(Handler handler) {
        super(handler);
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        if( uri.equals(RssObserver.URI_CATEGORY)){

        }else if( uri.equals(RssObserver.URI_ENTRY)){

        }else if( uri.equals(RssObserver.URI_FEED)){

        }else if( uri.equals(RssObserver.URI_PROFILE)){
            mHandler.sendEmptyMessage(MainActivity.MSG_UPDATE_PROFILE);
        }else if( uri.equals(RssObserver.URI_SUB_CATE)){

        }
    }
}
