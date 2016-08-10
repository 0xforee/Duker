package org.foree.duker.utils;

/**
 * Created by foree on 16-8-10.
 * Feedly API 工具类
 */
public class FeedlyApiUtils {
    private static final String API_HOST_URL = "http://cloud.feedly.com";
    private static final String API_CATEGORIES_URL = "/v3/categories";
    private static final String API_SUBSCRIPTIONS_URL = "/v3/subscriptions";
    private static final String API_PROFILE_URL = "/v3/profile";
    private static final String API_UNREAD_COUNTS_URL = "/v3/markers/counts";
    private static final String API_STREAM_IDS_URL = "/v3/streams/ids?streamId=:streamId";
    private static final String API_STREAM_CONTENTS_URL = "/v3/streams/contents?streamId=:streamId";
    private static final String API_MARKERS_URL = "/v3/markers";
    private static final String API_GLOBAL_ALL_URL = "user/:userId/category/global.all";
    private static final String USER_ID = "a5a12800-0cc3-4b9e-bc33-9d46f76cc162";

    public static String getApiCategoriesUrl(){
        return API_HOST_URL + API_CATEGORIES_URL;
    }

    public static String getApiSubscriptionsUrl(){
        return API_HOST_URL + API_SUBSCRIPTIONS_URL;
    }

    public static String getApiProfileUrl(){
        return API_HOST_URL + API_PROFILE_URL;
    }

    public static String getApiUnreadCountsUrl(){
        return API_HOST_URL + API_UNREAD_COUNTS_URL;
    }

    public static String getApiStreamContentsUrl(String streamId){
        return API_HOST_URL + API_STREAM_CONTENTS_URL.replace(":streamId", streamId);
    }

    public static String getApiMarkersUrl(){
        return API_HOST_URL + API_MARKERS_URL;
    }

    public static String getApiGlobalAllUrl(){
        return API_GLOBAL_ALL_URL.replace(":userId", USER_ID);
    }
}
