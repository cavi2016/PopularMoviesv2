package com.example.android.popularmovies;

import android.os.Parcelable;
import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.data.FavoriteMoviesContract;
import com.example.android.popularmovies.utilities.NetworkUtils;
import com.example.android.popularmovies.utilities.OpenMoviesJsonUtils;

import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks,
        MoviesAdapter.MoviesAdapterOnClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SORT_PARAMETER = "sort_param";
    private static final String PAGE_PARAMETER = "page_param";
    private static final String LIST_FAVORITE_MOVIES_PARAMETER = "list_favorite_movies_param";
    private static final String SAVED_LAYOUT_MANAGER = "saved_layout_manager";

    private static final int DATABASE_CURSOR_LOADER_ID = 0;
    private static final int WEB_LOADER_ID = 1;

    public static final int INDEX_ID = 0;
    public static final int INDEX_MOVIE_TITLE = 1;
    public static final int INDEX_MOVIE_ID = 2;
    public static final int INDEX_MOVIE_POSTER = 3;
    public static final int INDEX_MOVIE_SYNOPSIS = 4;
    public static final int INDEX_MOVIE_USER_RATING = 5;
    public static final int INDEX_MOVIE_RELEASE_DATE = 6;

    private RecyclerView mRecyclerView;
    private MoviesAdapter mMoviesAdapter;
    private GridLayoutManager layoutManager;

    private TextView mErrorMessageDisplay;

    private static ProgressBar mLoadingIndicator;

    private static String sortParameter;

    ArrayList<Movie> mMoviesData;

    private static int mPage;

    private int mPosition = RecyclerView.NO_POSITION;
    private boolean pageDown;

    private boolean listFavoriteMovies = false;

    private boolean loading = false;
    int pastVisiblesItems;
    int visibleItemCount;
    int totalItemCount;
    int child;

    Parcelable layoutManagerSavedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            sortParameter = savedInstanceState.getString(SORT_PARAMETER);
            listFavoriteMovies = savedInstanceState.getBoolean(LIST_FAVORITE_MOVIES_PARAMETER);
        } else {
            sortParameter = getString(R.string.popular_sort);
            mPage = 1;
        }

        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager = new GridLayoutManager(this, 2, OrientationHelper.VERTICAL, false);
        } else {
            layoutManager = new GridLayoutManager(this, 4, OrientationHelper.VERTICAL, false);
        }


        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mMoviesAdapter = new MoviesAdapter(this);
        mRecyclerView.setAdapter(mMoviesAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
                    totalItemCount = layoutManager.getItemCount();

                    if (!loading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            pageDown = true;
                            loading = true;
                            mPage = mPage + 1;
                            Log.v(TAG, "Down mPage" + mPage);
                            if (!sortParameter.equals("favorite")) {
                                loadMoviesData();
                            }
                        }
                    }
                } if (dy < 0) {
                    if (!loading) {
                        if (mPage > 1 && layoutManager.findFirstVisibleItemPosition() < layoutManager.getSpanCount()) {
                            pageDown = false;

                            loading = true;
                            mPage = mPage - 1;
                            Log.v(TAG, "Up mPage" + mPage);
                            if (!sortParameter.equals("favorite")) {
                                int lastPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                                loadMoviesData();
                                mRecyclerView.scrollToPosition(lastPosition + totalItemCount);
                            }
                        }
                    }
                }
            }
        });

        loadMoviesData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(SAVED_LAYOUT_MANAGER, mRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putString(SORT_PARAMETER, sortParameter);
        outState.putInt(PAGE_PARAMETER, mPage);
        outState.putBoolean(LIST_FAVORITE_MOVIES_PARAMETER, listFavoriteMovies);
        outState.putInt("recycle", mRecyclerView.getChildLayoutPosition(mRecyclerView.getFocusedChild()));

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            layoutManagerSavedState = savedInstanceState.getParcelable(SAVED_LAYOUT_MANAGER);
            sortParameter = savedInstanceState.getString(SORT_PARAMETER);
            mPage = savedInstanceState.getInt(PAGE_PARAMETER);
            listFavoriteMovies = savedInstanceState.getBoolean(LIST_FAVORITE_MOVIES_PARAMETER);
            child = savedInstanceState.getInt("recycle");
        } else {
            sortParameter = getString(R.string.popular_sort);
            listFavoriteMovies = false;
            mPage = 1;
        }
    }

    private void loadMoviesData() {
        showMoviesDataView();
        int loaderId;

        if (listFavoriteMovies) {
            loaderId = DATABASE_CURSOR_LOADER_ID;
        } else {
            loaderId = WEB_LOADER_ID;
        }
        getSupportLoaderManager().restartLoader(loaderId, null, this);
    }


    @Override
    public Loader onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case DATABASE_CURSOR_LOADER_ID:
                return new CursorLoader(this,
                        FavoriteMoviesContract.FavoriteMoviesEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);
            case WEB_LOADER_ID:
                return new MoviesLoader(this);
        }
        return null;
    }


    public static class MoviesLoader extends AsyncTaskLoader<ArrayList<Movie>> {

        public MoviesLoader(Context context) {
            super(context);
        }

        @Override
        public void onStartLoading() {
            if (!sortParameter.equals("favorite")) {
                mLoadingIndicator.setVisibility(View.VISIBLE);
                forceLoad();
            }
        }

        @Override
        public ArrayList<Movie> loadInBackground() {
            Log.v(TAG, "LoadIn Background internet? " + isInternetAvailable());
            if (!isInternetAvailable()) return null;

            URL moviesRequestUrl = NetworkUtils.buildUrl(sortParameter, String.valueOf(mPage));

            try {
                String jsonMoviesResponse = NetworkUtils
                        .getResponseFromHttpUrl(moviesRequestUrl);
                return OpenMoviesJsonUtils
                        .getMoviesStringsFromJson(getContext(), jsonMoviesResponse);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Log.v(TAG, "onLoadFinished");
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        loading = false;
        ArrayList<Movie> moviesData = new ArrayList<>();

        switch (loader.getId()) {
            case DATABASE_CURSOR_LOADER_ID: {
                if (!sortParameter.equals("favorite")) break;

                Cursor cursor = (Cursor) data;

                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    moviesData.add(new Movie(
                            cursor.getString(INDEX_MOVIE_TITLE),
                            cursor.getString(INDEX_MOVIE_ID),
                            cursor.getString(INDEX_MOVIE_POSTER),
                            cursor.getString(INDEX_MOVIE_SYNOPSIS),
                            cursor.getString(INDEX_MOVIE_USER_RATING),
                            cursor.getString(INDEX_MOVIE_RELEASE_DATE)));
                }

                mMoviesData = moviesData;

                if (cursor.getCount() != 0) {
                    showMoviesDataView();
                } else showErrorMessage();

                mMoviesAdapter.setMoviesData(mMoviesData);

                if (layoutManagerSavedState != null) {
                    mRecyclerView.getLayoutManager().onRestoreInstanceState(layoutManagerSavedState);
                    mRecyclerView.scrollToPosition(child);
                    layoutManagerSavedState = null;
                }

                break;
            }

            case WEB_LOADER_ID: {
                if (sortParameter.equals("favorite")) break;

                moviesData = (ArrayList<Movie>) data;

                if (moviesData != null) {
                    showMoviesDataView();
                    if (mMoviesData != null) {
                        if (pageDown){
                            Log.v(TAG, "pageDown");
                            mMoviesData.addAll(moviesData);
                        }else {
                            Log.v(TAG, "pageUp");
                            ArrayList<Movie> tempMoviesData;
                            tempMoviesData = mMoviesData;
                            mMoviesData = moviesData;
                            moviesData.addAll(tempMoviesData);
                        }
                    } else {
                        mMoviesData = moviesData;
                    }

                    mMoviesAdapter.setMoviesData(mMoviesData);

                    if (layoutManagerSavedState != null) {
                        Log.v(TAG, "restore layout");
                        mRecyclerView.getLayoutManager().onRestoreInstanceState(layoutManagerSavedState);
                        layoutManagerSavedState = null;
                    }
                } else {
                    showErrorMessage();
                }

                break;
            }
            default:
        }

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    private void showMoviesDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(Movie movieDataClicked) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, movieDataClicked);
        startActivity(intentToStartDetailActivity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movies, menu);
        return true;
    }

    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort_popularity) {
            mMoviesAdapter.setMoviesData(null);
            mMoviesData = null;

            sortParameter = getString(R.string.popular_sort);
            mPage = 1;
            mPosition = 0;

            layoutManagerSavedState = null;
            listFavoriteMovies = false;
            loadMoviesData();
            return true;
        }

        if (id == R.id.action_sort_top_rated) {
            mMoviesAdapter.setMoviesData(null);
            mMoviesData = null;

            sortParameter = getString(R.string.top_rated_sort);
            mPage = 1;
            mPosition = 0;

            layoutManagerSavedState = null;
            listFavoriteMovies = false;
            loadMoviesData();
            return true;
        }

        if (id == R.id.action_favorites_movies) {
            mMoviesAdapter.setMoviesData(null);
            mMoviesData = null;

            sortParameter = getString(R.string.favorite_sort);
            mPage = 1;
            mPosition = 0;

            layoutManagerSavedState = null;
            listFavoriteMovies = true;
            loadMoviesData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}