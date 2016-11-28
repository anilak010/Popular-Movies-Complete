package com.popularmovies.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class MoviesContract {

    public static final String CONTENT_AUTHORITY = "com.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);
    public static final String PATH_MOVIES="movies";

    public static final class MoviesEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        //Table Name
        public static final String TABLE_NAME = "PopMovies";

        //Columns of the MOVIES TABLE
        public static final String COLUMN_POSTER_PATH = "Poster_Path";
        public static final String COLUMN_MOVIE_NAME = "Movie_Name";
        public static final String COLUMN_OVERVIEW = "Overview";
        public static final String COLUMN_RELEASE_DATE = "Release_Date";
        public static final String COLUMN_POPULARITY = "Popularity";
        public static final String COLUMN_VOTE_COUNT = "Vote_Count";
        public static final String COLUMN_VOTE_AVERAGE = "Vote_Average";
        public static final String COLUMN_POPULAR = "Popular";
        public static final String COLUMN_TOP_RATED = "Top_Rated";
        public static final String COLUMN_FAVOURITES = "Favourites";
    }

}
