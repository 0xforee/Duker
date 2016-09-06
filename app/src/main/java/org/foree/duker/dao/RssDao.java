package org.foree.duker.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
public class RssDao {
    private static final String TAG = RssDao.class.getSimpleName();
    private RssSQLiteOpenHelper rssSQLiteOpenHelper;

    public RssDao(Context context){
        rssSQLiteOpenHelper = new RssSQLiteOpenHelper(context);
    }

    public void insertEntries(List<RssItem> itemList){
        synchronized (this) {
            int tmp = 1;
            Log.d(TAG, "insert " + itemList.size() + " entries to db");
            // 拆分itemList，dataBase 一次事务只能插入1000条数据
            while(itemList.size()>(1000*tmp)){
                insertEntryInternal(itemList.subList(1000*(tmp-1),1000*tmp));
                tmp++;
            }
            insertEntryInternal(itemList.subList(1000*(tmp-1), itemList.size()));
        }
    }

    private void insertEntryInternal(List<RssItem> subItemList) {
        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();
        for (RssItem item : subItemList) {
            contentValues.put("entry_id", item.getEntryId());
            contentValues.put("feed_id", item.getFeedId());
            contentValues.put("feed_name", item.getFeedName());
            contentValues.put("unread", item.isUnread());
            contentValues.put("visual", item.getVisual());
            contentValues.put("content", item.getContent());
            contentValues.put("url", item.getUrl());
            contentValues.put("published", item.getPublished());
            contentValues.put("title", item.getTitle());
            // 内容不重复
            if (db.insertWithOnConflict(RssSQLiteOpenHelper.DB_TABLE_ENTRY, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
                Log.e(TAG, "Database insertEntries id: " + item.getEntryId() + " error");
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void insertProfile(RssProfile profile) {
        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();
        contentValues.put("user_id", profile.getUserId());
        contentValues.put("locale", profile.getLocale());
        contentValues.put("gender", profile.getGender());
        contentValues.put("given_name", profile.getGivenName());
        contentValues.put("family_name", profile.getFamilyName());
        contentValues.put("full_name", profile.getFullName());
        contentValues.put("picture", profile.getPicture());
        contentValues.put("email", profile.getEmail());

        // 内容不重复
        if (db.insertWithOnConflict(RssSQLiteOpenHelper.DB_TABLE_PROFILE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
            Log.e(TAG, "Database insertUserProfile id: " + profile.getUserId() + " error");
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void insertCategory(List<RssCategory> categoryList) {
        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();
        for (RssCategory category : categoryList) {
            contentValues.put("category_id", category.getCategoryId());
            contentValues.put("label", category.getLabel());
            contentValues.put("description", category.getDescription());
            // 内容不重复
            if (db.insertWithOnConflict(RssSQLiteOpenHelper.DB_TABLE_CATEGORY, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
                Log.e(TAG, "Database insertCategory id: " + category.getLabel() + " error");
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    /**
     * 获取rssFeedList，插入feed与feed_category表
     * @param rssFeedList
     */
    public void insertFeedAndSubCate(List<RssFeed> rssFeedList) {
        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();
        for (RssFeed rssFeed : rssFeedList) {
            // insert subscription table
            contentValues.put("feed_id", rssFeed.getFeedId());
            contentValues.put("title", rssFeed.getName());
            contentValues.put("website", rssFeed.getUrl());
            //contentValues.put("icon_url", rssFeed.get());
            // 内容不重复
            if (db.insertWithOnConflict(RssSQLiteOpenHelper.DB_TABLE_FEED, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
                Log.e(TAG, "Database insertFeed id: " + rssFeed.getFeedId() + " error");
            }

            // insert sub_cate table
            for(RssCategory rssCategory: rssFeed.getCategories()) {
                ContentValues cateValues = new ContentValues();
                cateValues.put("feed_id", rssFeed.getFeedId());
                cateValues.put("category_id", rssCategory.getCategoryId());

                // 因为主键为自然增长，所以需要排除可能有多个同样的feed_id,cate_id重复出现的情况
                Cursor cursor = db.query(RssSQLiteOpenHelper.DB_TABLE_SUB_CATE, null,
                        "feed_id=? AND category_id=?", new String[]{rssFeed.getFeedId(), rssCategory.getCategoryId()}, null, null, null);
                if (cursor.getCount() == 0) {
                    if (db.insertWithOnConflict(RssSQLiteOpenHelper.DB_TABLE_SUB_CATE, null, cateValues, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
                        Log.e(TAG, "Database insertFeed_Category id: " + rssCategory.getCategoryId() + " error");
                    }
                }
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
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
        SQLiteDatabase db = rssSQLiteOpenHelper.getReadableDatabase();
        if (FeedlyApiUtils.isGlobalAllUrl(feedId)) {
            selection = "unread=?";
            selectionArgs = new String[]{unread ? "1" : "0"};
        } else {
            selection = "feed_id=? AND unread=?";
            selectionArgs = new String[]{feedId, unread ? "1" : "0"};
        }
        Cursor cursor = db.query(RssSQLiteOpenHelper.DB_TABLE_ENTRY, new String[]{"entry_id,title,url,published,unread,feed_name,content,visual"},
                selection, selectionArgs, null, null, "published DESC");
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
        db.close();
        return rssItemList;
    }

    /**
     * 读取profile表中内容
     */
    public RssProfile readProfile() {
        SQLiteDatabase db = rssSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();
        RssProfile rssProfile = null;
        Cursor cursor = db.query(RssSQLiteOpenHelper.DB_TABLE_PROFILE, null,
                null, null, null, null, null);
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
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

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
        SQLiteDatabase db = rssSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();
        List<RssCategory> rssCategories = new ArrayList<>();
        Cursor cursor = db.query(RssSQLiteOpenHelper.DB_TABLE_CATEGORY, null,
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            String categoryId = cursor.getString(cursor.getColumnIndex("category_id"));
            String label = cursor.getString(cursor.getColumnIndex("label"));
            String description = cursor.getString(cursor.getColumnIndex("description"));

            RssCategory rssCategory = new RssCategory(categoryId, label, description);
            rssCategories.add(rssCategory);
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return rssCategories;
    }

    /**
     * 从feed_category表中根据category_id读取分类下的feedId，
     * 然后根据feedId查询feed库，返回rssFeed
     */
    private List<RssFeed> readFeedsByCategoryId(String categoryId) {
        SQLiteDatabase db = rssSQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();
        List<RssFeed> rssFeeds = new ArrayList<>();
        Cursor cursor = db.query(RssSQLiteOpenHelper.DB_TABLE_SUB_CATE, null,
                "category_id=?", new String[]{categoryId}, null, null, null);
        while (cursor.moveToNext()) {
            String feedId = cursor.getString(cursor.getColumnIndex("feed_id"));

            // get rssFeed from table feed
            Cursor cursor_feed = db.query(RssSQLiteOpenHelper.DB_TABLE_FEED, null,
                    "feed_id=?", new String[]{feedId}, null, null, null);
            while (cursor_feed.moveToNext()) {
                String title = cursor_feed.getString(cursor_feed.getColumnIndex("title"));
                String website = cursor_feed.getString(cursor_feed.getColumnIndex("website"));
                RssFeed rssFeed = new RssFeed(feedId, title, website);
                rssFeeds.add(rssFeed);
            }

            cursor_feed.close();
        }

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return rssFeeds;
    }

    /**
     * 清空表
     * @param table 表名称
     */
    public void cleanTable(String table){
        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        db.delete(table, null, null);
        db.close();
    }

    /**
     * 更新rssItem的unread字段
     */
    public int updateUnreadByEntryId(String id, boolean newValue){

        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("unread", newValue);

        int result = db.update(RssSQLiteOpenHelper.DB_TABLE_ENTRY, contentValues, "entry_id=?", new String[]{id});

        db.close();

        return result;
    }

    /**
     * 删除某一项
     * @param id rssItem的标示
     */
    public int delete(String id){

        SQLiteDatabase db = rssSQLiteOpenHelper.getReadableDatabase();
        int result = db.delete(RssSQLiteOpenHelper.DB_TABLE_ENTRY, "entry_id=?", new String[]{id});
        db.close();
        return result;
    }

    /**
     * 批量删除
     * @param itemList item列表
     */
    public int deleteEntries(List<RssItem> itemList){
        int result = 0;
        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        for(RssItem item: itemList) {
            result = db.delete(RssSQLiteOpenHelper.DB_TABLE_ENTRY, "entry_id=?", new String[]{item.getEntryId()});
        }
        db.close();
        return result;

    }
}
