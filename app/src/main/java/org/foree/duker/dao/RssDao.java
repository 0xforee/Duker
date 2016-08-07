package org.foree.duker.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.foree.duker.rssinfo.RssItem;

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
                if( db.insert(RssSQLiteOpenHelper.DB_TABLE_ENTRIES, null, contentValues) == -1 ){
                    Log.e(TAG, "Database insert id: " + item.getEntryId() + " error");
                }
            }
        }
        cursor.close();
        db.close();
    }
}
