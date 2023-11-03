package com.cpen321.tunematch;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BackendInterface {

    @GET("/users/{userId}")
    Call<String> getUser(@Path("userId") String userId, @Query("fullProfile") boolean full);
    @POST("/users/create")
    Call<String> createUser(@Body JsonObject body);
    @GET("/users/search/{searchTerm}")
    Call<String> searchUser(@Path("searchTerm") String searchTerm, @Header("user-id") String currentUserId);
    @GET("/me")
    Call<String> getMe(@Header("user-id") String currentUserId, @Query("fullProfile") boolean full);
    @GET("/me/matches")
    Call<String> getMatches(@Header("user-id") String currentUserId);
    @PUT("/me/update")
    Call<String> updateMe(@Body JsonObject body, @Header("user-id") String currentUserId);

}