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
import org.foree.duker.utils.FeedlyApiUtils;
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
    protected static final String API_TOKEN_TEST = "AxFkyq6s7oql8gp5w04UzQrD-NEfWgkm4Ue2WDEVaYKNx8QJgxQCSmdqemDNNfJLL9JfCSlxPnSmsOg8r9xMxYMnM0ooXwwtpocMOYDHLGL8HR3dwnqJU1pdURuNx3vCJXtnhYX5I_AWlmiiBCgwrPFsKEkWBzDcKfI2f52y8qgKCLN2K-Thg1MfG5pVsutQuzgPpmGdBdkvI8SoSy46X-69YksE:feedlydev";

    @Override
    public void getCategoriesList(String token, final NetCallback<List<RssCategory>> netCallback) {
        token = API_TOKEN_TEST;

        final Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","OAuth " + token);
        NetWorkApiHelper.newInstance().getRequest(FeedlyApiUtils.getApiCategoriesUrl(), headers, new Response.Listener<String>() {
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

        final Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","OAuth " + token);
        NetWorkApiHelper.newInstance().getRequest(FeedlyApiUtils.getApiSubscriptionsUrl(), headers, new Response.Listener<String>() {
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

        String url = FeedlyApiUtils.getApiStreamContentsUrl(streamId);

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
    public void getStreamGlobalAll(String token, FeedlyApiArgs args, NetCallback<List<RssItem>> netCallback){
        getStream(token, FeedlyApiUtils.getApiGlobalAllUrl(), args, netCallback);
    }

    @Override
    public void getProfile(String token, final NetCallback<RssProfile> netCallback) {
        token = API_TOKEN_TEST;

        final Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","OAuth " + token);
        NetWorkApiHelper.newInstance().getRequest(FeedlyApiUtils.getApiProfileUrl(), headers, new Response.Listener<String>() {
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

        final Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","OAuth " + token);
        NetWorkApiHelper.newInstance().getRequest(FeedlyApiUtils.getApiUnreadCountsUrl(), headers, new Response.Listener<String>() {
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

        final Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","OAuth " + token);

        // generate JsonObject params
        JSONObject params = generateJsonParams(rssItems);

        NetWorkApiHelper.newInstance().postRequest(FeedlyApiUtils.getApiMarkersUrl(), params, headers, new Response.Listener<String>() {
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
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());

            // write continuation
            if (jsonObject.has("continuation")){
                sp.edit().putString("continuation", jsonObject.getString("continuation")).apply();
                Log.d(TAG, "write continuation");
            }else{
                sp.edit().putString("continuation", "").apply();
            }

            // write updated
            sp.edit().putLong("updated", jsonObject.getLong("updated")).apply();

            // write items
            JSONArray jsonArray = jsonObject.getJSONArray("items");
            for(int item_i=0; item_i<jsonArray.length(); item_i++){
                JSONObject itemObject = jsonArray.getJSONObject(item_i);
                RssItem rssItem = new RssItem();

                rssItem.setPublished(itemObject.getLong("published"));
                rssItem.setUrl(itemObject.getJSONArray("alternate").getJSONObject(0).getString("href"));
                // TODO:summary需要更好的过滤
                String summary = itemObject.getJSONObject("summary").getString("content");
                rssItem.setSummary(summary);

                // visual
                if( itemObject.getJSONObject("visual").getString("url") != null) {
                    String visual = itemObject.getJSONObject("visual").getString("url");
                    rssItem.setVisual(visual);
                }

                rssItem.setEntryId(itemObject.getString("id"));
                rssItem.setTitle(itemObject.getString("title"));
                rssItem.setFeedId(itemObject.getJSONObject("origin").getString("streamId"));
                rssItem.setFeedName(itemObject.getJSONObject("origin").getString("title"));

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
