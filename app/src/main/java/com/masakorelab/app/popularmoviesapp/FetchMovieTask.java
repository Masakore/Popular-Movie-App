package com.masakorelab.app.popularmoviesapp;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.masakorelab.app.popularmoviesapp.data.MovieContract.MovieEntry;

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
import java.util.Vector;

public class FetchMovieTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        private final Context mContext;

        FetchMovieTask(Context context) {
            mContext = context;
        }

        boolean isMovieIdExist (String movieID) {
            boolean result = false;
            Cursor movieIdCursor = mContext.getContentResolver().query(
                    MovieEntry.CONTENT_URI,
                    new String[]{MovieEntry._ID},
                    MovieEntry.COLUMN_MOVIE_ID + " =?",
                    new String[]{movieID},
                    null
            );
            if (movieIdCursor.moveToFirst()) {
                result = true;
            }
            movieIdCursor.close();
            return result;
        }

        private void getMovieDataFromJson(String movieJsonStr) throws JSONException {
            final String RESULTS = "results";
            final String ID = "id";
            final String TITLE = "original_title";
            final String RELEASE_DATE = "release_date";
            final String MOVIE_POSTER_PATH = "poster_path";
            final String VOTE_AVERAGE = "vote_average";
            final String SYNOPSIS = "overview";
            final String POPULARITY = "popularity";

            //Formatting for Movie Poster Thumbnail
            final String MOVIE_POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
            final String MOVIE_POSTER_SIZE = "w185";

            //Formatting for Movie Trailer and User Reviews
            final String MOVIE_BASE_URL_FOR_DETAIL = "http://api.themoviedb.org/3/movie/";
            final String MOVIE_TRAILER = "/videos";
            final String MOVIE_REVIEWS = "/reviews";

            try {
                JSONObject movieJson = new JSONObject(movieJsonStr);
                JSONArray movieArray = movieJson.getJSONArray(RESULTS);

                Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());

                for (int i = 0; i < movieArray.length(); i++) {
                    // These are the values that will be collected.
                    String movieID;
                    String title;
                    String poster_path;
                    String release_date;
                    Double vote_average;
                    String plot_synopsis;
                    String trailer_path;
                    String user_reviews;
                    Double popularity;

                    JSONObject results = movieArray.getJSONObject(i);

                    movieID = results.optString(ID, "");

                    //Check if already exists or not.
                    if (!isMovieIdExist(movieID)) {
                        title = results.optString(TITLE, "");
                        poster_path = MOVIE_POSTER_BASE_URL + MOVIE_POSTER_SIZE +
                                results.optString(MOVIE_POSTER_PATH, "");
                        release_date = results.optString(RELEASE_DATE, "");
                        vote_average = results.getDouble(VOTE_AVERAGE);
                        plot_synopsis = results.optString(SYNOPSIS, "");
                        trailer_path = MOVIE_BASE_URL_FOR_DETAIL + movieID + MOVIE_TRAILER;
                        user_reviews = MOVIE_BASE_URL_FOR_DETAIL + movieID + MOVIE_REVIEWS;
                        popularity = results.getDouble(POPULARITY);

                        ContentValues movieValues = new ContentValues();
                        movieValues.put(MovieEntry.COLUMN_MOVIE_ID, movieID);
                        movieValues.put(MovieEntry.COLUMN_TITLE, title);
                        movieValues.put(MovieEntry.COLUMN_POSTER_PATH, poster_path);
                        movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, release_date);
                        movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, vote_average);
                        movieValues.put(MovieEntry.COLUMN_PLOT_SYNOPSIS, plot_synopsis);
                        movieValues.put(MovieEntry.COLUMN_TRAILER_PATH, trailer_path);
                        movieValues.put(MovieEntry.COLUMN_USER_REVIEWS, user_reviews);
                        movieValues.put(MovieEntry.COLUMN_POPULARITY, popularity);

                        cVVector.add(movieValues);
                    }
                }

                int inserted = 0;
                //add to database
                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    inserted = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);
                }

                Log.d(LOG_TAG, "FetchMovieTask Complete." + inserted + " Inserted");

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(String... params) {

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
                // Construct the URL for the themoviedb discovery query
                final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/discover/movie?";
                final String SORT_ORDER = "sort_by";
                final String API_KEY = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY, api_key)
                        .appendQueryParameter(SORT_ORDER, params[0])
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
                getMovieDataFromJson(movieJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "err: ", e);
                e.printStackTrace();
                return null;
            } catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
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
            return null;
        }
}
