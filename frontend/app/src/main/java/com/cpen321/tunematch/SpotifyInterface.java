package com.cpen321.tunematch;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface SpotifyInterface {

    @GET("me")
    Call<String> getUser(@Header("Authorization") String authHeader);

    @GET("me/top/artists?limit=50&time_range=long_term")
    Call<String> getTopArtists(@Header("Authorization") String authHeader);

    @GET("search")
    Call<String> getSong(@Header("Authorization") String authHeader, @Query("q") String query, @Query("type") String type);
}
