package org.foree.contentprovidersample.dao;

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
    public static final String DB_NAME = "sample.db";
    public static final String DB_TABLE_ENTRY = "entry";

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
                createEntryTable(db);
                break;
            default:
                throw new IllegalStateException("Don't known to upgrade to " + version);
        }
    }


    private void createEntryTable(SQLiteDatabase db) {
        // _id title
        db.execSQL("create table entry(" +
                "_id integer primary key autoincrement, " +
                "title varchar" +
                ")"
        );
    }

}
