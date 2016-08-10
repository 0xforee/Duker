package org.foree.duker.api;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.foree.duker.base.BaseApplication;
import org.foree.duker.base.MyApplication;
import org.foree.duker.dao.RssDao;
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssItem;
import org.foree.duker.rssinfo.RssProfile;
import org.foree.duker.utils.FeedlyApiUtils;
import org.foree.duker.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 16-7-25.
 * 缓存操作类，包括数据库与本地文件缓存
 */
public class LocalApiHelper extends FeedlyApiHelper {

    private static final String TAG = LocalApiHelper.class.getSimpleName();

    @Override
    public void getCategoriesList(String token, final NetCallback<List<RssCategory>> netCallback) {
        token = API_TOKEN_TEST;

        String localCategories = "";

        final File categories_json = new File(MyApplication.myApplicationDirPath +
                File.separator + MyApplication.myApplicationDataName + File.separator + "categories.json");

        try {
            localCategories = FileUtils.readFile(categories_json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (localCategories.isEmpty()) {
            final Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "OAuth " + token);
            NetWorkApiHelper.newInstance().getRequest(FeedlyApiUtils.getApiCategoriesUrl(), headers, new Response.Listener<String>() {
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
        } else if (netCallback != null) {
            netCallback.onSuccess(parseCategories(localCategories));
        }

    }
    @Override
    public void getProfile(String token, final NetCallback<RssProfile> netCallback) {

        token = API_TOKEN_TEST;
        String localProfile = "";

        final File profile_json = new File(MyApplication.myApplicationDirPath + File.separator + MyApplication.myApplicationDataName + File.separator + "profile.json");

        try {
            localProfile = FileUtils.readFile(profile_json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (localProfile.isEmpty()){
            final Map<String,String> headers = new HashMap<>();
            headers.put("Authorization","OAuth " + token);

            NetWorkApiHelper.newInstance().getRequest(FeedlyApiUtils.getApiProfileUrl(), headers, new Response.Listener<String>() {
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
        } else if ( netCallback != null){
                netCallback.onSuccess(parseProfile(localProfile));
        }

    }

    @Override
    public void getSubscriptions(String token, final NetCallback<List<RssFeed>> netCallback) {
        token = API_TOKEN_TEST;
        String localSubscriptions = "";
        final File subscriptions_json = new File(MyApplication.myApplicationDirPath + File.separator + MyApplication.myApplicationDataName + File.separator + "subscriptions.json");

        try {
            localSubscriptions = FileUtils.readFile(subscriptions_json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (localSubscriptions.isEmpty()){
        final Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","OAuth " + token);
        NetWorkApiHelper.newInstance().getRequest(FeedlyApiUtils.getApiSubscriptionsUrl(), headers, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG,"onResponse:getSubscriptions " + response);
                try {
                    FileUtils.writeFile(subscriptions_json, response);
                    if ( netCallback != null){
                        netCallback.onSuccess(parseSubscriptions(response));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
        } else if ( netCallback != null){
            netCallback.onSuccess(parseSubscriptions(localSubscriptions));
        }
    }

    @Override
    public void getStream(String token, String streamId, FeedlyApiArgs args, final NetCallback<List<RssItem>> netCallback) {

        // only get data from db
        final RssDao rssDao = new RssDao(BaseApplication.getInstance().getApplicationContext());
        List<RssItem> rssItemList = rssDao.find(streamId, true);

        if( netCallback != null) {
            if (!rssItemList.isEmpty()) {
                netCallback.onSuccess(rssItemList);
            } else {
                netCallback.onFail("rssItemList null");
            }
        }
    }

}
