package org.foree.duker.ui.presenter;

import android.util.Log;

import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.foree.duker.api.AbsApiFactory;
import org.foree.duker.api.AbsApiHelper;
import org.foree.duker.api.ApiFactory;
import org.foree.duker.api.LocalApiHelper;
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssProfile;
import org.foree.duker.ui.view.IMainView;

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

    }
}
