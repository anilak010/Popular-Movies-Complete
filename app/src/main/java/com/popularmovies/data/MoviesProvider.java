package com.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

public class MoviesProvider extends ContentProvider{
        //A log tag to print log messages
    public static final String LOG_TAG = MoviesProvider.class.getSimpleName();

    //Create an PetDbHelper object to access the database
    private MoviesHelper mDbHelper;

    //Generate IDs for different patterns in uri
    private static final int MOVIES = 100;
    private static final int MOVIES_ID = 101;

    //Declare a uri matcher to match the uris
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //link the ids to the uri patterns using adddUri()
    static{
        sUriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES, MOVIES);
        sUriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES + "/#", MOVIES_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new MoviesHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        switch (sUriMatcher.match(uri)){
            case MOVIES:
                cursor = db.query(MoviesContract.MoviesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case MOVIES_ID:
                selection = MoviesContract.MoviesEntry._ID + "=?;";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(MoviesContract.MoviesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown uri: " + uri);

        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch(sUriMatcher.match(uri)){
            case MOVIES:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            case MOVIES_ID:
                return MoviesContract.MoviesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Cannot query unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long row;
            switch (sUriMatcher.match(uri)) {
                case MOVIES:
                    row = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, values);
                    if (row == -1)
                        return null;
                    break;

                default:
                    throw new IllegalArgumentException("Cannot query unknown uri: " + uri);
            }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, row);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)){
            case MOVIES:
                return db.delete(MoviesContract.MoviesEntry.TABLE_NAME, selection, selectionArgs);
            case MOVIES_ID:
                //set the selection and arguments
                selection = MoviesContract.MoviesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return db.delete(MoviesContract.MoviesEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int row;
        switch(sUriMatcher.match(uri)){
            case MOVIES:
                row = db.update(MoviesContract.MoviesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MOVIES_ID:
                //set the selection and arguments
                selection = MoviesContract.MoviesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                row = db.update(MoviesContract.MoviesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return row;
    }
}
