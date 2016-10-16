package org.foree.contentprovidersample.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import org.foree.contentprovidersample.dao.RssSQLiteOpenHelper;

/**
 * Created by foree on 16-10-13.
 */

public class ItemProvider extends ContentProvider {

    private static final String AUTHORITY = "org.foree.contentprovidersample";
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

    public ItemProvider(){
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
                cur = db.query(RssSQLiteOpenHelper.DB_TABLE_ENTRY, null, null, null, null,null, null, null);
                break;
            case CODE_PROFILE:
                cur = db.query(RssSQLiteOpenHelper.DB_TABLE_PROFILE, null, null, null, null, null, null);
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
        Uri resultUri = null;

        int code = uriMatcher.match(uri);
        switch (code){
            case CODE_ENTRY:
                SQLiteDatabase db = rssSQLiteOpenHelper.getWritableDatabase();
                long id = db.insert(RssSQLiteOpenHelper.DB_TABLE_ENTRY, null, values);
                resultUri = ContentUris.withAppendedId(uri, id);
                break;

        }
        getContext().getContentResolver().notifyChange(uri,null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
