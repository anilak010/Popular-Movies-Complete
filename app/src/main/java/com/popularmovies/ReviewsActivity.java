package com.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

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

public class ReviewsActivity extends AppCompatActivity {

    ArrayList<String> reviewList= new ArrayList<>();
    MoviesObj moviesObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        //Get movie id as an intent extra from MovieDetailFragment
        long id;
        Bundle reviewExtras= getIntent().getExtras();
        id= reviewExtras.getLong("ReviewMovieId");

        //Log.i("ReviewMovieId is=", ""+id);

        String movieId= String.valueOf(id);

        moviesObj= new MoviesObj(this , movieId);
        TextView movieTitle= (TextView)findViewById(R.id.review_movie_title);
        movieTitle.setText(moviesObj.getMovieName());

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()){
            ReviewAsyncTask reviewAsyncTask= new ReviewAsyncTask();
            reviewAsyncTask.execute(movieId);
        }
        else{
            setContentView(R.layout.empty_layout);
        }

    }

    public class ReviewAsyncTask extends AsyncTask<String, Void, ArrayList<String>>{

        @Override
        protected ArrayList<String> doInBackground(String... idArray) {

            HttpURLConnection reviewUrlConnection= null;
            BufferedReader reviewReader= null;
            URL reviewUrl= null;
            String reviewJsonStr= null;
            final String REVIEW_BASE_URL= "https://api.themoviedb.org/3/movie/"+idArray[0]+ "/reviews?api_key=";

            try {
                reviewUrl= new URL(REVIEW_BASE_URL.concat(BuildConfig.POP_MOVIES_API_KEY));
                reviewUrlConnection= (HttpURLConnection)reviewUrl.openConnection();
                reviewUrlConnection.setRequestMethod("GET");
                reviewUrlConnection.connect();
                Log.i("URL", reviewUrl.toString());

                //Read input stream into a string
                InputStream inputStream= reviewUrlConnection.getInputStream();
                StringBuffer stringBuffer= new StringBuffer();
                if(inputStream== null)
                    return null;

                reviewReader= new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line= reviewReader.readLine())!= null)
                    stringBuffer.append(line+ "\n");

                //If stream is empty, no point in parsing
                if(stringBuffer.length()==0)
                    return null;

                reviewJsonStr = stringBuffer.toString();
                Log.i("reviewJsonStr is = ", reviewJsonStr);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reviewUrlConnection != null) {
                    reviewUrlConnection.disconnect();
                }
                if (reviewReader != null) {
                    try {
                        reviewReader.close();
                    } catch (final IOException e) {
                        Log.e("", "Error closing stream", e);
                    }
                }
                try {
                    getReviews(reviewJsonStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return reviewList;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {
            super.onPostExecute(list);
            TextView noReview= (TextView)findViewById(R.id.no_review);
            Context context= ReviewsActivity.this;
            if(list.size()==0){

                noReview.setText("No Reviews Available");
            }
            else{
                assert noReview != null;
                if (noReview != null) {
                    noReview.setVisibility(View.GONE);
                }
                ListView reviewListView= (ListView)findViewById(R.id.reviews_list_view);
                ReviewAdapter reviewsAdapter= new ReviewAdapter(context, list);
                reviewListView.setAdapter(reviewsAdapter);
            }

        }

        private void getReviews(String reviewJsonStr) throws JSONException {
            JSONObject reviewJson= new JSONObject(reviewJsonStr);
            JSONArray resultsArray= reviewJson.getJSONArray("results");


            for(int i=0; i<resultsArray.length();i++){
                JSONObject jsonObjectItem= resultsArray.getJSONObject(i);
                String authorString= jsonObjectItem.getString("author");
                String contentString= jsonObjectItem.getString("content");

                String reviewString= "\""+ contentString + "\"" + "\n\n -- " + authorString +"\n\n";
                reviewList.add(reviewString);
            }
        }
    }
}
