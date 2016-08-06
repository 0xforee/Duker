package org.foree.duker.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

    public long insert(List<RssItem> itemList){
        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        for(RssItem item: itemList) {
            // 内容不重复
            contentValues.put("id", item.getEntryId());
            // TODO: 继续添加item

        }
        Long result = db.insert("entries", null, contentValues);
        db.close();
        return result;
    }
}
