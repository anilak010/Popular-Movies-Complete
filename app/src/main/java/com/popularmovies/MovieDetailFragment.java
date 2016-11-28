package com.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.popularmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by android on 28-10-2016.
 */
public class MovieDetailFragment extends Fragment {

    private MoviesObj moviesObj;
    public ArrayList<String> list= new ArrayList<>();
    public boolean count= false;
    TrailerTask mTtrailerTask= new TrailerTask();

    public MovieDetailFragment(){
        //required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Receive argument from MovieDetailActivity
        Bundle arguments= this.getArguments();
        final String movieId= arguments.getString("message", "0");

        View rootView = inflater.inflate(com.popularmovies.R.layout.movie_detail_fragment, container, false);


        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (count == false) {
            if (networkInfo != null && networkInfo.isConnected()){

                mTtrailerTask.execute(movieId);
            }

        }



        TextView title = (TextView) rootView.findViewById(com.popularmovies.R.id.movie_title);

        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        moviesObj= new MoviesObj(getContext(), movieId);

        ImageView poster = (ImageView) rootView.findViewById(com.popularmovies.R.id.movie_details_poster);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        Picasso.with(getContext()).load(moviesObj.getPosterPath()).resize(width/2,0).into(poster);


        TextView movieTitle = (TextView) rootView.findViewById(com.popularmovies.R.id.movie_title);
        movieTitle.setText(moviesObj.getMovieName());

        TextView overview = (TextView) rootView.findViewById(com.popularmovies.R.id.overview);
        overview.setText(moviesObj.getOverview());

        TextView release = (TextView) rootView.findViewById(com.popularmovies.R.id.release);
        release.setText(moviesObj.getReleaseDate());

        TextView votes = (TextView) rootView.findViewById(com.popularmovies.R.id.votes);
        votes.setText(moviesObj.getVoteCount());

        TextView ratings = (TextView) rootView.findViewById(com.popularmovies.R.id.ratings);
        ratings.setText(moviesObj.getRating());

        //set a click listener for Reviews textview and send an intent to ReviewsActivity
        TextView reviews= (TextView) rootView.findViewById(R.id.review);
        reviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "going to reviews", Toast.LENGTH_SHORT).show();
                long longMovieId= Long.valueOf(movieId);
                Intent reviewIntent= new Intent(getActivity(), ReviewsActivity.class).putExtra("ReviewMovieId", longMovieId);
                startActivity(reviewIntent);
            }
        });




        ListView lt= (ListView) rootView.findViewById(R.id.list_trailer_view);
        lt.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, list.size()*80));
        TrailerAdapter trailerAdapter= new TrailerAdapter(getActivity(), list);
        lt.setAdapter(trailerAdapter);



        //if internet connection is lost, show "No Internet Connection"
        if(list.size()==0){
            TextView noTrailer= (TextView)rootView.findViewById(R.id.no_trailer);
            if (!(networkInfo != null && networkInfo.isConnected())){
                // Update empty state with no connection error message
                noTrailer.setText("No Internet Connection");
            }
            else{
                noTrailer.setText("Trailer Not Available");
            }

        }
        else{
            lt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(list.get(position)));
                    startActivity(intent);
                }
            });
        }

        //Check if the movie is already in favourites. If yes then pre check the check box.
        CheckBox favouritesCheckBox = (CheckBox)rootView.findViewById(R.id.favourites_check_box);

        int isFavourites= Integer.parseInt(moviesObj.getFavourites()) ;
        if(isFavourites== 1){
            favouritesCheckBox.setChecked(true);
        }

        //set a click listener to the check box to add or remove the movie from favourites list
        favouritesCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

               @Override
               public void onCheckedChanged(CompoundButton checkBoxView,boolean isChecked) {
                   int movID= Integer.parseInt(movieId);
                   if(isChecked){
                       //Add to favourites
                       Toast.makeText(getActivity(), "Added to Favourites list", Toast.LENGTH_SHORT).show();
                       favouritesList(movID, isChecked);
                   }
                   else{
                       //Remove from favourites
                        Toast.makeText(getActivity(), "Removed from Favourites list", Toast.LENGTH_SHORT).show();
                       favouritesList(movID, isChecked);
                    }


               }
           }
        );


        return rootView;


    }

    @Override
    public void onDestroyView() {
        mTtrailerTask.cancel(true);
        super.onDestroyView();
    }

    //Method to add or remove a movie to/from favourites list
    public void favouritesList(int id, boolean isChecked){


        ContentValues values= new ContentValues();

        String[] projection= {
                MoviesContract.MoviesEntry._ID,
                MoviesContract.MoviesEntry.COLUMN_FAVOURITES
        };

        String selection = MoviesContract.MoviesEntry._ID + "=?";
        String[] args = {String.valueOf(id)};
        Cursor cursor = getContext().getContentResolver().query(MoviesContract.MoviesEntry.CONTENT_URI, projection, selection, args, null);

        if(cursor.getCount()!=0){
            cursor.moveToFirst();

            values.put(MoviesContract.MoviesEntry._ID, id);
            if(isChecked){
                values.put(MoviesContract.MoviesEntry.COLUMN_FAVOURITES, 1); //1 means add to favourites
                getContext().getContentResolver().update(MoviesContract.MoviesEntry.CONTENT_URI, values, selection, args);
            }
            else{
                values.put(MoviesContract.MoviesEntry.COLUMN_FAVOURITES, 0); //0 means remove from favourites
                getContext().getContentResolver().update(MoviesContract.MoviesEntry.CONTENT_URI, values, selection, args);
            }


        }else{
            Uri temp = getContext().getContentResolver().insert(MoviesContract.MoviesEntry.CONTENT_URI, values);
            Log.v("URI", temp.toString());
        }
    }


    public class TrailerTask extends AsyncTask<String, Void, ArrayList<String>> {


        @Override
        protected ArrayList<String> doInBackground(String... movieId) {
            //String movieId=String.valueOf(id);
            HttpURLConnection trailerUrlConnection= null;
            BufferedReader trailerReader= null;
            URL trailerUrl= null;
            String trailerJsonStr= null;
            final String TRAILER_BASE_URL= "https://api.themoviedb.org/3/movie/"+movieId[0]+ "/videos?api_key=";

            try {
                trailerUrl= new URL(TRAILER_BASE_URL.concat(BuildConfig.POP_MOVIES_API_KEY));
                trailerUrlConnection= (HttpURLConnection)trailerUrl.openConnection();
                trailerUrlConnection.setRequestMethod("GET");
                trailerUrlConnection.connect();
                Log.i("URL", trailerUrl.toString());

                //Read input stream into a string
                InputStream inputStream= trailerUrlConnection.getInputStream();
                StringBuffer stringBuffer= new StringBuffer();
                if(inputStream== null)
                    return null;

                trailerReader= new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line= trailerReader.readLine())!= null)
                    stringBuffer.append(line+ "\n");

                //If stream is empty, no point in parsing
                if(stringBuffer.length()==0)
                    return null;

                trailerJsonStr = stringBuffer.toString();
                Log.i("trailerJsonStr is = ", trailerJsonStr);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (trailerUrlConnection != null) {
                    trailerUrlConnection.disconnect();
                }
                if (trailerReader != null) {
                    try {
                        trailerReader.close();
                    } catch (final IOException e) {
                        Log.e("", "Error closing stream", e);
                    }
                }
                try {
                    getTrailers(trailerJsonStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return list;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);

            if(count == false){

                Fragment fr= getFragmentManager().findFragmentByTag("DetailFragmentTag");
                fr.getFragmentManager().beginTransaction().detach(fr).attach(fr).commit();

                count= true;
            }


        }


        //Fetch Trailer from JSON
        private void getTrailers(String trailerJsonStr) throws JSONException{
            String baselink = "https://www.youtube.com/watch?v=";
            String finalLink= "";

            JSONObject trailerJson= new JSONObject(trailerJsonStr);
            JSONArray trailerList= trailerJson.getJSONArray("results");

            for(int i=0; i< trailerList.length(); i++){
                JSONObject trailerDetails= trailerList.getJSONObject(i);
                String trailerKey= trailerDetails.getString("key");
                String videoType= trailerDetails.getString("type");


                String appendLink= trailerKey;
                finalLink= baselink+ trailerKey;
                list.add(finalLink);

            }


        }


    }
}
