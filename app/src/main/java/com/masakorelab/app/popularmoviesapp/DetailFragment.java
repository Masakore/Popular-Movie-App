package com.masakorelab.app.popularmoviesapp;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.masakorelab.app.popularmoviesapp.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */

public class DetailFragment extends Fragment implements LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAILFRAGMENT_URI = "DFURI";
    static final String MOVIEID = "MOVIEID";


    //private static final String MOVIE_SHARE_HASHTAG;
    //private ShareActionProvider mShareActionProvider;

    private ArrayAdapter mTrailerAdapter;
    private ArrayAdapter mReviewAdapter;
    private Uri mUri;
    private String mMovieId;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    private static final int DETAIL_LOADER = 1;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS,
            MovieContract.MovieEntry.COLUMN_TRAILER_PATH,
            MovieContract.MovieEntry.COLUMN_USER_REVIEWS
    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_MOVIE_ID = 1;
    private static final int COL_MOVIE_TITLE = 2;
    private static final int COL_MOVIE_POSTER = 3;
    private static final int COL_MOVIE_RELEASE = 4;
    private static final int COL_MOVIE_VOTE = 5;
    private static final int COL_MOVIE_PLOT = 6;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAILFRAGMENT_URI);
            mMovieId = arguments.getString(DetailFragment.MOVIEID);
        } else {
            Intent intent = getActivity().getIntent();
            mMovieId = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        onFetchIndMovie(getActivity(),mMovieId);

        mTrailerAdapter = new TrailerAdapter(getActivity(), new ArrayList<Trailers>());
        mReviewAdapter = new ReviewAdapter(getActivity(), new ArrayList<Reviews>());

        return rootView;
    }

    private void onFetchIndMovie(Context context, String movie_ID) {
        FetchIndividualData fetchIndividualData = new FetchIndividualData(context);
        fetchIndividualData.execute(movie_ID);
    }

    private class TrailerAdapter extends ArrayAdapter<Trailers> {
        Context mContext;

        public TrailerAdapter(Context context, ArrayList<Trailers> objects) {
            super(context, 0, objects);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_movie_trailer, parent, false);
            }
            TextView title = (TextView) convertView.findViewById(R.id.list_movie_trailers_textview);
            title.setText(getItem(position).getTrailerName());
            return convertView;
        }
    }

    private class ReviewAdapter extends ArrayAdapter<Reviews> {
        Context mContext;

        public ReviewAdapter(Context context, ArrayList<Reviews> objects) {
            super(context, 0, objects);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_movie_reviews, parent, false);
            }
            TextView author = (TextView) convertView.findViewById(R.id.list_item_reviews_author_textview);
            author.setText(getItem(position).getAuthor());
            TextView content = (TextView) convertView.findViewById(R.id.list_item_reviews_textview);
            content.setText(getItem(position).getContent());
            return convertView;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareMovieIntent());
        } else {
            Log.d(LOG_TAG, "Share action provider is null?");
        }
    }

    private Intent createShareMovieIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
        if ( null != mUri) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    MOVIE_COLUMNS,
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + " =?",
                    new String[]{mMovieId},
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) {
            return;
        }
        // The detail Activity called via intent.  Inspect the intent for forecast data.
        //Set View items
        TextView textView_title =(TextView) getView().findViewById(R.id.detail_title);
        textView_title.setText(data.getString(COL_MOVIE_TITLE));

        ImageView imageView_poster = (ImageView) getView().findViewById(R.id.detail_poster);
        Picasso.with(getActivity()).load(data.getString(COL_MOVIE_POSTER)).into(imageView_poster);

        TextView textView_release_date =(TextView) getView().findViewById(R.id.detail_release_date);
        textView_release_date.setText(data.getString(COL_MOVIE_RELEASE));

        TextView textView_vote_average =(TextView) getView().findViewById(R.id.detail_vote_average);
        textView_vote_average.setText(data.getDouble(COL_MOVIE_VOTE) + "/10.0");

        TextView textView_plot_synopsis =(TextView) getView().findViewById(R.id.detail_plot_synopsis);
        textView_plot_synopsis.setText(data.getString(COL_MOVIE_PLOT));

        ListView listView_reviews = (ListView) getView().findViewById(R.id.detail_reviews);
        listView_reviews.setAdapter(mReviewAdapter);

        ListView listView_trailers = (ListView) getView().findViewById(R.id.detail_trailers);
        listView_trailers.setAdapter(mTrailerAdapter);
        listView_trailers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Trailers trailers = (Trailers) mTrailerAdapter.getItem(position);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailers.getYoutubePath()));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Please install Youtube app", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button markAsFavoriteBttn = (Button) getView().findViewById(R.id.detail_favorite_bttn);
        markAsFavoriteBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SharedPref exist? -> Already the Movie ID added? ->

                SharedPreferences sharedPref =
                        getActivity().getSharedPreferences(getString(R.string.pref_idfile_key), getActivity().MODE_PRIVATE);
                Set<String> movieIdList = sharedPref.getStringSet(getString(R.string.pref_idfile_filename), null);
                SharedPreferences.Editor editor = sharedPref.edit();
                Intent intent = getActivity().getIntent();

                if (movieIdList == null) {
                    Set<String> set = new HashSet<String>();
                    set.add(intent.getStringExtra(Intent.EXTRA_TEXT));
                    editor.putStringSet(getString(R.string.pref_idfile_filename), set);
                    editor.commit();
                }

                if (movieIdList.contains(intent.getStringExtra(Intent.EXTRA_TEXT))) {
                    Toast.makeText(getActivity(), "Already Exist!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    movieIdList.add(intent.getStringExtra(Intent.EXTRA_TEXT));
                    editor.clear();
                    editor.putStringSet(getString(R.string.pref_idfile_filename), movieIdList);
                    editor.commit();
                    Toast.makeText(getActivity(), "Added!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {


    }

    public class FetchIndividualData extends AsyncTask<String, Void, List<List>> {

        private final String LOG_TAG = FetchIndividualData.class.getSimpleName();

        private final Context mContext;

        FetchIndividualData(Context context) {mContext = context;}

        private List<List> getIndividualMovieDataFromJson(String indMovieJsonStr) {
            //For trailers
            final String TRAILERS = "trailers";
            final String NAME = "name";
            final String YOUTUBE = "youtube";
            final String YOUTUBE_SRC = "source";


            //For reviews
            final String REVIEWS = "reviews";
            final String RESULTS = "results";
            final String AUTHOR = "author";
            final String CONTENT = "content";

            try {

                JSONObject jsonObject = new JSONObject(indMovieJsonStr);

                //For trailers
                JSONObject trailerJson = jsonObject.getJSONObject(TRAILERS);
                JSONArray jsonYoutubeArray = trailerJson.getJSONArray(YOUTUBE);
                List<Trailers> trailerList = new ArrayList<Trailers>();
                Trailers trailers = null;

                if (jsonYoutubeArray.length() != 0) {
                    for (int i = 0; i < jsonYoutubeArray.length(); i++) {
                        JSONObject results = jsonYoutubeArray.getJSONObject(i);
                        trailers = new Trailers(results.optString(NAME,""), results.optString(YOUTUBE_SRC, ""));
                        trailerList.add(trailers);
                    }
                }

                //For reviews
                JSONObject reviewsJson = jsonObject.getJSONObject(REVIEWS);
                JSONArray jsonReviewArray = reviewsJson.getJSONArray(RESULTS);
                List<Reviews> reviewList = new ArrayList<Reviews>();
                Reviews reviews;

                if (jsonReviewArray.length() != 0) {

                    for (int i = 0; i < jsonReviewArray.length(); i++) {
                        JSONObject results = jsonReviewArray.getJSONObject(i);
                        String test = results.optString(AUTHOR, "");
                        reviews = new Reviews(results.optString(AUTHOR, ""), results.optString(CONTENT, ""));
                        reviewList.add(reviews);
                    }
                }

                List<List> lists = new ArrayList<>();
                lists.add(trailerList);
                lists.add(reviewList);

                return lists;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected List<List> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String indMovieJsonStr = null;

            String api_key = "409a18458e8fb71e7569779b711c38f9";

            //Test Id=76341
            //http://api.themoviedb.org/3/movie/{movie_id}?api_key=your_key&append_to_response=trailers,reviews
            //i.e. http://api.themoviedb.org/3/movie/76341?api_key=409a18458e8fb71e7569779b711c38f9&append_to_response=trailers,reviews
            //https://www.youtube.com/watch?v=FRDdRto_3SA
            //http://api.themoviedb.org/3/discover/movie?api_key=409a18458e8fb71e7569779b711c38f9&append_to_response=trailers,reviews&popularity.desc

            try {
                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie";
                final String API_KEY = "api_key";
                final String APPEND = "append_to_response";
                final String TRAILERS = "trailers";
                final String REVIEWS = "reviews";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(params[0] + "?")
                        .appendQueryParameter(API_KEY, api_key)
                        .appendQueryParameter(APPEND, TRAILERS + "," + REVIEWS).build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer buffer = new StringBuffer();

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                indMovieJsonStr = buffer.toString();
                return getIndividualMovieDataFromJson(indMovieJsonStr);

            } catch (MalformedURLException e) {
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
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing stream: ", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<List> result) {
            if (result != null) {
                List<Trailers> trailerList = result.get(0);
                List<Reviews> reviewList = result.get(1);
                mTrailerAdapter.clear();
                mReviewAdapter.clear();
                for (Trailers trailers: trailerList) {
                    mTrailerAdapter.add(trailers);
                }

                for (Reviews reviews: reviewList) {
                    mReviewAdapter.add(reviews);
                }
            }
        }
    }

}