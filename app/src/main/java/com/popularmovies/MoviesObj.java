package com.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.popularmovies.data.MoviesContract.MoviesEntry;

/**
 * Created by android on 20-10-2016.
 */
public class MoviesObj {

    private String mMovieTitle, mOverview, mReleaseDate, mVoteCount, mRating, mPosterPath, mFavourites;

    public MoviesObj(Context context, String id){


        //now we will use cursor to get required columns from the database.
        String[] projection= {
                MoviesEntry._ID,
                MoviesEntry.COLUMN_MOVIE_NAME,
                MoviesEntry.COLUMN_OVERVIEW,
                MoviesEntry.COLUMN_RELEASE_DATE,
                MoviesEntry.COLUMN_VOTE_COUNT,
                MoviesEntry.COLUMN_VOTE_AVERAGE,
                MoviesEntry.COLUMN_POSTER_PATH,
                MoviesEntry.COLUMN_FAVOURITES
        };

        String[] movieArgs= {id};
        Cursor cursor= context.getContentResolver().query(
                MoviesEntry.CONTENT_URI,
                projection,
                MoviesEntry._ID + " = ?",
                movieArgs,
                null
        );

        int movieTitle= cursor.getColumnIndex(MoviesEntry.COLUMN_MOVIE_NAME);
        int overview= cursor.getColumnIndex(MoviesEntry.COLUMN_OVERVIEW);
        int releaseDate= cursor.getColumnIndex(MoviesEntry.COLUMN_RELEASE_DATE);
        int voteCount= cursor.getColumnIndex(MoviesEntry.COLUMN_VOTE_COUNT);
        int rating= cursor.getColumnIndex(MoviesEntry.COLUMN_VOTE_AVERAGE);
        int posterPath= cursor.getColumnIndex(MoviesEntry.COLUMN_POSTER_PATH);
        int favourites= cursor.getColumnIndex(MoviesEntry.COLUMN_FAVOURITES);

        while(cursor.moveToNext()) {
            // Use that index to extract the String or Int value of the word
            // at the current row the cursor is on.
            mMovieTitle = cursor.getString(movieTitle);
            mOverview = cursor.getString(overview);
            mReleaseDate = cursor.getString(releaseDate);
            mVoteCount = cursor.getString(voteCount);
            mRating = cursor.getString(rating);
            mPosterPath = cursor.getString(posterPath);
            mFavourites= cursor.getString(favourites);
        }

        cursor.close();
    }


    //in following methods, retrive the data from database
    public String getPosterPath() {

        return mPosterPath;
    }

    public String getMovieName() {
        return mMovieTitle;
    }

    public String getOverview() {
        return mOverview;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public String getVoteCount() {
        return mVoteCount;
    }

    public String getRating() {
        return mRating;
    }

    public String getFavourites() {
        return mFavourites;
    }

}
