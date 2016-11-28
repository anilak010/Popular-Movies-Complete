package com.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.support.v4.widget.CursorAdapter;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.popularmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

public class MoviesAdapter extends CursorAdapter{


    public MoviesAdapter(Context context, Cursor movies){
        super(context, movies , 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.display_arrangement, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView MoviePoster = (ImageView) view.findViewById(com.popularmovies.R.id.poster);

        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        /*if(MainActivity.mTwoPane){
            width = width/3;
        }*/
        //Picasso.with(context).load(cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH))).resize(width/3,0).into(MoviePoster);
        Picasso.with(context).load(cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH))).into(MoviePoster);
    }
}
