package org.foree.duker.api;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssItem;
import org.foree.duker.rssinfo.RssItem;
import org.foree.duker.rssinfo.RssProfile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 16-7-15.
 * Feedly相关的接口显示
 */
public class FeedlyApiHelper extends AbsApiHelper {
    private static final String TAG = FeedlyApiHelper.class.getSimpleName();
    private static final String API_HOST_URL = "http://cloud.feedly.com";
    private static final String API_TOKEN_TEST = "A3wMXqyNgMOZwCqIoBC5OZoKdSyKemk1IYWp12rk86Kb7KIBHlUBER2Pe2PWaro4Ur_0Rq1h8MiqQBFE_uly7A6GNbjtT5wWbIF5rf6haQetytQcjZj6_FSDSTrkmF3y5CclNtH3q_6UlK1kPPY0i4_CXXIkhIrT7aTJRUTry3b-HGvq_rwWK7JFewguG4PvV7EMozQuosYKOcMrcd3cGwmYsToq8hc:feedlydev";
    private static final String API_CATEGORIES_URL = "/v3/categories";
    private static final String API_SUBSCRIPTIONS_URL = "/v3/subscriptions";
    private static final String API_PROFILE_URL = "/v3/profile";
    private static final String API_STREAM_IDS_URL = "/v3/streams/ids?streamId=:streamId";
    private static final String API_STREAM_CONTENTS_URL = "/v3/streams/contents?streamId=:streamId";
    public static final String USER_ID = "a5a12800-0cc3-4b9e-bc33-9d46f76cc162";
    public static final String API_GLOBAL_ALL_URL = "user/:userId/category/global.all";

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

    @Override
    public void getStream(String token, String streamId, final NetCallback<List<RssItem>> netCallback) {
        token = API_TOKEN_TEST;

        String url = API_HOST_URL + API_STREAM_CONTENTS_URL.replace(":streamId", streamId);

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

    private RssProfile parseProfile(String data) {
        RssProfile rssProfile = new RssProfile();
        try {
            JSONObject jsObject = new JSONObject(data);
            rssProfile.setId(jsObject.getString("id"));
            rssProfile.setEmail(jsObject.getString("email"));
            rssProfile.setFullName(jsObject.getString("fullName"));
            rssProfile.setPicture(jsObject.getString("picture"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rssProfile;
    }

    private List<RssCategory> parseCategories(String data){
        List<RssCategory> rssCategories = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(data);
            for( int js_i = 0; js_i < jsonArray.length(); js_i++){
                RssCategory rssCategory = new RssCategory();
                JSONObject jsonObject = jsonArray.getJSONObject(js_i);
                rssCategory.setCategoryId(jsonObject.getString("id"));
                rssCategory.setLable(jsonObject.getString("label"));

                rssCategories.add(rssCategory);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rssCategories;
    }

    private List<RssFeed> parseSubscriptions(String data){
        List<RssFeed> RssFeeds = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(data);
            for( int js_i = 0; js_i < jsonArray.length(); js_i++){
                RssFeed RssFeed = new RssFeed();
                JSONObject jsonObject = jsonArray.getJSONObject(js_i);
                RssFeed.setFeedId(jsonObject.getString("id"));
                RssFeed.setName(jsonObject.getString("title"));
                RssFeed.setUrl(jsonObject.getString("website"));

                JSONArray cateArray = jsonObject.getJSONArray("categories");
                List<String> rssCategoriesId = new ArrayList<>();
                for(int cate_i = 0; cate_i < cateArray.length(); cate_i++){
                    rssCategoriesId.add(cateArray.getJSONObject(cate_i).getString("id"));
                }
                RssFeed.setCategoryIds(rssCategoriesId);

                RssFeeds.add(RssFeed);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return RssFeeds;
    }

    private List<RssItem> parseStream(String data){
        List<RssItem> RssItems = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jsonArray = jsonObject.getJSONArray("items");
            for(int item_i=0; item_i<jsonArray.length(); item_i++){
                JSONObject itemObject = jsonArray.getJSONObject(item_i);
                RssItem RssItem = new RssItem();

                RssItem.setPubDate(new Date(itemObject.getLong("published")));
                RssItem.setUrl(itemObject.getJSONArray("alternate").getJSONObject(0).getString("href"));
                String summary = itemObject.getJSONObject("summary").getString("content");
                RssItem.setSummary(summary.substring(0, Math.min(32, summary.length())));
                RssItem.setEntryId(itemObject.getString("id"));
                RssItem.setTitle(itemObject.getString("title"));

                RssItems.add(RssItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return RssItems;
    }
}
