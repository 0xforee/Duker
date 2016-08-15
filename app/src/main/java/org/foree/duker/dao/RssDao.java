package org.foree.duker.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.foree.duker.rssinfo.RssItem;
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
                insertInternal(itemList.subList(1000*(tmp-1),1000*tmp));
                tmp++;
            }
            insertInternal(itemList.subList(1000*(tmp-1), itemList.size()));
        }
    }

    private void insertInternal(List<RssItem> subItemList) {
        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();
        for (RssItem item : subItemList) {
            contentValues.put("id", item.getEntryId());
            contentValues.put("title", item.getTitle());
            contentValues.put("url", item.getUrl());
            contentValues.put("feedId", item.getFeedId());
            contentValues.put("feedName", item.getFeedName());
            contentValues.put("published", item.getPublished());
            contentValues.put("unread", item.isUnread());
            // 内容不重复
            if (db.insertWithOnConflict(RssSQLiteOpenHelper.DB_TABLE_ENTRIES, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
                Log.e(TAG, "Database insertEntries id: " + item.getEntryId() + " error");
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
        Log.d(TAG, "get unread = " + unread + " rssItems from db");
        String selection;
        String[] selectionArgs;
        List<RssItem> rssItemList = new ArrayList<>();
        SQLiteDatabase db = rssSQLiteOpenHelper.getReadableDatabase();
        if (FeedlyApiUtils.isGlobalAllUrl(feedId)) {
            selection = "unread=?";
            selectionArgs = new String[]{unread ? "1" : "0"};
        } else {
            selection = "feedId=? AND unread=?";
            selectionArgs = new String[]{feedId, unread ? "1" : "0"};
        }
        Cursor cursor = db.query(RssSQLiteOpenHelper.DB_TABLE_ENTRIES, new String[]{"id,title,url,published,unread,feedName"},
                selection, selectionArgs, null, null, "published DESC");
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String url = cursor.getString(cursor.getColumnIndex("url"));
            boolean local_unread = cursor.getInt(cursor.getColumnIndex("unread")) > 0;
            long published = cursor.getLong(cursor.getColumnIndex("published"));
            String feedName = cursor.getString(cursor.getColumnIndex("feedName"));
            RssItem rssItem = new RssItem(id, title, url, feedName, local_unread, published);

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

        int result = db.update(RssSQLiteOpenHelper.DB_TABLE_ENTRIES, contentValues, "id=?", new String[]{id});

        db.close();

        return result;
    }

    /**
     * 删除某一项
     * @param id rssItem的标示
     */
    public int delete(String id){

        SQLiteDatabase db = rssSQLiteOpenHelper.getReadableDatabase();
        int result = db.delete(RssSQLiteOpenHelper.DB_TABLE_ENTRIES, "id=?", new String[]{id});
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
            result = db.delete(RssSQLiteOpenHelper.DB_TABLE_ENTRIES, "id=?", new String[]{item.getEntryId()});
        }
        db.close();
        return result;

    }
}
