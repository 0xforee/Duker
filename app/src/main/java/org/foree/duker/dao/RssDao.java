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

    public void insert(List<RssItem> itemList){
        Log.d(TAG, "insert rssItems to db");
        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        Cursor cursor = db.query(RssSQLiteOpenHelper.DB_TABLE_ENTRIES, null,
                null, null, null, null, null);

        for(RssItem item: itemList) {
            // 内容不重复
            cursor = db.query(RssSQLiteOpenHelper.DB_TABLE_ENTRIES, null,
                    "id=?", new String[]{item.getEntryId()}, null, null, null);
            if ( cursor.getCount() == 0 ) {
                contentValues.put("id", item.getEntryId());
                contentValues.put("title", item.getTitle());
                contentValues.put("url", item.getUrl());
                contentValues.put("feedId", item.getFeedId());
                contentValues.put("published", item.getPublished());
                contentValues.put("unread", item.isUnread());
                if( db.insert(RssSQLiteOpenHelper.DB_TABLE_ENTRIES, null, contentValues) == -1 ){
                    Log.e(TAG, "Database insert id: " + item.getEntryId() + " error");
                }
            }
        }
        cursor.close();
        db.close();
    }

    /**
     * 根据feedId, unread状态来查询文章
     * @return 符合要求的rssItemList
     */
    public List<RssItem> find(String feedId, boolean unread){
        Log.d(TAG, "get rssItems from db");
        Cursor cursor;
        List<RssItem> rssItemList = new ArrayList<>();
        SQLiteDatabase db = rssSQLiteOpenHelper.getReadableDatabase();
        if (!feedId.equals(FeedlyApiUtils.getApiGlobalAllUrl())) {
            cursor = db.query(RssSQLiteOpenHelper.DB_TABLE_ENTRIES, new String[]{"id,title,url,published,unread"},
                    "feedId=? AND unread=?", new String[]{feedId, unread?"1":"0"}, null, null, "published DESC");
        } else {
            cursor = db.query(RssSQLiteOpenHelper.DB_TABLE_ENTRIES, new String[]{"id,title,url,published,unread"},
                    "unread=?", new String[]{unread?"1":"0"}, null, null, "published DESC");
        }
        while(cursor.moveToNext()){
            String id = cursor.getString(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String url = cursor.getString(cursor.getColumnIndex("url"));
            boolean local_unread = cursor.getInt(cursor.getColumnIndex("unread"))>0;
            long published = cursor.getLong(cursor.getColumnIndex("published"));
            RssItem rssItem = new RssItem(id, title, url, local_unread, published);

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
    public int update(String id, boolean newValue){

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
    public int deleteSome(List<RssItem> itemList){
        int result = 0;
        SQLiteDatabase db = rssSQLiteOpenHelper.getReadableDatabase();
        for(RssItem item: itemList) {
            result = db.delete(RssSQLiteOpenHelper.DB_TABLE_ENTRIES, "id=?", new String[]{item.getEntryId()});
        }
        db.close();
        return result;

    }
}
