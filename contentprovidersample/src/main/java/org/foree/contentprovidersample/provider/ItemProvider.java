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
    private static final String PATH_ENTRY = "entry";

    RssSQLiteOpenHelper rssSQLiteOpenHelper;
    UriMatcher uriMatcher;

    // 匹配码
    private static final int CODE_ENTRY = 0;

    public ItemProvider(){
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PATH_ENTRY, CODE_ENTRY);
    }

    @Override
    public boolean onCreate() {
        rssSQLiteOpenHelper = new RssSQLiteOpenHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
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
