package org.foree.duker.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by foree on 2016/8/6.
 * 数据库创建升级的帮助类
 */
public class RssSQLiteOpenHelper extends SQLiteOpenHelper{
    private static final String TAG = RssSQLiteOpenHelper.class.getSimpleName();
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "duker.db";
    public static final String DB_TABLE_ENTRIES = "entries";

    public RssSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        onUpgrade(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        for ( int version = oldVersion +1; version <= newVersion; version++){
            onUpgradeTo(db, version);
        }
    }

    private void onUpgradeTo(SQLiteDatabase db, int version) {
        switch (version) {
            case 1:
                createEntriesTable(db);
                break;
            default:
                throw new IllegalStateException("Don't known to upgrade to " + version);
        }
    }

    private void createEntriesTable(SQLiteDatabase db) {
        // id, category, unread, url, published, title
        db.execSQL("create table entries(" +
                "id varchar(255) primary key," +
                "feedId varchar(255), " +
                "feedName varchar(255)," +
                "unread integer, " +
                "visual varchar(255), " +
                "summary varchar," +
                "content varchar," +
                "url varchar(255), " +
                "published integer," +
                "title varchar(255)" +
                ")"
        );
    }

}
