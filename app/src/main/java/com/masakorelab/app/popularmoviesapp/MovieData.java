package com.masakorelab.app.popularmoviesapp;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class MovieData implements Parcelable{

    public MovieData() {}

    //the contents of movies
    private String title;
    private String release_date;
    private String movie_poster_path;
    private String vote_average;
    private String synopsis;

    // parcel keys
    private static final String KEY_TITLE = "title";
    private static final String KEY_RELEASE_DATE = "release_date";
    private static final String KEY_POSTER_PATH = "poster_path";
    private static final String KEY_VOTE_AVERAGE = "vote_average";
    private static final String KEY_SYNOPSIS = "sysnopsis";


    //Setter
    public void setTitle(String title) {
        this.title = title;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public void setMovie_poster_path(String movie_poster_path) {
        this.movie_poster_path = movie_poster_path;
    }

    public void setVote_average(String vote_average) {
        this.vote_average = vote_average;
    }

    public void setSynopisis(String synopsis) {
        this.synopsis = synopsis;
    }

    // getter
    public String getTitle() {
        return title;
    }

    public String getRelease_date() {
        return release_date;
    }

    public String getMovie_poster_path() {
        return movie_poster_path;
    }

    public String getVote_average() { return vote_average; }

    public String getSynopsis() {
        return synopsis;
    }

    // Parcel Part
    // (Reference)http://www.easyinfogeek.com/2014/01/android-tutorial-two-methods-of-passing.html
    public static final Parcelable.Creator<MovieData> CREATOR = new Parcelable.Creator<MovieData>() {

        @Override
        public MovieData createFromParcel(Parcel source) {
            Bundle bundle = source.readBundle();

            MovieData mMovieData = new MovieData();
            mMovieData.title = bundle.getString(KEY_TITLE);
            mMovieData.release_date = bundle.getString(KEY_RELEASE_DATE);
            mMovieData.movie_poster_path = bundle.getString(KEY_POSTER_PATH);
            mMovieData.vote_average = bundle.getString(KEY_VOTE_AVERAGE);
            mMovieData.synopsis = bundle.getString(KEY_SYNOPSIS);

            return mMovieData;
        }

        @Override
        public MovieData[] newArray(int size) {
            return new MovieData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // create a bundle for the key value pairs
        Bundle bundle = new Bundle();

        bundle.putString(KEY_TITLE, title);
        bundle.putString(KEY_RELEASE_DATE, release_date);
        bundle.putString(KEY_POSTER_PATH, movie_poster_path);
        bundle.putString(KEY_VOTE_AVERAGE, vote_average);
        bundle.putString(KEY_SYNOPSIS, synopsis);

        dest.writeBundle(bundle);
    }
}
