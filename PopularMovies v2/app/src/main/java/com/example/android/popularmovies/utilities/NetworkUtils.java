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

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/";
    private static final String SORT_POPULAR = "movie/popular";
    private static final String SORT_TOP_RATED = "movie/top_rated";
    private static final String MOVIES_API = "";
    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";

    final static String API_PARAM = "api_key";
    final static String PAGE_PARAM = "page";

    public static URL buildUrl(String sortParam, String page) {
        String moviesUrl;

        if (sortParam.equals("popular")) {
            moviesUrl = MOVIES_BASE_URL + SORT_POPULAR;
        }else if (sortParam.equals("top_rated")){
            moviesUrl = MOVIES_BASE_URL + SORT_TOP_RATED;
        }else return null;

        Uri builtUri = Uri.parse(moviesUrl).buildUpon()
                .appendQueryParameter(API_PARAM, MOVIES_API)
                .appendQueryParameter(PAGE_PARAM, page)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildUrlWithID(String id, String param) {
        String moviesUrl;
        switch (param) {
            case "trailer":
                moviesUrl = MOVIES_BASE_URL + "movie/" + id + "/videos";
                break;
            case "reviews":
                moviesUrl = MOVIES_BASE_URL + "movie/" + id + "/reviews";
                break;
            default:
                return null;
        }


        Uri builtUri = Uri.parse(moviesUrl).buildUpon()
                .appendQueryParameter(API_PARAM, MOVIES_API)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildYoutubeUrl(String youtubeMovieKey) {
        String youtubeURLWithKey;

        youtubeURLWithKey = YOUTUBE_BASE_URL + youtubeMovieKey;

        URL url = null;
        try {
            url = new URL(youtubeURLWithKey.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;

    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) {
                InputStream in = urlConnection.getInputStream();

                Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");
                boolean hasInput = scanner.hasNext();
                if (hasInput) {
                    return scanner.next();
                } else {
                    return null;
                }
            }else{
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
