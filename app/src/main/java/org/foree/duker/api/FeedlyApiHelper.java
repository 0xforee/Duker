package org.foree.duker.api;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.foree.duker.base.BaseApplication;
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssItem;
import org.foree.duker.rssinfo.RssProfile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 16-7-15.
 * Feedly相关的接口显示
 */
public class FeedlyApiHelper extends AbsApiHelper {
    protected static final String TAG = FeedlyApiHelper.class.getSimpleName();
    protected static final String API_HOST_URL = "http://cloud.feedly.com";
    protected static final String API_TOKEN_TEST = "A3wMXqyNgMOZwCqIoBC5OZoKdSyKemk1IYWp12rk86Kb7KIBHlUBER2Pe2PWaro4Ur_0Rq1h8MiqQBFE_uly7A6GNbjtT5wWbIF5rf6haQetytQcjZj6_FSDSTrkmF3y5CclNtH3q_6UlK1kPPY0i4_CXXIkhIrT7aTJRUTry3b-HGvq_rwWK7JFewguG4PvV7EMozQuosYKOcMrcd3cGwmYsToq8hc:feedlydev";
    protected static final String API_CATEGORIES_URL = "/v3/categories";
    protected static final String API_SUBSCRIPTIONS_URL = "/v3/subscriptions";
    protected static final String API_PROFILE_URL = "/v3/profile";
    protected static final String API_STREAM_IDS_URL = "/v3/streams/ids?streamId=:streamId";
    // TODO: 考虑最高请求数目为10000条时，线程的处理逻辑
    protected static final String API_STREAM_CONTENTS_URL = "/v3/streams/contents?streamId=:streamId";
    protected static final String API_UNREAD_COUNTS_URL = "/v3/markers/counts";
    protected static final String API_MARKERS_URL = "/v3/markers";
    protected static final String USER_ID = "a5a12800-0cc3-4b9e-bc33-9d46f76cc162";
    protected static final String API_GLOBAL_ALL_URL = "user/:userId/category/global.all";

    public String getGlobalAllUrl(){
        return API_GLOBAL_ALL_URL.replace(":userId", USER_ID);
    }

    @Override
    public void getCategoriesList(String token, final NetCallback<List<RssCategory>> netCallback) {
        token = API_TOKEN_TEST;

        String url = API_HOST_URL + API_CATEGORIES_URL;

        final Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","OAuth " + token);
        NetWorkApiHelper.newInstance().getRequest(url, headers, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG,"onResponse:getCategoriesList " + response);
                if (netCallback != null){
                    netCallback.onSuccess(parseCategories(response));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"onErrorResponse:getCategoriesList " + error.getMessage());

                if (netCallback != null){
                    netCallback.onFail(error.getMessage());
                }
            }
        });
    }

    @Override
    public void getSubscriptions(String token, final NetCallback<List<RssFeed>> netCallback) {
        token = API_TOKEN_TEST;

        String url = API_HOST_URL + API_SUBSCRIPTIONS_URL;

        final Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","OAuth " + token);
        NetWorkApiHelper.newInstance().getRequest(url, headers, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG,"onResponse:getSubscriptions " + response);
                if (netCallback != null){
                    netCallback.onSuccess(parseSubscriptions(response));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"onErrorResponse:getSubscriptions " + error.getMessage());

                if (netCallback != null){
                    netCallback.onFail(error.getMessage());
                }
            }
        });
    }

    public void getStream(String token, String streamId, FeedlyApiArgs args, final NetCallback<List<RssItem>> netCallback){
        token = API_TOKEN_TEST;

        String url = API_HOST_URL + API_STREAM_CONTENTS_URL.replace(":streamId", streamId);

        if(args != null) {
            url = args.generateUrl(url);
        }

        final Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","OAuth " + token);
        NetWorkApiHelper.newInstance().getRequest(url, headers, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG,"onResponse:getStream " + response);
                if (netCallback != null){
                    netCallback.onSuccess(parseStream(response));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"onErrorResponse:getStream " + error.getMessage());

                if (netCallback != null){
                    netCallback.onFail(error.getMessage());
                }
            }
        });
    }
    @Override
    public void getProfile(String token, final NetCallback<RssProfile> netCallback) {
        token = API_TOKEN_TEST;

        String url = API_HOST_URL + API_PROFILE_URL;

        final Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","OAuth " + token);
        NetWorkApiHelper.newInstance().getRequest(url, headers, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG,"onResponse:getProfile " + response);
                if (netCallback != null){
                    netCallback.onSuccess(parseProfile(response));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"onErrorResponse:getProfile " + error.getMessage());

                if (netCallback != null){
                    netCallback.onFail(error.getMessage());
                }
            }
        });
    }

    @Override
    public void getUnreadCounts(String token, final NetCallback<Map<String, Long>> netCallback) {
        token = API_TOKEN_TEST;

        String url = API_HOST_URL + API_UNREAD_COUNTS_URL;

        final Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","OAuth " + token);
        NetWorkApiHelper.newInstance().getRequest(url, headers, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG,"onResponse:getUnreadCounts " + response);
                if (netCallback != null){
                    netCallback.onSuccess(parseUnreadCounts(response));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"onErrorResponse:getUnreadCounts " + error.getMessage());

                if (netCallback != null){
                    netCallback.onFail(error.getMessage());
                }
            }
        });
    }

    @Override
    public void markAsOneRead(String token, RssItem rssItem, NetCallback<String> netCallback){
        List<RssItem>  rssItems = new ArrayList<>();
        rssItems.add(rssItem);
        markStream(token, rssItems, netCallback);
    }

    @Override
    public void markStream(String token, final List<RssItem> rssItems, final NetCallback<String> netCallback){
        token = API_TOKEN_TEST;
        String url = API_HOST_URL + API_MARKERS_URL;

        final Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","OAuth " + token);

        // generate JsonObject params
        JSONObject params = generateJsonParams(rssItems);

        NetWorkApiHelper.newInstance().postRequest(url, params, headers, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "setArticleRead: " + rssItems.size() + " StatusCode: " + NetWorkApiHelper.newInstance().getStatusCode());

                if (netCallback != null){
                    netCallback.onSuccess(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "setArticleRead: " + error.getMessage());

                if (netCallback != null){
                    netCallback.onFail(error.getMessage());
                }
            }
        });
    }

    private JSONObject generateJsonParams(List<RssItem> items) {

        JSONArray jsonArray = new JSONArray();
        for(int item_i=0; item_i < items.size(); item_i++) {
            jsonArray.put(items.get(item_i).getEntryId());
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", "markAsRead");
            jsonObject.put("entryIds", jsonArray);
            jsonObject.put("type", "entries");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    protected Map<String, Long> parseUnreadCounts(String data){
        Map<String, Long> unReadCountsMap = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray unreadArray = jsonObject.getJSONArray("unreadcounts");
            for (int unread_i = 0; unread_i < unreadArray.length(); unread_i++) {
                JSONObject id = unreadArray.getJSONObject(unread_i);
                String identifier = id.getString("id");
//                Log.d(TAG, "identifier = " + identifier + " count = " + id.getLong("count"));
                unReadCountsMap.put(identifier, id.getLong("count"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return unReadCountsMap;
    }

    protected RssProfile parseProfile(String data) {

        return new Gson().fromJson(data, RssProfile.class);
    }

    protected List<RssCategory> parseCategories(String data){

        return new Gson().fromJson(data, new TypeToken<List<RssCategory>>(){}.getType());
    }

    protected List<RssFeed> parseSubscriptions(String data){

        return new Gson().fromJson(data, new TypeToken<List<RssFeed>>(){}.getType());
    }

    protected List<RssItem> parseStream(String data){
        List<RssItem> rssItems = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(data);

            if (jsonObject.has("continuation")){
                // write continuation
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("continuation", jsonObject.getString("continuation"));
                editor.apply();
                Log.d(TAG, "write continuation");
            }

            JSONArray jsonArray = jsonObject.getJSONArray("items");
            for(int item_i=0; item_i<jsonArray.length(); item_i++){
                JSONObject itemObject = jsonArray.getJSONObject(item_i);
                RssItem rssItem = new RssItem();

                rssItem.setPublished(itemObject.getLong("published"));
                rssItem.setUrl(itemObject.getJSONArray("alternate").getJSONObject(0).getString("href"));
                // TODO:summary需要更好的过滤
                String summary = itemObject.getJSONObject("summary").getString("content");
                rssItem.setSummary(summary.substring(0, Math.min(32, summary.length())));

                rssItem.setEntryId(itemObject.getString("id"));
                rssItem.setTitle(itemObject.getString("title"));
                rssItem.setFeedId(itemObject.getJSONObject("origin").getString("streamId"));

                List<RssCategory> categoryList = new Gson().fromJson(itemObject.getJSONArray("categories").toString(), new TypeToken<List<RssCategory>>(){}.getType());
                rssItem.setCategories(categoryList);

                rssItem.setUnread(itemObject.getBoolean("unread"));
                rssItems.add(rssItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rssItems;
    }
}
