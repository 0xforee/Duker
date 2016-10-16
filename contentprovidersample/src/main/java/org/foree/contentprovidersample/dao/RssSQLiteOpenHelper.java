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
    public static final String DB_NAME = "duker.db";
    public static final String DB_TABLE_ENTRY = "entry";
    public static final String DB_TABLE_PROFILE = "profile";
    public static final String DB_TABLE_CATEGORY = "category";
    public static final String DB_TABLE_FEED = "feed";
    public static final String DB_TABLE_SUB_CATE = "feed_category";

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
                createProfileTable(db);
                createSubCateTable(db);
                createCategoryTable(db);
                createFeedTable(db);
                createEntryTable(db);
                break;
            default:
                throw new IllegalStateException("Don't known to upgrade to " + version);
        }
    }

    private void createSubCateTable(SQLiteDatabase db){
        // _id, feed_id, cate_id
        db.execSQL("create table feed_category( " +
                "_id integer primary key autoincrement, " +
                "feed_id varchar, " +
                "category_id varchar " +
                ")"
        );
    }

    private void createFeedTable(SQLiteDatabase db){
        // feed_id title website icon_url
        db.execSQL("create table feed( " +
                "feed_id varchar primary key, " +
                "title varchar, " +
                "website varchar, " +
                "icon_url varchar " +
                ")"
        );

    }

    private void createCategoryTable(SQLiteDatabase db) {
        // category_id label description
        db.execSQL("create table category(" +
                "category_id varchar primary key," +
                "label varchar, " +
                "description varchar" +
                ")"
        );
    }

    private void createProfileTable(SQLiteDatabase db) {
        // user_id locale gender given_name family_name full_name picture email
        db.execSQL("create table profile(" +
                "user_id varchar primary key," +
                "locale varchar, " +
                "gender varchar," +
                "given_name varchar, " +
                "family_name varchar, " +
                "full_name varchar," +
                "picture varchar," +
                "email varchar " +
                ")"
        );
    }

    private void createEntryTable(SQLiteDatabase db) {
        // entry_id feed_id feed_name unread visual summary content url published title
        db.execSQL("create table entry(" +
                "entry_id varchar primary key," +
                "feed_id varchar, " +
                "feed_name varchar," +
                "unread integer, " +
                "visual varchar, " +
                "summary varchar," +
                "content varchar," +
                "url varchar, " +
                "published integer," +
                "title varchar" +
                ")"
        );
    }

}
