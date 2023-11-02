package com.cpen321.tunematch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Headers;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private HomeFragment homeFrag;
    private RoomFragment roomFrag;
    private SearchFragment searchFrag;
    private ProfileFragment profileFrag;
    private ApiClient apiClient;
    private WebSocketClient webSocketClient;
    private ReduxStore model;

    public boolean isServiceBound = false;
    private WebSocketService webSocketService;
    private SpotifyService mSpotifyService;
    private boolean mSpotifyBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.LocalBinder binder = (WebSocketService.LocalBinder) service;
            webSocketService = binder.getService();
            isServiceBound = true;

            homeFrag = new HomeFragment();
            roomFrag = new RoomFragment();
            searchFrag = new SearchFragment();
            profileFrag = new ProfileFragment();
            setFragment(0);             // Initialize default fragment to home
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            webSocketService = null;
            isServiceBound = false;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        Intent intention = new Intent(this, WebSocketService.class);
        bindService(intention, serviceConnection, Context.BIND_AUTO_CREATE);
        Intent spotifyIntent = new Intent(this, SpotifyService.class);
        bindService(spotifyIntent, mSpotifyConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        if (mSpotifyBound) {
            unbindService(mSpotifyConnection);
            mSpotifyBound = false;
        }
    }

    private ServiceConnection mSpotifyConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SpotifyService.LocalBinder binder = (SpotifyService.LocalBinder) service;
            mSpotifyService = binder.getService();
            mSpotifyBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mSpotifyBound = false;
        }
    };

    public SpotifyService getSpotifyService() {
        if (mSpotifyBound) {
            return mSpotifyService;
        } else {
            return null;
        }
    }

    public void sendMessageViaWebSocket(String message) {
        if (isServiceBound && webSocketService != null) {
            webSocketService.sendMessage(message);
        }
    }


    // ChatGPT Usage: No
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        model = ReduxStore.getInstance();



        // Retrieve the Spotify User ID from the Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("spotifyUserId")) {
            String spotifyUserId = intent.getStringExtra("spotifyUserId");

            apiClient = new ApiClient("https://zphy19my7b.execute-api.us-west-2.amazonaws.com/v1",
                    new Headers.Builder().add("user-id", spotifyUserId).build());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String response = apiClient.doGetRequest("/me?fullProfile=true", true);
                        JSONObject resJson = new JSONObject(response);

                        String name = resJson.getString("username");
                        String id = resJson.getString("userId");
                        String profileUrl = resJson.getString("profilePic");
                        ArrayList<String> topArtists = parseList(response, "topArtists");
                        ArrayList<String> topGenres = parseList(response, "topGenres");

                        User currUser = new User(id, name, profileUrl);
                        currUser.setTopArtists(topArtists);
                        currUser.setTopGenres(topGenres);

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                model.setCurrentUser(currUser);
                            }
                        });

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }

        bottomNavigationView = findViewById(R.id.bottomNavi);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_room:
                        setFragment(1);
                        break;
                    case R.id.navigation_search:
                        setFragment(2);
                        break;
                    case R.id.navigation_profile:
                        setFragment(3);
                        break;
                    default:
                        setFragment(0);         // home fragment

                }
                return true;
            }
        });

    }

    // ChatGPT Usage: No
    private void setFragment(int n) {
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();

        switch (n) {
            case 1:
                ft.replace(R.id.mainFrame, roomFrag);
                ft.commit();
                break;
            case 2:
                ft.replace(R.id.mainFrame, searchFrag);
                ft.commit();
                break;
            case 3:
                ft.replace(R.id.mainFrame, profileFrag);
                ft.commit();
                break;
            default:
                ft.replace(R.id.mainFrame, homeFrag);
                ft.commit();
                break;
        }
    }

    // ChatGPT Usage: No
    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocketClient.stop();

    }

    // ChatGPT Usage: No
    public ApiClient getApiClient() {return apiClient;}

    // ChatGPT Usage: No
    public WebSocketClient getWebSocketClient() {return webSocketClient;}

    // ChatGPT Usage: No
    public ReduxStore getModel() {return model;}

    // ChatGPT Usage: No
    public WebSocketService getWebSocketService() {
        if (isServiceBound && webSocketService != null) {
            return webSocketService;
        }
        return null;
    }

    // ChatGPT Usage: No
    public ArrayList<String> parseList(String response, String key) {
        Log.d("MainActivity", "parseList: "+key);

        ArrayList<String> parsedList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            String tempList = jsonObject.getString(key).replace("[", "").replace("]","").trim();
            Log.d("MainActivity", "tempList:"+tempList);

            for (String item : tempList.split(",")) {
                parsedList.add(item.replace("\"", ""));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parsedList;
    }
}

