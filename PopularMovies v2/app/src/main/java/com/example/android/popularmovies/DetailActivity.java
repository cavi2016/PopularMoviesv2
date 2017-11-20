package com.example.android.popularmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.data.FavoriteMoviesContract;
import com.example.android.popularmovies.utilities.NetworkUtils;
import com.example.android.popularmovies.utilities.OpenMoviesJsonUtils;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DetailActivity extends AppCompatActivity {
    private Movie mMovie;

    @InjectView(R.id.tv_title)
    TextView mTitle;
    @InjectView(R.id.movie_image)
    ImageView mImage;
    @InjectView(R.id.movie_year)
    TextView mYear;
    @InjectView(R.id.movie_rating)
    TextView mRating;
    @InjectView(R.id.movie_overview)
    TextView mOverview;
    @InjectView(R.id.add_favorite_movie)
    Button mButton;
    @InjectView(R.id.movie_trailer_button)
    Button mTrailer;
    @InjectView(R.id.movie_review)
    TextView mReview;

    private Uri movieUri;
    private URL youtubeMovieUrl;

    @InjectView(R.id.list)
    ListView listView;

    private static final String TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.inject(this);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
                mMovie = intentThatStartedThisActivity.getParcelableExtra(Intent.EXTRA_TEXT);

                mTitle.setText(mMovie.getTitle());
                Picasso.with(getApplicationContext()).load(mMovie.formatUrlPoster(mMovie.getPosterString()))
                        .into(mImage);
                mYear.setText(mMovie.getYear(mMovie.getReleaseDate()));
                mRating.setText(mMovie.getUserRating());
                mOverview.setText(mMovie.getSynopsis());

                movieUri = FavoriteMoviesContract.FavoriteMoviesEntry.CONTENT_URI.buildUpon().
                        appendPath(String.valueOf(mMovie.getMovieId())).build();

                if (queryFavoriteMovie()) {
                    mButton.setBackground(getResources().getDrawable(R.drawable.ic_dialog_yellow_star));
                } else {
                    mButton.setBackground(getResources().getDrawable(R.drawable.ic_dialog_white_star));
                }

                new FetchMovieInfoTask().execute();
            }
        }
    }

    public class FetchMovieInfoTask extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... param) {
            URL reviewsMovieRequestUrl = NetworkUtils.buildUrlWithID(mMovie.getMovieId(), "reviews");

            URL trailerMovieRequestUrl = NetworkUtils.buildUrlWithID(mMovie.getMovieId(), "trailer");
            Log.v(TAG, "trailerUrl" + trailerMovieRequestUrl);

            try {
                String jsonReviewsMoviesResponse = NetworkUtils
                        .getResponseFromHttpUrl(reviewsMovieRequestUrl);

                String review = OpenMoviesJsonUtils
                        .getMoviesInfoStringFromJson(getApplicationContext(), jsonReviewsMoviesResponse, "review").get(0);

                String jsonTrailerMovieResponse = NetworkUtils
                        .getResponseFromHttpUrl(trailerMovieRequestUrl);

                ArrayList<String> trailerKey = OpenMoviesJsonUtils
                        .getMoviesInfoStringFromJson(getApplicationContext(), jsonTrailerMovieResponse, "trailers");

                ArrayList<String> movieInfo = new ArrayList<>();
                movieInfo.add(review);
                movieInfo.addAll(trailerKey);

                return movieInfo;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> parameter) {
            if (parameter != null) {
                mReview.setText(parameter.get(0));
                parameter.remove(0);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_list_item_1, parameter);

                for (int i = 0; i< parameter.size();i++) {
                    Log.v(TAG, "parameter " + parameter.get(i));
                }
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Context context = view.getContext();

                        // ListView Clicked item index
                        int itemPosition     = position;

                        // ListView Clicked item value
                        String  itemValue    = (String) listView.getItemAtPosition(position);

                        // Show Alert
                        Toast.makeText(getApplicationContext(),
                                "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                                .show();
                    }
                });

                //youtubeMovieUrl = NetworkUtils.buildYoutubeUrl(parameter[0]);

            }
        }
    }



    public void playTrailer(View v) {
        Uri uri = Uri.parse(youtubeMovieUrl.toString());

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString()));
        startActivity(browserIntent);
    }

    public void favoriteMovie(View v) {
        if (queryFavoriteMovie()) {
            removeFavorite();
            mButton.setBackground(getResources().getDrawable(R.drawable.ic_dialog_white_star));
        } else {
            insertFavorite();
            mButton.setBackground(getResources().getDrawable(R.drawable.ic_dialog_yellow_star));
        }

        finish();
    }

    private boolean queryFavoriteMovie() {
        ContentResolver moviesContentResolver = this.getContentResolver();
        Cursor c = moviesContentResolver.query(
                movieUri,
                new String[]{FavoriteMoviesContract.FavoriteMoviesEntry._ID},
                null,
                null,
                null);

        if (c.getCount() == 0) {
            return false;
        } else {
            return true;
        }
    }

    private void insertFavorite() {
        ContentResolver moviesContentResolver = this.getContentResolver();
        ContentValues movieValues = new ContentValues();
        movieValues.put(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_TITLE, mMovie.getTitle());
        movieValues.put(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_MOVIE_ID, mMovie.getMovieId());
        movieValues.put(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_MOVIE_POSTER, mMovie.getPosterString());
        movieValues.put(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_SYNOPSIS, mMovie.getSynopsis());
        movieValues.put(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_USER_RATING, mMovie.getUserRating());
        movieValues.put(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());

        if (movieValues != null && movieValues.size() != 0) {
            moviesContentResolver.insert(FavoriteMoviesContract.FavoriteMoviesEntry.CONTENT_URI,
                    movieValues);
        }
    }

    private void removeFavorite() {
        ContentResolver moviesContentResolver = this.getContentResolver();
        moviesContentResolver.delete(movieUri, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType(getString(R.string.text_plain));
            String shareBody = getString(R.string.watch_trailer) + youtubeMovieUrl.toString();
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mMovie.getTitle() + getString(R.string.movie_trailer));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
        }

        return super.onOptionsItemSelected(item);
    }
}