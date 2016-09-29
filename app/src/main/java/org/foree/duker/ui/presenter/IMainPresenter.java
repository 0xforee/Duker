package org.foree.duker.ui.presenter;

/**
 * Created by foree on 16-9-29.
 * MVP中P,用于与数据交互
 */

public interface IMainPresenter {
    void getSubscriptions();
    void getProfile();
    void getUnreadCounts();

}
