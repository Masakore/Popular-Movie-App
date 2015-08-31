package com.masakorelab.app.popularmoviesapp;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
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

    public static class DetailFragment extends Fragment {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private MovieData mMovieData;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // The detail Activity called via intent.  Inspect the intent for forecast data.
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(MovieFragment.PAR_KEY)) {
                //Get object by Parcelable Key
                mMovieData = (MovieData)intent.getParcelableExtra(MovieFragment.PAR_KEY);

                //Set View items
                TextView textView_title =(TextView) rootView.findViewById(R.id.detail_title);
                textView_title.setText(mMovieData.getTitle());

                ImageView imageView_poster = (ImageView) rootView.findViewById(R.id.detail_poster);
                Picasso.with(getActivity()).load(mMovieData.getMovie_poster_path()).into(imageView_poster);

                TextView textView_release_date =(TextView) rootView.findViewById(R.id.detail_release_date);
                textView_release_date.setText(mMovieData.getRelease_date());

                TextView textView_vote_average =(TextView) rootView.findViewById(R.id.detail_vote_average);
                textView_vote_average.setText(mMovieData.getVote_average() + "/10");

                TextView textView_plot_synopsis =(TextView) rootView.findViewById(R.id.detail_plot_synopsis);
                textView_plot_synopsis.setText(mMovieData.getSynopsis());

            }
            return rootView;
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
    }
}
