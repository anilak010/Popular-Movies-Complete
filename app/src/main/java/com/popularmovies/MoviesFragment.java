package com.popularmovies;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.popularmovies.data.MoviesContract.MoviesEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private int mPosition = ListView.INVALID_POSITION;
    private MoviesAdapter adapter;
    public static String sortBy = "";
    private String[] projection = {MoviesEntry._ID, MoviesEntry.COLUMN_MOVIE_NAME,
            MoviesEntry.COLUMN_VOTE_COUNT, MoviesEntry.COLUMN_VOTE_AVERAGE, MoviesEntry.COLUMN_POPULARITY,
            MoviesEntry.COLUMN_POSTER_PATH, MoviesEntry.COLUMN_RELEASE_DATE, MoviesEntry.COLUMN_OVERVIEW,
            MoviesEntry.COLUMN_POPULAR, MoviesEntry.COLUMN_TOP_RATED, MoviesEntry.COLUMN_POPULAR};
    GridView displayType;
    private static final String SELECTED_KEY = "selected_position";
    private Context mContext;
    TextView mNoInternetTextView;

    public MoviesFragment() {
        // Required empty public constructor
    }
    /** TextView that is displayed when the list is empty */


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(com.popularmovies.R.layout.fragment_movies, container, false);

        mNoInternetTextView= (TextView)rootView.findViewById(R.id.movies_no_internet);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){
            mNoInternetTextView.setVisibility(View.GONE);
        }


        // internet connectivity is present
        final String BASE_URL;
        SharedPreferences sort = PreferenceManager.getDefaultSharedPreferences(getContext());
        sortBy = sort.getString("sort", "");
        if(sortBy.equals("Favourites")){
            //getFavouriteMovies();
            getLoaderManager().initLoader(0, null, MoviesFragment.this);
        }
        else{

            if (sortBy.equals("Popularity")) {
                BASE_URL = "http://api.themoviedb.org/3/movie/popular?api_key=";
            } else{
                BASE_URL = "http://api.themoviedb.org/3/movie/top_rated?api_key=";
            }

            if (networkInfo != null && networkInfo.isConnected()){
                new FetchMoviesTask().execute(BASE_URL);
            }

        }

        displayType = (GridView) rootView.findViewById(R.id.displaytype);

        // Initialise the adapter
        adapter = new MoviesAdapter(mContext, null);


        // Set adapter to the gridview
        displayType = (GridView) rootView.findViewById(com.popularmovies.R.id.displaytype);
        displayType.setAdapter(adapter);

        displayType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                View detailContainer = getActivity().findViewById(R.id.container_detail);
                if(detailContainer!= null){
                    Bundle bundle = new Bundle();
                    bundle.putString("message", String.valueOf(id));
                    MovieDetailFragment movieDetailFragment= new MovieDetailFragment();
                    movieDetailFragment.setArguments(bundle);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container_detail, movieDetailFragment, "DetailFragmentTag").commit();
                }
                else{
                    Intent intent= new Intent(getActivity(), MovieDetailActivity.class).putExtra("MovieID", id);
                    Log.i("THIS IS THE POSITION =", ""+position+ " and ID is" + id);
                    startActivity(intent);
                }
            }
        });



        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            mContext= context;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    public class FetchMoviesTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            URL url = null;
            String MoviesJsonStr = null;
            final String POPMOVIES_BASE_URL= params[0];

            try {

                url = new URL(POPMOVIES_BASE_URL.concat(BuildConfig.POP_MOVIES_API_KEY));
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(1000);
                urlConnection.connect();
                Log.v("URL", url.toString());

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                MoviesJsonStr = buffer.toString();
                //Log.v("OUTPUT", MoviesJsonStr);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
                try {
                    getMovieNames(MoviesJsonStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            getLoaderManager().initLoader(0, null, MoviesFragment.this);
        }

        private void getMovieNames(String MovieJsonStr) throws JSONException {
            JSONObject MovieJson = new JSONObject(MovieJsonStr);
            JSONArray movieLists = MovieJson.getJSONArray("results");
            Log.v("Length: ",String.valueOf(movieLists.length()));

            for (int i = 0; i < movieLists.length(); i++) {

                JSONObject jMovieDetails = movieLists.getJSONObject(i);
                String movie_name = jMovieDetails.getString("original_title");
                String poster_path = jMovieDetails.getString("poster_path");
                poster_path = "https://image.tmdb.org/t/p/w185" + poster_path;
                //Log.v("Poster: ", poster_path);
                int id = jMovieDetails.getInt("id");
                String overview = jMovieDetails.getString("overview");
                String release_date = jMovieDetails.getString("release_date");
                String popularity = jMovieDetails.getString("popularity");
                String vote_count = jMovieDetails.getString("vote_count");
                String vote_average = jMovieDetails.getString("vote_average");


                // Set the data in the ContentValues to insert into the database.
                // As it is being directly inserted, we dont need a bean class.
                ContentValues values = new ContentValues();
                values.put(MoviesEntry._ID, id);
                values.put(MoviesEntry.COLUMN_MOVIE_NAME, movie_name);
                values.put(MoviesEntry.COLUMN_POSTER_PATH, poster_path);
                values.put(MoviesEntry.COLUMN_OVERVIEW, overview);
                values.put(MoviesEntry.COLUMN_RELEASE_DATE, release_date);
                values.put(MoviesEntry.COLUMN_POPULARITY, popularity);
                values.put(MoviesEntry.COLUMN_VOTE_COUNT, vote_count);
                values.put(MoviesEntry.COLUMN_VOTE_AVERAGE, vote_average);

                if (sortBy.equals("Popularity")) {
                    values.put(MoviesEntry.COLUMN_POPULAR, 1);
                    String[] check = {MoviesEntry.COLUMN_MOVIE_NAME, MoviesEntry.COLUMN_POPULAR};
                    String selection = MoviesEntry._ID + "=?";
                    String[] args = {String.valueOf(id)};
                    Cursor cursor = getContext().getContentResolver().query(MoviesEntry.CONTENT_URI, check, selection, args, null);
                    if(cursor.getCount()!=0){
                        cursor.moveToFirst();
                        if(cursor.getInt(cursor.getColumnIndex(MoviesEntry.COLUMN_POPULAR)) != 1){
                            getContext().getContentResolver().update(MoviesEntry.CONTENT_URI, values, selection, args);
                        }
                    }else{
                        Uri temp = getContext().getContentResolver().insert(MoviesEntry.CONTENT_URI, values);
                        Log.v("URI", temp.toString());
                    }
                } else {
                    values.put(MoviesEntry.COLUMN_TOP_RATED, 1);
                    String[] check = {MoviesEntry.COLUMN_MOVIE_NAME, MoviesEntry.COLUMN_TOP_RATED};
                    String selection = MoviesEntry._ID + "=?";
                    String[] args = {String.valueOf(id)};
                    Cursor cursor = getContext().getContentResolver().query(MoviesEntry.CONTENT_URI, check, selection, args, null);
                    if(cursor.getCount()!=0){
                        cursor.moveToFirst();
                        if(cursor.getInt(cursor.getColumnIndex(MoviesEntry.COLUMN_TOP_RATED)) != 1){
                            getContext().getContentResolver().update(MoviesEntry.CONTENT_URI, values, selection, args);
                        }
                    }else{
                        Uri temp = getContext().getContentResolver().insert(MoviesEntry.CONTENT_URI, values);
                        Log.v("URI", temp.toString());
                    }
                }
            }
        }
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = "";
        String[] sArgs =  {String.valueOf(1)};
        if(sortBy.equals("Favourites")) {
            selection= MoviesEntry.COLUMN_FAVOURITES + "=?";
        }
        else if(sortBy.equals("Popularity")) {
            selection = MoviesEntry.COLUMN_POPULAR + "=?";
        } else {
            selection = MoviesEntry.COLUMN_TOP_RATED + "=?";
        }
        return new CursorLoader(getActivity(), MoviesEntry.CONTENT_URI, projection, selection, sArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            displayType.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}
