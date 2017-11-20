package com.example.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.example.android.popularmovies.data.FavoriteMoviesContract.FavoriteMoviesEntry;

public class FavoriteMoviesProvider extends ContentProvider {

    public static final int CODE_FAVORITE_MOVIES = 100;
    public static final int CODE_FAVORITE_MOVIES_WITH_ID = 200;

    private static final String LOG_TAG = FavoriteMoviesProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FavoriteMoviesDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavoriteMoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, FavoriteMoviesContract.PATH_MOVIES, CODE_FAVORITE_MOVIES);
        matcher.addURI(authority, FavoriteMoviesContract.PATH_MOVIES + "/#", CODE_FAVORITE_MOVIES_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new FavoriteMoviesDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {

            case CODE_FAVORITE_MOVIES: {
                cursor = db.query(
                        FavoriteMoviesContract.FavoriteMoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            }
            case CODE_FAVORITE_MOVIES_WITH_ID: {
                String idString = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{idString};

                cursor = db.query(
                        FavoriteMoviesContract.FavoriteMoviesEntry.TABLE_NAME,
                        projection,
                        FavoriteMoviesEntry.COLUMN_MOVIE_ID + " = ?",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);

                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        int rowsInserted = 0;

        switch (sUriMatcher.match(uri)) {
            case CODE_FAVORITE_MOVIES:
                long newRowId = db.insert(FavoriteMoviesEntry.TABLE_NAME, null, values);
                if (newRowId != -1) {
                    rowsInserted++;
                    returnUri = FavoriteMoviesEntry.buildMoviesUri(newRowId);
                }else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }

                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported URI for insertion: " + uri);
        }

        if (rowsInserted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numRowsDeleted;

        switch (sUriMatcher.match(uri)) {

            case CODE_FAVORITE_MOVIES:
                numRowsDeleted = db.delete(
                        FavoriteMoviesContract.FavoriteMoviesEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case CODE_FAVORITE_MOVIES_WITH_ID:
                String movieIdString = uri.getLastPathSegment();

                String[] selectionArguments = new String[]{movieIdString};

                numRowsDeleted = db.delete(
                        FavoriteMoviesContract.FavoriteMoviesEntry.TABLE_NAME,
                        FavoriteMoviesEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectionArguments);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri,ContentValues values,
                      String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numUpdated ;

        if (values == null){
            throw new IllegalArgumentException("Cannot have null content values");
        }

        switch(sUriMatcher.match(uri)){
            case CODE_FAVORITE_MOVIES:{
                numUpdated = db.update(FavoriteMoviesEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            case CODE_FAVORITE_MOVIES_WITH_ID: {
                numUpdated = db.update(FavoriteMoviesEntry.TABLE_NAME,
                        values,
                        FavoriteMoviesEntry._ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))});
                break;
            }
            default:{
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        if (numUpdated > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numUpdated;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match){
            case CODE_FAVORITE_MOVIES:{
                return FavoriteMoviesContract.FavoriteMoviesEntry.CONTENT_DIR_TYPE;
            }
            case CODE_FAVORITE_MOVIES_WITH_ID:{
                return FavoriteMoviesContract.FavoriteMoviesEntry.CONTENT_ITEM_TYPE;
            }
            default:{
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }
}