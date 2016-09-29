package org.foree.duker.ui.view;

import org.foree.duker.rssinfo.RssProfile;

/**
 * Created by foree on 16-9-29.
 */

public interface IMainView {
    void updateProfile(RssProfile rssProfile);
    void updateSubscriptions();
    void updateUnreadCounts();
}
