package org.foree.duker.api;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.foree.duker.base.MyApplication;
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssItem;
import org.foree.duker.rssinfo.RssProfile;
import org.foree.duker.utils.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 16-7-25.
 */
public class LocalApiHelper extends AbsApiHelper {
    private static final String API_HOST_URL = "http://cloud.feedly.com";
    private static final String API_TOKEN_TEST = "A3wMXqyNgMOZwCqIoBC5OZoKdSyKemk1IYWp12rk86Kb7KIBHlUBER2Pe2PWaro4Ur_0Rq1h8MiqQBFE_uly7A6GNbjtT5wWbIF5rf6haQetytQcjZj6_FSDSTrkmF3y5CclNtH3q_6UlK1kPPY0i4_CXXIkhIrT7aTJRUTry3b-HGvq_rwWK7JFewguG4PvV7EMozQuosYKOcMrcd3cGwmYsToq8hc:feedlydev";
    private static final String API_CATEGORIES_URL = "/v3/categories";
    private static final String API_SUBSCRIPTIONS_URL = "/v3/subscriptions";
    private static final String API_PROFILE_URL = "/v3/profile";
    private static final String API_STREAM_IDS_URL = "/v3/streams/ids?streamId=:streamId";
    private static final String API_STREAM_CONTENTS_URL = "/v3/streams/contents?streamId=:streamId";
    public static final String USER_ID = "a5a12800-0cc3-4b9e-bc33-9d46f76cc162";
    public static final String API_GLOBAL_ALL_URL = "user/:userId/category/global.all";

    private static final String TAG = LocalApiHelper.class.getSimpleName();

    @Override
    public void getCategoriesList(String token, final NetCallback<List<RssCategory>> netCallback) {
        token = API_TOKEN_TEST;

        String url = API_HOST_URL + API_CATEGORIES_URL;
        String localCategories = "";
        // TODO:重构一波
        final File categories_json = new File(MyApplication.myApplicationDirPath + File.separator + MyApplication.myApplicationDataName + File.separator + "categories.json");

        try {
            localCategories = FileUtils.readFile(categories_json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (localCategories.isEmpty()) {
            final Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "OAuth " + token);
            NetWorkApiHelper.newInstance().getRequest(url, headers, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i(TAG, "onResponse:getCategoriesList " + response);
                    try {
                        FileUtils.writeFile(categories_json, response);
                        if (netCallback != null) {
                            netCallback.onSuccess(parseCategories(response));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "onErrorResponse:getCategoriesList " + error.getMessage());

                    if (netCallback != null) {
                        netCallback.onFail(error.getMessage());
                    }
                }
            });
        } else {
        if (netCallback != null) {
            netCallback.onSuccess(parseCategories(localCategories));
        }
    }

}

    @Override
    public void getSubscriptions(String token, NetCallback<List<RssFeed>> netCallback) {

    }

    @Override
    public void getStream(String token, String streamId, NetCallback<List<RssItem>> netCallback) {

    }

    @Override
    public void getProfile(String token, final NetCallback<RssProfile> netCallback) {

        token = API_TOKEN_TEST;
        String localProfile = "";

        String url = API_HOST_URL + API_PROFILE_URL;


        final File profile_json = new File(MyApplication.myApplicationDirPath + File.separator + MyApplication.myApplicationDataName + File.separator + "profile.json");

        try {
            localProfile = FileUtils.readFile(profile_json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (localProfile.isEmpty()){
            final Map<String,String> headers = new HashMap<>();
            headers.put("Authorization","OAuth " + token);

            NetWorkApiHelper.newInstance().getRequest(url, headers, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i(TAG,"onResponse:getProfile " + response);

                    try {
                        FileUtils.writeFile(profile_json, response);
                        if ( netCallback != null){
                            netCallback.onSuccess(parseProfile(response));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG,"onErrorResponse:getProfile " + error.getMessage());

                }
            });
        } else {
            if ( netCallback != null){
                netCallback.onSuccess(parseProfile(localProfile));
            }
        }

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

}
