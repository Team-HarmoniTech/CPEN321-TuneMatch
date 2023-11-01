package com.cpen321.tunematch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Headers;

public class LoginActivity extends AppCompatActivity {
    // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "cpen321tunematch://callback";
    private static final String TAG = "LoginActivity";
    private static final String CLIENT_ID = "0dcb406f508a4845b32a1342a91a71af";
    private static final int MAX_PROFILE_URL = 500;

    private String spotifyUserId;

    // ChatGPT Usage: Partial
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (!SpotifyAppRemote.isSpotifyInstalled(this)) {
            Log.d(TAG, "onCreate: spotify is not installed");
            // Create an AlertDialog.Builder instance
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Spotify app is not installed. Please download the Spotify app as it is required to run our app.")
                    .setTitle("Spotify Not Installed")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            AuthorizationRequest.Builder builder =
                    new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

            builder.setScopes(new String[]{"user-read-private", "user-library-read", "user-top-read", "user-read-email", "playlist-read-private", "streaming"});
            AuthorizationRequest request = builder.build();
            AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
        }

        Button loginButton = findViewById(R.id.spotify_login_button);
        loginButton.setOnClickListener(v -> {
            if (!SpotifyAppRemote.isSpotifyInstalled(this)) {
                Log.d(TAG, "onCreate: spotify is not installed");
                // Create an AlertDialog.Builder instance
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Spotify app is not installed. Please download the Spotify app as it is required to run our app.")
                        .setTitle("Spotify Not Installed")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK button
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                AuthorizationRequest.Builder builder =
                        new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

                builder.setScopes(new String[]{"user-read-private", "user-library-read", "user-top-read", "user-read-email", "playlist-read-private", "streaming"});
                AuthorizationRequest request = builder.build();
                AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
            }
        });
    }

    // ChatGPT Usage: Partial
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);
            Log.d(TAG, "here is the type of the response: " + response.getType());

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    Log.d(TAG, "onActivityResult: " + response.getAccessToken());
                    // Send to main activity
                    String auth_token = response.getAccessToken();

                    // Save the token
                    SharedPreferences preferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("auth_token", auth_token);
                    editor.apply();

                    fetchSpotifyUserId(auth_token);

                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Log.d(TAG, "onActivityResult: " + response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

    // ChatGPT Usage: Partial
    private void fetchSpotifyUserId(String authToken) {
        Log.d("fetchSpotifyUserId", "Inside function");
        // Set up your custom headers (including the Authorization header with the access token)
        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer "+authToken)
                .build();
        Log.d(TAG, "in fetchSpotifyUserId, authToken="+authToken);

        // Initialize your ApiClient with the base URL and custom headers
        String spotifyBaseUrl = "https://api.spotify.com/v1/";
        ApiClient spotifyApiClient = new ApiClient(spotifyBaseUrl, headers);

        // Define the endpoint to retrieve the user details
        String userEndpoint = "me";

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("fetchSpotifyUserId","inside thread");
                    // Make the GET request to retrieve user details
                    String userResponse = spotifyApiClient.doGetRequest(userEndpoint, true);

                    // Example using JSONObject (make sure to handle exceptions and null checks)
                    JSONObject jsonResponse = new JSONObject(userResponse);
                    Log.d(TAG, "Spotify my info: "+jsonResponse);
                    spotifyUserId = jsonResponse.getString("id");
                    Log.d(TAG, "Spotify User ID: " + spotifyUserId);

                    // Use the Spotify User ID to check if user already have an account
                    ApiClient apiClient = new ApiClient("https://zphy19my7b.execute-api.us-west-2.amazonaws.com/v1",
                                                        null);

                    try {
                        apiClient.doGetRequest("/users/" + spotifyUserId, false);

                        // If didn't fail start MainActivity with the Spotify User ID; user already exist
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("spotifyUserId", spotifyUserId);
                                Intent webSocketServiceIntent = new Intent(LoginActivity.this, WebSocketService.class);
                                webSocketServiceIntent.putExtra("spotifyUserId", spotifyUserId);
                                startService(webSocketServiceIntent);
                                startActivity(intent);
                            }
                        });
                    } catch (IOException e) {
                        try {
                            // account does not exist need to create
                            String spotifyUserName = jsonResponse.getString("display_name");
                            JSONArray spotifyImage = jsonResponse.getJSONArray("images");
                            String spotifyImageUrl = "";
                            for (int i = 0; i < spotifyImage.length(); i++) {
                                JSONObject imageObject = spotifyImage.getJSONObject(i);
                                spotifyImageUrl = imageObject.getString("url");
                            }

                            if (spotifyImageUrl.length() > MAX_PROFILE_URL) {
                                spotifyImageUrl = "profile.com/url";
                            }

                            // get top artists
                            String topArtistsRes = spotifyApiClient.doGetRequest("me/top/artists", true);

                            // Parse the top artists response
                            JSONObject topArtistsObject = new JSONObject(topArtistsRes);
                            JSONArray topArtistsArray = topArtistsObject.getJSONArray("items");
                            ArrayList<String> topArtistList = new ArrayList<>();
                            Set<String> genreSet = new HashSet<>();
                            for (int i = 0; i < topArtistsArray.length(); i++) {
                                JSONObject artist = topArtistsArray.getJSONObject(i);
                                topArtistList.add(artist.getString("name"));

                                String genres = artist.getString("genres").replace("[", "").replace("]", "").trim();
                                for (String g : genres.split(",")) {
                                    genreSet.add(g.replace("\"", ""));
                                }
                            }

                            JSONObject createUserBody = new JSONObject();
                            JSONObject userInfo = new JSONObject();
                            userInfo.put("spotify_id", spotifyUserId);
                            userInfo.put("username", spotifyUserName);
                            userInfo.put("top_artists", new JSONArray(topArtistList));
                            userInfo.put("top_genres", new JSONArray(new ArrayList<>(genreSet)));
                            userInfo.put("pfp_url", spotifyImageUrl);
                            createUserBody.put("userData", userInfo);

                            Log.d(TAG, createUserBody.toString());

                            apiClient.doPostRequest("/users/create", createUserBody.toString(), false);

                            // Start MainActivity with the Spotify User ID
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("spotifyUserId", spotifyUserId);
                                    startActivity(intent);
                                }
                            });
                        } catch (IOException e1) {
                            e.printStackTrace();
                            Log.e(TAG, "Error creating user account: " + e1.getMessage());

                            handleError("Failed to create user account for TuneMatch. Please try again.");
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error fetching Spotify User ID: " + e.getMessage());

                    handleError("Failed to fetch Spotify User ID. Please try logging in again.");
                }
            }
        }).start();
    }

    // ChatGPT Usage: Partial
    private void handleError(String warningMessage) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage(warningMessage)
                        .setTitle("Error")
                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Retry login activity
                                startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                                finish(); // Finish the current instance of LoginActivity
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}
