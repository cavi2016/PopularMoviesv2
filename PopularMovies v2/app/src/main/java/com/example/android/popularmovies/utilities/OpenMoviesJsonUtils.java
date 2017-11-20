/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.popularmovies.utilities;

import android.content.Context;

import com.example.android.popularmovies.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public final class OpenMoviesJsonUtils {

    public static ArrayList<Movie> getMoviesStringsFromJson(Context context, String moviesJsonStr)
            throws JSONException {

        final String TMDB_RESULTS = "results";
        final String TMDB_MOVIE_ID = "id";
        final String TMDB_TITLE = "title";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_IMAGE_PATH = "poster_path";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_VOTE_AVERAGE = "vote_average";

        ArrayList<Movie> moviesData = new ArrayList<>();

        JSONObject moviesJson = new JSONObject(moviesJsonStr);

        JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

        for (int i = 0; i < moviesArray.length(); i++) {
            String movieId;
            String title;
            String overview;
            String imagePath;
            String releaseDate;
            String voteAverage;

            JSONObject movieJSONOBject = moviesArray.getJSONObject(i);

            movieId = movieJSONOBject.getString(TMDB_MOVIE_ID);
            title = movieJSONOBject.getString(TMDB_TITLE);
            overview = movieJSONOBject.getString(TMDB_OVERVIEW);
            imagePath = movieJSONOBject.getString(TMDB_IMAGE_PATH);
            releaseDate = movieJSONOBject.getString(TMDB_RELEASE_DATE);
            voteAverage = movieJSONOBject.getString(TMDB_VOTE_AVERAGE);

            moviesData.add(new Movie(title, movieId, imagePath, overview, voteAverage, releaseDate));
        }

        return moviesData;
    }

    public static ArrayList<String> getMoviesInfoStringFromJson(Context context, String moviesJsonStr, String param)
            throws JSONException {
        final String TMDB_RESULTS = "results";
        final String TMDB_TRAILER_KEY = "key";
        final String TMDB_REVIEW = "content";

        JSONObject moviesJson = new JSONObject(moviesJsonStr);

        JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

        if (moviesArray.length() != 0) {
            if (param.equals("trailers")) {
                ArrayList<String> trailers = new ArrayList<>();
                JSONObject movieJSONOBject;

                for (int i = 0; i < moviesArray.length(); i++) {
                    movieJSONOBject = moviesArray.getJSONObject(i);
                    trailers.add(movieJSONOBject.getString(TMDB_TRAILER_KEY));
                }
                return trailers;
            }else {
                JSONObject movieJSONOBject = moviesArray.getJSONObject(0);
                ArrayList<String> review = new ArrayList<>();
                review.add(movieJSONOBject.getString(TMDB_REVIEW));
                return review ;
            }
        } else
            return null;
    }
}