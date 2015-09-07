package com.masakorelab.app.popularmoviesapp;

public class Trailers {
    private String trailerName;
    private String youtubePath;


    public Trailers(String trailerName, String youtubePath){
        this.trailerName = trailerName;
        this.youtubePath = youtubePath;
    }

    public String getYoutubePath() {
        return youtubePath;
    }

    public String getTrailerName() {
        return trailerName;
    }

}
