package com.example.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Movie implements Parcelable{

    private String title;
    private String movieId;
    private String posterString;
    private String synopsis;
    private String userRating;
    private String releaseDate;

    private final static String baseUrl = "http://image.tmdb.org/t/p/w185/";

    public Movie(String title, String movieId, String imagePath, String synopsis, String voteAverage, String releaseDate) {
        this.title = title;
        this.movieId = movieId;
        this.posterString = imagePath;
        this.synopsis = synopsis;
        this.releaseDate = releaseDate;
        this.userRating = formatRating(voteAverage);
    }

    public String formatUrlPoster(String imagePath) { return baseUrl + imagePath;
    }

    public String getYear(String date) {
        DateFormat df = new SimpleDateFormat("yyyy-mm-dd", Locale.US);
        try {
            Date result = df.parse(date);
            //Date date; // your date
            Calendar cal = Calendar.getInstance();
            cal.setTime(result);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            return String.valueOf(year);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String formatRating(String voteAverage) {
        return voteAverage + "/10";
    }

    public String getMovieId() {return movieId;}

    public void setMovieId(String movieId) {this.movieId = movieId;}

    public String getTitle() {
        return title;
    }

    public void setTitle(String originalTitle) {
        this.title = title;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getPosterString() {
        return posterString;
    }

    public void setPosterString(String imageUrlString) {
        this.posterString = posterString;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getUserRating() {
        return userRating;
    }

    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movieId);
        dest.writeString(title);
        dest.writeString(synopsis);
        dest.writeString(posterString);
        dest.writeString(releaseDate);
        dest.writeString(userRating);
    }

    private Movie(Parcel in) {
        movieId = in.readString();
        title = in.readString();
        synopsis = in.readString();
        posterString = in.readString();
        releaseDate = in.readString();
        userRating = in.readString();
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
