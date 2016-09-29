package org.foree.duker.ui.view;

import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssProfile;

import java.util.List;
import java.util.Map;

/**
 * Created by foree on 16-9-29.
 */

public interface IMainView {
    void updateProfile(RssProfile data);
    void updateSubscriptions(Map<RssCategory, List<RssFeed>> data);
    void updateUnreadCounts(Map<String, Long> data);
}
