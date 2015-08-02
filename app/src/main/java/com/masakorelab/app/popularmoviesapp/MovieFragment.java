package com.masakorelab.app.popularmoviesapp;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MovieFragment extends Fragment {

    private ImageAdapter mImageAdapter;
    private ArrayList<MovieData> mMovieData;

    // Set the key of mImageAdapter for saving bundle
    //public final static String

    // Set the key for Pracelable implementation
    public  final static String PAR_KEY = "com.masakorelab.objectPass.par";

    //Constructor
    public MovieFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        if (savedInstanceState != null) {
            mMovieData = savedInstanceState.getParcelableArrayList(PAR_KEY);
            mImageAdapter = new ImageAdapter(getActivity(), mMovieData);
        } else {
            mImageAdapter = new ImageAdapter(getActivity(), new ArrayList<MovieData>());
        }

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movie_poster);
        gridView.setAdapter(mImageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieData movieData = mImageAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putParcelable(PAR_KEY, movieData);
                intent.putExtras(mBundle);
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(PAR_KEY, mMovieData);
        super.onSaveInstanceState(outState);
    }

    private void updateMovieData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = sharedPreferences.getString(getString(R.string.pref_sort_order_key), getString(R.string.pref_sort_order_default));
        FetchMovieDataTask movieDataTask = new FetchMovieDataTask();
        movieDataTask.execute(sortOrder);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMovieData.isEmpty()) {
            updateMovieData();
        }
        if (SettingsActivity.PREFERENCE_CHANGED) {
            updateMovieData();
        }
    }

    public class ImageAdapter extends ArrayAdapter<MovieData> {
        private Context mContext;

        public ImageAdapter(Context context, ArrayList<MovieData> movieData) {
            super(context, 0, movieData);
            mMovieData = movieData;
            mContext = context;
        }

        @Override
        public void add(MovieData object) {
            super.add(object);
        }

        @Override
        public int getCount() {
            return mMovieData.size();
        }

        @Override
        public MovieData getItem(int position) {
            return mMovieData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
            } else {
                imageView = (ImageView) convertView;
            }

            MovieData movieDatas = getItem(position);
            String url = movieDatas.getMovie_poster_path();

            Picasso.with(mContext).load(url).into(imageView);
            return imageView;
        }
    }


    // Fetch Data through TMDB API
    public class FetchMovieDataTask extends AsyncTask<String, Void, List<MovieData>> {

        private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();

        private List<MovieData> getMovieDataFromJson(String movieJsonStr) throws JSONException {
            final String RESULTS = "results";
            final String ID = "id";
            final String TITLE = "original_title";
            final String RELEASE_DATE = "release_date";
            final String MOVIE_POSTER_PATH = "poster_path";
            final String VOTE_AVERAGE = "vote_average";
            final String SYNOPISIS = "overview";

            //Formatting for Movie Poster Thumbnail
            final String MOVIE_POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
            final String MOVIE_POSTER_SIZE = "w185/";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(RESULTS);

            List<MovieData> movieDatas = new ArrayList<MovieData>();

            for(int i =0; i < movieArray.length(); i++) {
                JSONObject results = movieArray.getJSONObject(i);
                MovieData movieData = new MovieData();

                //Null Check before setting
                if (results.getString(TITLE) != null) {
                    movieData.setTitle(results.getString(TITLE));
                } else {
                    movieData.setTitle("");
                }

                if (results.getString(RELEASE_DATE) != null) {
                    movieData.setRelease_date(results.getString(RELEASE_DATE));
                } else {
                    movieData.setRelease_date("");
                }

                if (results.getString(MOVIE_POSTER_PATH) != null) {
                    //Setting movie poster path to MovieData Object
                    movieData.setMovie_poster_path(MOVIE_POSTER_BASE_URL + MOVIE_POSTER_SIZE +
                            results.getString(MOVIE_POSTER_PATH));
                } else {
                    movieData.setMovie_poster_path("");
                }

                if (results.getString(VOTE_AVERAGE) != null) {
                    movieData.setVote_average(results.getString(VOTE_AVERAGE));
                } else {
                    movieData.setVote_average("");
                }

                if (results.getString(SYNOPISIS) != null) {
                    movieData.setSynopisis(results.getString(SYNOPISIS));
                } else {
                    movieData.setSynopisis("");
                }

                movieDatas.add(movieData);
            }
            return movieDatas;
        }

        @Override
        protected List<MovieData> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            /*
            * Please Obtain API KEY from https://www.themoviedb.org/documentation/api
            */
            String api_key = "";

            try {
                // Construct the URL for the themoviedb query
                final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/discover/movie?";
                final String SORT_ORDER = "sort_by";
                final String API_KEY = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY, api_key)
                        .appendQueryParameter(SORT_ORDER, params[0])//params are set when execute
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to the themoviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

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
                    return null;
                }
                movieJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "err: ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream: ", e);
                    }
                }
            }
            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MovieData> result) {
            if (result != null) {
                mImageAdapter.clear();
                for (MovieData movieData: result) {
                    mImageAdapter.add(movieData);
                }
            }
        }
    }
}
