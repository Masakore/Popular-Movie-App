package com.masakorelab.app.popularmoviesapp;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieData implements Parcelable{

    public MovieData() {}

    //the contents of movies
    private String title;
    private String release_date;
    private String movie_poster_path;
    private String vote_average;
    private String synopisis;

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

    public void setSynopisis(String synopisis) {
        this.synopisis = synopisis;
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

    public String getSynopisis() {
        return synopisis;
    }

    // Parcel Part
    // (Reference)http://www.easyinfogeek.com/2014/01/android-tutorial-two-methods-of-passing.html
    public static final Parcelable.Creator<MovieData> CREATOR = new Parcelable.Creator<MovieData>() {

        @Override
        public MovieData createFromParcel(Parcel source) {
            MovieData mMovieData = new MovieData();
            mMovieData.title = source.readString();
            mMovieData.release_date = source.readString();
            mMovieData.movie_poster_path = source.readString();
            mMovieData.vote_average = source.readString();
            mMovieData.synopisis = source.readString();

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
        dest.writeString(title);
        dest.writeString(release_date);
        dest.writeString(movie_poster_path);
        dest.writeString(vote_average);
        dest.writeString(synopisis);
    }
}
