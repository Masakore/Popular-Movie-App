package com.masakorelab.app.popularmoviesapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.masakorelab.app.popularmoviesapp.data.MovieContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MovieAdapter extends CursorAdapter {

    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public String convertCursorRowToUXFormat(Cursor cursor) {
        int inx_poster_path = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);

        String moviePosterURL = cursor.getString(inx_poster_path);
        return moviePosterURL;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_movie_poster, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view;
        Picasso.with(mContext).load(convertCursorRowToUXFormat(cursor)).into(imageView);
    }
}
