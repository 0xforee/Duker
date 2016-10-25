package org.foree.duker.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import org.foree.duker.dao.RssSQLiteOpenHelper;

import java.util.Arrays;

/**
 * Created by foree on 16-10-13.
 */

public class RssInfoProvider extends ContentProvider {
    private static final String TAG = RssInfoProvider.class.getSimpleName();

    private static final String AUTHORITY = "org.foree.duker";
    private static final String PATH_ENTRY = RssSQLiteOpenHelper.DB_TABLE_ENTRY;
    private static final String PATH_CATEGORY = RssSQLiteOpenHelper.DB_TABLE_CATEGORY;
    private static final String PATH_FEED = RssSQLiteOpenHelper.DB_TABLE_FEED;
    private static final String PATH_PROFILE = RssSQLiteOpenHelper.DB_TABLE_PROFILE;
    private static final String PATH_SUB_CATE = RssSQLiteOpenHelper.DB_TABLE_SUB_CATE;

    RssSQLiteOpenHelper rssSQLiteOpenHelper;
    UriMatcher uriMatcher;

    // 匹配码
    private static final int CODE_ENTRY = 0;
    private static final int CODE_CATEGORY = 1;
    private static final int CODE_FEED = 2;
    private static final int CODE_PROFILE = 3;
    private static final int CODE_SUB_CATE = 4;

    public RssInfoProvider(){
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // add table uri
        uriMatcher.addURI(AUTHORITY, PATH_ENTRY, CODE_ENTRY);
        uriMatcher.addURI(AUTHORITY, PATH_CATEGORY, CODE_CATEGORY);
        uriMatcher.addURI(AUTHORITY, PATH_FEED, CODE_FEED);
        uriMatcher.addURI(AUTHORITY, PATH_PROFILE, CODE_PROFILE);
        uriMatcher.addURI(AUTHORITY, PATH_SUB_CATE, CODE_SUB_CATE);

        // add id uri
    }

    @Override
    public boolean onCreate() {
        rssSQLiteOpenHelper = new RssSQLiteOpenHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cur = null;
        SQLiteDatabase db = rssSQLiteOpenHelper.getReadableDatabase();

        switch (uriMatcher.match(uri)){
            case CODE_ENTRY:
                cur = db.query(RssSQLiteOpenHelper.DB_TABLE_ENTRY, projection, selection, selectionArgs, null,null, sortOrder);
                break;
            case CODE_PROFILE:
                cur = db.query(RssSQLiteOpenHelper.DB_TABLE_PROFILE, projection, selection, selectionArgs, null,null, sortOrder);
                break;
            case CODE_FEED:
                cur = db.query(RssSQLiteOpenHelper.DB_TABLE_FEED, projection, selection, selectionArgs, null,null, sortOrder);
                break;
            case CODE_SUB_CATE:
                cur = db.query(RssSQLiteOpenHelper.DB_TABLE_SUB_CATE, projection, selection, selectionArgs, null,null, sortOrder);
                break;
            case CODE_CATEGORY:
                cur = db.query(RssSQLiteOpenHelper.DB_TABLE_CATEGORY, projection, selection, selectionArgs, null,null, sortOrder);
                break;

        }
        return cur;
    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri resultUri;
        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        long id = 0;

        int code = uriMatcher.match(uri);
        switch (code){
            case CODE_ENTRY:
                id = db.insertWithOnConflict(RssSQLiteOpenHelper.DB_TABLE_ENTRY, null, values,SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case CODE_PROFILE:
                id = db.insertWithOnConflict(RssSQLiteOpenHelper.DB_TABLE_PROFILE, null, values,SQLiteDatabase.CONFLICT_REPLACE);
                getContext().getContentResolver().notifyChange(uri,null);
                break;
            case CODE_CATEGORY:
                id = db.insertWithOnConflict(RssSQLiteOpenHelper.DB_TABLE_CATEGORY, null, values,SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case CODE_FEED:
                id = db.insertWithOnConflict(RssSQLiteOpenHelper.DB_TABLE_FEED, null, values,SQLiteDatabase.CONFLICT_REPLACE);
                getContext().getContentResolver().notifyChange(uri,null);
                break;
            case CODE_SUB_CATE:
                id = db.insertWithOnConflict(RssSQLiteOpenHelper.DB_TABLE_SUB_CATE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                getContext().getContentResolver().notifyChange(uri,null);
                break;
        }

        resultUri = ContentUris.withAppendedId(uri, id);

        return resultUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int result = values.length;

        switch (uriMatcher.match(uri)){
            case CODE_ENTRY:
            case CODE_CATEGORY:
                insertItems(uri, values);
                break;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        int result = 0;

        switch (uriMatcher.match(uri)){
            case CODE_ENTRY:
                result = db.delete(RssSQLiteOpenHelper.DB_TABLE_ENTRY, selection, selectionArgs);
                break;

        }
        return result;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        int result = 0;

        switch (uriMatcher.match(uri)){
            case CODE_ENTRY:
                result = db.update(RssSQLiteOpenHelper.DB_TABLE_ENTRY, values, selection, selectionArgs);
                break;

        }
        return result;
    }

    // 使用事务加快大量数据插入速度
    private void insertItems(Uri uri, ContentValues[] contentValues){
        synchronized (this) {
            int tmp = 1;
            Log.d(TAG, "insert uri = " + uri.toString() + " " + contentValues.length + " items to db");
            // 拆分itemList，dataBase 一次事务只能插入1000条数据
            while(contentValues.length>(1000*tmp)){
                insertItemsInternal(uri, Arrays.copyOfRange(contentValues,1000*(tmp-1),1000*tmp));
                tmp++;
            }
            insertItemsInternal(uri, Arrays.copyOfRange(contentValues,1000*(tmp-1),contentValues.length));
        }
    }

    private void insertItemsInternal(Uri uri, ContentValues[] contentValues) {
        SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        for (ContentValues item : contentValues) {
            // 内容不重复
            if( ContentUris.parseId(insert(uri, item)) == -1 ){
                Log.e(TAG, "Database insert error");
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }
}
