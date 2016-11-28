package com.popularmovies;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MovieDetailActivity extends AppCompatActivity {
    Bitmap img;
    static String movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.popularmovies.R.layout.activity_movie_detail);

        //Receive the id from setOnItemClickListener
        long id;


        //Get movie id as as intent extra from MoviesFragment class
        Bundle extras= getIntent().getExtras();
        id= extras.getLong("MovieID");

        Log.i("MovieID is= ", ""+id);

        movieId= String.valueOf(id);
        if(savedInstanceState==null){
            Bundle bundle = new Bundle();
            bundle.putString("message", movieId );
            MovieDetailFragment movieDetailFragment= new MovieDetailFragment();
            movieDetailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_detail,movieDetailFragment, "DetailFragmentTag").commit();
        }


    }

}

