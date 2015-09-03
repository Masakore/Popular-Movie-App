package com.masakorelab.app.popularmoviesapp;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Movie;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;

import com.masakorelab.app.popularmoviesapp.data.MovieContract;
import com.squareup.picasso.Picasso;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }  else {
            //Seems this one is not working as expected.
            onBackPressed();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */

    public static class DetailFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>{

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private static final String MOVIE_SHARE_HASHTAG = "";

        private ShareActionProvider mShareActionProvider;
        private MovieData mMovieData;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        private static final int DETAIL_LOADER = 0;

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
        private static final int COL_MOVIE_ID = 0;
        private static final int COL_MOVIE_MID = 1;
        private static final int COL_MOVIE_TITLE = 2;
        private static final int COL_MOVIE_POSTER = 3;
        private static final int COL_MOVIE_RELEASE = 4;
        private static final int COL_MOVIE_VOTE = 5;
        private static final int COL_MOVIE_PLOT = 6;
        private static final int COL_MOVIE_TRAILER = 7;
        private static final int COL_MOVIE_REVIEWS = 8;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_detail, container, false);
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

            //Pleaese share the first trailer video URL
            //Please share the external youtube URL
            //shareIntent.putExtra(Intent.EXTRA_TEXT, );
            return shareIntent;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In onCreateLoader");
            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }

            return new CursorLoader(
                    getActivity(),
                    intent.getData(),
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null
            );
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
                textView_vote_average.setText(data.getDouble(COL_MOVIE_VOTE) + "/10");

                TextView textView_plot_synopsis =(TextView) getView().findViewById(R.id.detail_plot_synopsis);
                textView_plot_synopsis.setText(data.getString(COL_MOVIE_PLOT));



        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
