package org.foree.duker.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.foree.duker.api.FeedlyApiHelper;
import org.foree.duker.rssinfo.RssItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by foree on 2016/8/6.
 * 数据库操作方法
 */
public class RssDao {
    private static final String TAG = RssDao.class.getSimpleName();
    private RssSQLiteOpenHelper rssSQLiteOpenHelper;

    public RssDao(Context context){
        rssSQLiteOpenHelper = new RssSQLiteOpenHelper(context);
    }

    public void insert(List<RssItem> itemList){
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
     * 根据feedId来查询
     * @return 符合要求的rssItemList
     */
    public List<RssItem> find(String feedId){
        Cursor cursor;
        List<RssItem> rssItemList = new ArrayList<>();
        SQLiteDatabase db = rssSQLiteOpenHelper.getReadableDatabase();
        if (!feedId.equals(FeedlyApiHelper.API_GLOBAL_ALL_URL.replace(":userId", FeedlyApiHelper.USER_ID))) {
            cursor = db.query(RssSQLiteOpenHelper.DB_TABLE_ENTRIES, new String[]{"id,title,url,published,unread"},
                    "feedId=?", new String[]{feedId}, null, null, "published DESC");
        } else {
            cursor = db.query(RssSQLiteOpenHelper.DB_TABLE_ENTRIES, new String[]{"id,title,url,published,unread"},
                    null, null, null, null, "published DESC");
        }
        while(cursor.moveToNext()){
            String id = cursor.getString(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String url = cursor.getString(cursor.getColumnIndex("url"));
            boolean unread = cursor.getInt(cursor.getColumnIndex("unread"))>0;
            long published = cursor.getLong(cursor.getColumnIndex("published"));
            RssItem rssItem = new RssItem(id, title, url, unread, published);

            rssItemList.add(rssItem);

        }
        cursor.close();
        db.close();
        return rssItemList;
    }
}
