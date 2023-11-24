package com.cpen321.tunematch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    public boolean webSocketBound = false;
    private HomeFragment homeFrag;
    private RoomFragment roomFrag;
    private SearchFragment searchFrag;
    private ProfileFragment profileFrag;
    private BackendClient backend;
    private ReduxStore model;
    private WebSocketService webSocketService;
    // ChatGPT Usage: Partial
    private final ServiceConnection webSocketConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.LocalBinder binder = (WebSocketService.LocalBinder) service;
            webSocketService = binder.getService();
            webSocketBound = true;

            homeFrag = new HomeFragment();
            roomFrag = new RoomFragment();
            searchFrag = new SearchFragment();
            profileFrag = new ProfileFragment();
            setFragment(0);             // Initialize default fragment to home
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            webSocketService = null;
            webSocketBound = false;
        }
    };
    private SpotifyService mSpotifyService;
    private boolean mSpotifyBound = false;
    // ChatGPT Usage: Partial
    private final ServiceConnection mSpotifyConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SpotifyService.LocalBinder binder = (SpotifyService.LocalBinder) service;
            mSpotifyService = binder.getService();
            mSpotifyBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mSpotifyService = null;
            mSpotifyBound = false;
        }
    };

    // ChatGPT Usage: Partial
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("spotifyUserId")) {
            throw new RuntimeException("no spotify userID");
        }

        Intent webSocketIntent = new Intent(this, WebSocketService.class);
        bindService(webSocketIntent, webSocketConnection, Context.BIND_AUTO_CREATE);

        Intent spotifyIntent = new Intent(this, SpotifyService.class);
        bindService(spotifyIntent, mSpotifyConnection, Context.BIND_AUTO_CREATE);
    }

    // ChatGPT Usage: Partial
    @Override
    protected void onStop() {
        super.onStop();
        if (webSocketBound) {
            unbindService(webSocketConnection);
            webSocketBound = false;
        }
        if (mSpotifyBound) {
            unbindService(mSpotifyConnection);
            mSpotifyBound = false;
        }
    }

    // ChatGPT Usage: Partial
    public SpotifyService getSpotifyService() {
        if (mSpotifyBound) {
            return mSpotifyService;
        } else {
            return null;
        }
    }

    // ChatGPT Usage: No
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        model = ReduxStore.getInstance();

        // Retrieve the Spotify User ID from the Intent
        String spotifyUserId = getIntent().getStringExtra("spotifyUserId");
        backend = new BackendClient(spotifyUserId);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    User currUser = backend.getMe(true);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            model.setCurrentUser(currUser);
                        }
                    });

                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Check if notification permission is granted
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            // If permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 0);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavi);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
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
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        switch (n) {
            case 1:
                ft.replace(R.id.mainFrame, roomFrag);
                ft.addToBackStack(null);
                ft.commit();
                break;
            case 2:
                ft.replace(R.id.mainFrame, searchFrag);
                ft.addToBackStack(null);
                ft.commit();
                break;
            case 3:
                ft.replace(R.id.mainFrame, profileFrag);
                ft.addToBackStack(null);
                ft.commit();
                break;
            default:
                ft.replace(R.id.mainFrame, homeFrag);
                ft.addToBackStack(null);
                ft.commit();
                break;
        }
    }

    // ChatGPT Usage: No
    public BackendClient getBackend() {
        return backend;
    }

    // ChatGPT Usage: No
    public ReduxStore getModel() {
        return model;
    }

    // ChatGPT Usage: No
    public WebSocketService getWebSocketService() {
        if (webSocketBound && webSocketService != null) {
            return webSocketService;
        }
        return null;
    }
}

