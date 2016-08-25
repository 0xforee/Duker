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
import java.util.List;

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

    private void insertProfile(RssProfile profile) {
        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();
        contentValues.put("user_id", profile.getId());
        contentValues.put("locale", profile.getLocale());
        contentValues.put("gender", profile.getGender());
        contentValues.put("given_name", profile.getGivenName());
        contentValues.put("family_name", profile.getFamilyName());
        contentValues.put("full_name", profile.getFullName());
        contentValues.put("picture", profile.getPicture());
        contentValues.put("email", profile.getEmail());

        // 内容不重复
        if (db.insertWithOnConflict(RssSQLiteOpenHelper.DB_TABLE_PROFILE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
            Log.e(TAG, "Database insertUserProfile id: " + profile.getId() + " error");
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    private void insertCategory(List<RssCategory> categoryList) {
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

    private void insertSubscription(List<RssFeed> rssFeedList) {
        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();
        for (RssFeed rssFeed : rssFeedList) {
            contentValues.put("feed_id", rssFeed.getFeedId());
            contentValues.put("title", rssFeed.getName());
            contentValues.put("website", rssFeed.getUrl());
            //contentValues.put("icon_url", rssFeed.get());
            // 内容不重复
            if (db.insertWithOnConflict(RssSQLiteOpenHelper.DB_TABLE_FEED, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
                Log.e(TAG, "Database insertFeed id: " + rssFeed.getFeedId() + " error");
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
    public List<RssItem> findUnreadByFeedId(String feedId, boolean unread) {
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

        SQLiteDatabase db = rssSQLiteOpenHelper.getReadableDatabase();
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
        SQLiteDatabase db = rssSQLiteOpenHelper.getReadableDatabase();
        for(RssItem item: itemList) {
            result = db.delete(RssSQLiteOpenHelper.DB_TABLE_ENTRY, "entry_id=?", new String[]{item.getEntryId()});
        }
        db.close();
        return result;

    }
}
