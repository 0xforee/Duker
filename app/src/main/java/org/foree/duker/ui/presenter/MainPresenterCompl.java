package org.foree.duker.ui.presenter;

import android.util.Log;

import org.foree.duker.api.AbsApiFactory;
import org.foree.duker.api.AbsApiHelper;
import org.foree.duker.api.ApiFactory;
import org.foree.duker.api.LocalApiHelper;
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssProfile;
import org.foree.duker.ui.view.IMainView;

import java.util.List;
import java.util.Map;

/**
 * Created by foree on 16-9-29.
 */

public class MainPresenterCompl implements IMainPresenter {

    private static final String TAG = MainPresenterCompl.class.getSimpleName();
    private IMainView mainView;
    private AbsApiHelper localApiHelper;

    public MainPresenterCompl(IMainView view){
        mainView = view;
        AbsApiFactory absApiFactory = new ApiFactory();
        localApiHelper = absApiFactory.createApiHelper(LocalApiHelper.class);
    }

    @Override
    public void getSubscriptions() {
        localApiHelper.getFeedCate("", new NetCallback<Map<RssCategory, List<RssFeed>>>() {
            @Override
            public void onSuccess(final Map<RssCategory, List<RssFeed>> data) {
                mainView.updateSubscriptions(data);
            }

            @Override
            public void onFail(String msg) {
                Log.e(TAG,"getSubscription " + msg);
            }
        });
    }

    @Override
    public void getProfile() {
        // getProfile from db
        localApiHelper.getProfile("", new NetCallback<RssProfile>() {
            @Override
            public void onSuccess(RssProfile data) {
                mainView.updateProfile(data);
            }
            @Override
            public void onFail(String msg) {
                Log.e(TAG, "updateProfile:" + msg);
            }
        });
    }

    @Override
    public void getUnreadCounts() {
        localApiHelper.getUnreadCounts("", new NetCallback<Map<String, Long>>() {
            @Override
            public void onSuccess(Map<String, Long> unReadCountsMap) {
                mainView.updateUnreadCounts(unReadCountsMap);
            }
            @Override
            public void onFail(String msg) {
                Log.e(TAG,"getUnreadCounts " + msg);
            }
        });
    }
}
