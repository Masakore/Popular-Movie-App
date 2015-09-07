package com.masakorelab.app.popularmoviesapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.masakorelab.app.popularmoviesapp.data.MovieContract;


public class MovieFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private static final int MOVIE_LOADER = 0;
    private MovieAdapter mMovieAdapter;

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

        if (savedInstanceState == null) {
            mMovieAdapter = new MovieAdapter(getActivity(), null, 0);
            updateMovieData();
        }

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movie);
        gridView.setAdapter(mMovieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                int COL_MOVIE_ID = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                String movie_ID = cursor.getString(COL_MOVIE_ID);
                if (cursor != null) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, movie_ID)
                            .setData(MovieContract.MovieEntry.CONTENT_URI);
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    private void updateMovieData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = sharedPreferences.getString(getString(R.string.pref_sort_order_key), getString(R.string.pref_sort_order_default));
        FetchMovieTask fetchMovieTask = new FetchMovieTask(getActivity());
        fetchMovieTask.execute(sortOrder);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onSortOrderChange() {
        updateMovieData();
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String pref = Utility.getPreferredSortOrder(getActivity());
        String sort = null;
        if (pref.equals(getString(R.string.pref_sort_order_popularity_desc))) {
            sort = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        } else if (pref.equals(getString(R.string.pref_sort_order_vote_average_desc))) {
            sort = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
        }

        return new CursorLoader(getActivity(),
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                sort);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }
}
