package com.example.flixster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.flixster.models.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class MovieDetailsActivity extends AppCompatActivity {

    Movie movie;
    String url;
    public static final String TAG = "MovieDetailsActivity";

    ImageView ivPoster;
    TextView tvTitle;
    TextView tvOverview;
    RatingBar rbVoteAverage;
    ImageView ivYoutubePlayBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // Initialize the view objects
        ivPoster = findViewById(R.id.ivPoster);
        tvTitle = findViewById(R.id.tvTitle);
        tvOverview = findViewById(R.id.tvOverview);
        rbVoteAverage = findViewById(R.id.rbVoteAverage);
        ivYoutubePlayBtn = findViewById(R.id.ivYoutubePlayBtn);

        // Unwrap the movie passed via intent
        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        url = "https://api.themoviedb.org/3/movie/"+movie.getId()+"/videos?api_key=a07e22bc18f5cb106bfe4cc1f83ad8ed";

        // Set the poster
        String imageUrl;
        int radius;
        int margin;
        String buttonUrl = "https://lh3.googleusercontent.com/proxy/_gpOjHL0wE4hBq96-wfUAXeAy2GM9X3z_thRR_JkaXWfbOVdTUTFK2UEkj2SI8gBCkkW5tEc5Cw_JEmUqG7qGecZH9xDNVs";
        int buttonRadius;
        int buttonMargin;

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageUrl = movie.getBackdropPath();
            radius = 50; // corner radius, higher value = more rounded
            margin = 10; // crop margin, set to 0 for corners with no crop
            buttonRadius = 75; // corner radius, higher value = more rounded
            buttonMargin = 50; // crop margin, set to 0 for corners with no crop
        } else {
            imageUrl = movie.getPosterPath();
            radius = 30; // corner radius, higher value = more rounded
            margin = 10; // crop margin, set to 0 for corners with no crop
            buttonRadius = 50; // corner radius, higher value = more rounded
            buttonMargin = 10; // crop margin, set to 0 for corners with no crop
        }

        Glide.with(this)
                .load(imageUrl)
                .centerCrop() // scale image to fill the entire ImageView
                .transform(new RoundedCornersTransformation(radius, margin))
                .placeholder(R.drawable.flicks_movie_placeholder)
                .into(ivPoster);

        Glide.with(this)
                .load(buttonUrl)
                .centerCrop() // scale image to fill the entire ImageView
                .transform(new RoundedCornersTransformation(buttonRadius, buttonMargin))
                .placeholder(R.drawable.flicks_movie_placeholder)
                .into(ivYoutubePlayBtn);

        // Set the title and overview
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());

        // vote average is 0..10, convert to 0..5 by dividing by 2
        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage / 2.0f);

        ivPoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncHttpClient client = new AsyncHttpClient();
                client.get(url, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Headers headers, JSON json) {
                        Log.d(TAG, "onSucess");
                        Log.d(TAG, json.toString());
                        JSONObject jsonObject = json.jsonObject;
                        try {
                            JSONArray results = jsonObject.getJSONArray("results");
                            String key = results.getJSONObject(0).getString("key");
                            Intent intent = new Intent(MovieDetailsActivity.this, MovieTrailerActivity.class);
                            intent.putExtra("youtubeKey", key);
                            MovieDetailsActivity.this.startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int i, Headers headers, String s, Throwable throwable) {
                        Log.d(TAG, "onFailure");
                    }
                });
            }
        });
    }
}