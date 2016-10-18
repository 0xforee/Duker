package org.foree.duker.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.foree.duker.provider.RssObserver;
import org.foree.duker.rssinfo.RssCategory;
import org.foree.duker.rssinfo.RssFeed;
import org.foree.duker.rssinfo.RssItem;
import org.foree.duker.rssinfo.RssProfile;
import org.foree.duker.utils.FeedlyApiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by foree on 2016/8/6.
 * 数据库操作方法
 * TODO:性能优化，批量操作不使用循环
 */
public class RssDaoHelper {
    private static final String TAG = RssDaoHelper.class.getSimpleName();
    private RssSQLiteOpenHelper rssSQLiteOpenHelper;
    private ContentResolver mContentResolver;

    public RssDaoHelper(Context context){
        rssSQLiteOpenHelper = new RssSQLiteOpenHelper(context);
        mContentResolver = context.getContentResolver();
    }

    public void insertEntryData(List<RssItem> itemList){
        ContentValues[] contentValuesArray = new ContentValues[itemList.size()];
        for (int i=0; i < itemList.size(); i++) {
            ContentValues contentValues = new ContentValues();

            contentValues.put("entry_id", itemList.get(i).getEntryId());
            contentValues.put("feed_id", itemList.get(i).getFeedId());
            contentValues.put("feed_name", itemList.get(i).getFeedName());
            contentValues.put("unread", itemList.get(i).isUnread());
            contentValues.put("visual", itemList.get(i).getVisual());
            contentValues.put("content", itemList.get(i).getContent());
            contentValues.put("url", itemList.get(i).getUrl());
            contentValues.put("published", itemList.get(i).getPublished());
            contentValues.put("title", itemList.get(i).getTitle());

            contentValuesArray[i] = contentValues;
        }

        mContentResolver.bulkInsert(RssObserver.URI_ENTRY, contentValuesArray);
    }

    public void insertProfile(RssProfile profile) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("user_id", profile.getUserId());
        contentValues.put("locale", profile.getLocale());
        contentValues.put("gender", profile.getGender());
        contentValues.put("given_name", profile.getGivenName());
        contentValues.put("family_name", profile.getFamilyName());
        contentValues.put("full_name", profile.getFullName());
        contentValues.put("picture", profile.getPicture());
        contentValues.put("email", profile.getEmail());

        mContentResolver.insert(RssObserver.URI_PROFILE, contentValues);
    }

    public void insertCategory(List<RssCategory> categoryList) {
        ContentValues[] contentValuesArray = new ContentValues[categoryList.size()];
        for (int i=0; i < categoryList.size(); i++) {
            ContentValues contentValues = new ContentValues();

            contentValues.put("category_id", categoryList.get(i).getCategoryId());
            contentValues.put("label", categoryList.get(i).getLabel());
            contentValues.put("description", categoryList.get(i).getDescription());

            contentValuesArray[i] = contentValues;
        }

        mContentResolver.bulkInsert(RssObserver.URI_CATEGORY, contentValuesArray);
    }

    /**
     * 获取rssFeedList，插入feed与feed_category表
     * @param rssFeedList
     */
    public void insertFeedAndSubCate(List<RssFeed> rssFeedList) {

        ContentValues contentValues = new ContentValues();
        for (RssFeed rssFeed : rssFeedList) {
            // insert subscription table
            contentValues.put("feed_id", rssFeed.getFeedId());
            contentValues.put("title", rssFeed.getName());
            contentValues.put("website", rssFeed.getUrl());
            //contentValues.put("icon_url", rssFeed.get());
            mContentResolver.insert(RssObserver.URI_FEED, contentValues);

            // insert sub_cate table
            for(RssCategory rssCategory: rssFeed.getCategories()) {
                ContentValues cateValues = new ContentValues();
                cateValues.put("feed_id", rssFeed.getFeedId());
                cateValues.put("category_id", rssCategory.getCategoryId());

                // 因为主键为自然增长，所以需要排除可能有多个同样的feed_id,cate_id重复出现的情况
                Cursor cursor = mContentResolver.query(RssObserver.URI_SUB_CATE, null,
                        "feed_id=? AND category_id=?", new String[]{rssFeed.getFeedId(), rssCategory.getCategoryId()}, null);
                if( cursor != null && cursor.getCount() == 0){
                    mContentResolver.insert(RssObserver.URI_SUB_CATE, cateValues);
                    cursor.close();
                }
            }
        }

    }

    /**
     * 根据feedId, unread状态来查询文章
     * @return 符合要求的rssItemList
     */
    public List<RssItem> findUnreadEntriesByFeedId(String feedId, boolean unread) {
        Log.d(TAG, "get feedId = " + feedId + " unread = " + unread + " rssItems from db");
        String selection;
        String[] selectionArgs;
        List<RssItem> rssItemList = new ArrayList<>();
        if (FeedlyApiUtils.isGlobalAllUrl(feedId)) {
            selection = "unread=?";
            selectionArgs = new String[]{unread ? "1" : "0"};
        } else {
            selection = "feed_id=? AND unread=?";
            selectionArgs = new String[]{feedId, unread ? "1" : "0"};
        }

        Cursor cursor = mContentResolver.query(RssObserver.URI_ENTRY, null, selection, selectionArgs, "published DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex("entry_id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String url = cursor.getString(cursor.getColumnIndex("url"));
                boolean local_unread = cursor.getInt(cursor.getColumnIndex("unread")) > 0;
                long published = cursor.getLong(cursor.getColumnIndex("published"));
                String feedName = cursor.getString(cursor.getColumnIndex("feed_name"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                String visual = cursor.getString(cursor.getColumnIndex("visual"));
                RssItem rssItem = new RssItem(id, title, url, feedName, content, visual, local_unread, published);

                rssItemList.add(rssItem);

            }
            cursor.close();
        }

        return rssItemList;
    }

    /**
     * 读取profile表中内容
     */
    public RssProfile readProfile() {
        RssProfile rssProfile = null;
        Cursor cursor = mContentResolver.query(RssObserver.URI_PROFILE, null,null,null,null);
        if( cursor != null) {
            while (cursor.moveToNext()) {
                String userId = cursor.getString(cursor.getColumnIndex("user_id"));
                String locale = cursor.getString(cursor.getColumnIndex("locale"));
                String gender = cursor.getString(cursor.getColumnIndex("gender"));
                String givenName = cursor.getString(cursor.getColumnIndex("given_name"));
                String familyName = cursor.getString(cursor.getColumnIndex("family_name"));
                String fullName = cursor.getString(cursor.getColumnIndex("full_name"));
                String picture = cursor.getString(cursor.getColumnIndex("picture"));
                String email = cursor.getString(cursor.getColumnIndex("email"));
                rssProfile = new RssProfile(locale, gender, givenName, familyName,
                        fullName, userId, picture, email);
            }
            cursor.close();
        }

        return rssProfile;
    }

    /**
     * 读取分类数据
     * @return rssCategory与rssFeedList所构建的map
     * 用于构建DrawerLayout的侧边栏分类
     */
    public Map<RssCategory, List<RssFeed>> readFeedCate(){
        Map<RssCategory, List<RssFeed>> feedCateMap = new HashMap<>();

        List<RssCategory> rssCategories = readCategory();
        if ( rssCategories != null ){
            for(RssCategory rssCategory: rssCategories){
                feedCateMap.put(rssCategory, readFeedsByCategoryId(rssCategory.getCategoryId()));
            }
        }

        return feedCateMap;
    }

    /**
     * 读取分类数据表
     */
    public List<RssCategory> readCategory() {

        List<RssCategory> rssCategories = new ArrayList<>();
        Cursor cursor = mContentResolver.query(RssObserver.URI_CATEGORY, null,null,null,null);
        if(cursor != null) {
            while (cursor.moveToNext()) {
                String categoryId = cursor.getString(cursor.getColumnIndex("category_id"));
                String label = cursor.getString(cursor.getColumnIndex("label"));
                String description = cursor.getString(cursor.getColumnIndex("description"));

                RssCategory rssCategory = new RssCategory(categoryId, label, description);
                rssCategories.add(rssCategory);
            }

            cursor.close();
        }

        return rssCategories;
    }

    /**
     * 从feed_category表中根据category_id读取分类下的feedId，
     * 然后根据feedId查询feed库，返回rssFeed
     */
    private List<RssFeed> readFeedsByCategoryId(String categoryId) {

        List<RssFeed> rssFeeds = new ArrayList<>();
        Cursor cursor = mContentResolver.query(RssObserver.URI_SUB_CATE, null, "category_id=?", new String[]{categoryId}, null);

        if( cursor != null) {
            while (cursor.moveToNext()) {
                String feedId = cursor.getString(cursor.getColumnIndex("feed_id"));

                // get rssFeed from table feed
                Cursor cursor_feed = mContentResolver.query(RssObserver.URI_FEED, null, "feed_id=?", new String[]{feedId}, null);

                if ( cursor_feed != null) {
                    while (cursor_feed.moveToNext()) {
                        String title = cursor_feed.getString(cursor_feed.getColumnIndex("title"));
                        String website = cursor_feed.getString(cursor_feed.getColumnIndex("website"));
                        RssFeed rssFeed = new RssFeed(feedId, title, website);
                        rssFeeds.add(rssFeed);
                    }

                    cursor_feed.close();
                }
            }

            cursor.close();
        }

        return rssFeeds;
    }

    private List<String> getFeedIdList(){
        List<String> feedIdList = new ArrayList<>();

        // get global all
        feedIdList.add(FeedlyApiUtils.getApiGlobalAllUrl());

        // get category feeds
        Cursor cursor = mContentResolver.query(RssObserver.URI_FEED,new String[]{ "feed_id"}, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String feed_id = cursor.getString(cursor.getColumnIndex("feed_id"));
                feedIdList.add(feed_id);
            }

            cursor.close();
        }

        return feedIdList;
    }

    /**
     * 获取本地各个分类未读的文章数量
     * unreadCountsMap
     * id - counts
     */
    public Map<String, Long> getUnreadCounts(){
        Map<String, Long> unReadCountsMap = new HashMap<>();
        String selection;
        String[] selectionArgs;

        // do calculate unread counts
        for(String feedId: getFeedIdList()) {

            if (FeedlyApiUtils.isGlobalAllUrl(feedId)) {
                selection = "unread=?";
                selectionArgs = new String[]{"1"};
            } else {
                selection = "feed_id=? AND unread=?";
                selectionArgs = new String[]{feedId, "1"};
            }

            // get all feeds
            Cursor cursor = mContentResolver.query(RssObserver.URI_ENTRY, null, selection, selectionArgs, null);

            if( cursor != null ){
                unReadCountsMap.put(feedId, (long)cursor.getCount());
                cursor.close();
            }

            // get all counts
            // get feed_id and unread is true counts
        }

        return unReadCountsMap;
    }

    /**
     * 清空表
     * @param tableUri 表名称
     */
    public void cleanTable(Uri tableUri){
        mContentResolver.delete(tableUri, null,null);
    }

    /**
     * 更新rssItem的unread字段
     */
    public int updateUnreadByEntryId(String id, boolean newValue){

        ContentValues contentValues = new ContentValues();
        contentValues.put("unread", newValue);

        return mContentResolver.update(RssObserver.URI_ENTRY, contentValues, "entry_id=?", new String[]{id});
    }

    /**
     * 删除某一项
     * @param id rssItem的标示
     */
    public int delete(String id){
        return mContentResolver.delete(RssObserver.URI_ENTRY, "entry_id=?", new String[]{id});
    }

    /**
     * 批量删除
     * @param itemList item列表
     */
    public int deleteEntries(List<RssItem> itemList){
        int result = 0;
        for(RssItem item: itemList) {
            result = delete(item.getEntryId());
        }
        return result;

    }
}
