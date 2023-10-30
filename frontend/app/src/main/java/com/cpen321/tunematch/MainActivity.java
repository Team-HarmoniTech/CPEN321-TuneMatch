package com.cpen321.tunematch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.SpotifyAppRemote;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        model = new ReduxStore();
        webSocketClient = new WebSocketClient(model);


        // Retrieve the Spotify User ID from the Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("spotifyUserId")) {
            String spotifyUserId = intent.getStringExtra("spotifyUserId");

            apiClient = new ApiClient("https://zphy19my7b.execute-api.us-west-2.amazonaws.com/v1",
                    new Headers.Builder().add("user-id", spotifyUserId).build());
            webSocketClient.start(new Headers.Builder().add("user-id", spotifyUserId).build());
        }

        bottomNavigationView = findViewById(R.id.bottomNavi);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_home:
                        setFragment(0);
                        break;
                    case R.id.navigation_room:
                        setFragment(1);
                        break;
                    case R.id.navigation_search:
                        setFragment(2);
                        break;
                    case R.id.navigation_profile:
                        setFragment(3);
                        break;
                }
                return true;
            }
        });
        homeFrag = new HomeFragment();
        roomFrag = new RoomFragment();
        searchFrag = new SearchFragment();
        profileFrag = new ProfileFragment();
        setFragment(0);             // Initialize default fragment to home
    }

    // Switch between fragments
    private void setFragment(int n) {
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();

        switch (n) {
            case 0:
                ft.replace(R.id.mainFrame, homeFrag);
                ft.commit();
                break;
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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocketClient.stop();
    }

    public ApiClient getApiClient() {return apiClient;}

    public WebSocketClient getWebSocketClient() {return webSocketClient;}
    public ReduxStore getModel() {return model;}

}

