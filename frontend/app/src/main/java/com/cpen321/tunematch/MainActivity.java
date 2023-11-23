package com.cpen321.tunematch;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import java.util.ArrayList;
import java.util.logging.Handler;

import javax.naming.Context;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {
    private HomeFragment homeFrag;
    private RoomFragment roomFrag;
    private SearchFragment searchFrag;
    private ProfileFragment profileFrag;
    private BackendClient backend;

    private ReduxStore model;
    public boolean isServiceBound = false;
    private WebSocketService webSocketService;
    private SpotifyService mSpotifyService;
    private boolean mSpotifyBound = false;
    private Song lastSongState = null;

    // ChatGPT Usage: Partial
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

    // ChatGPT Usage: Partial
    @Override
    protected void onStart() {
        super.onStart();
        Intent intention = new Intent(this, WebSocketService.class);
        bindService(intention, serviceConnection, Context.BIND_AUTO_CREATE);
        Intent spotifyIntent = new Intent(this, SpotifyService.class);
        bindService(spotifyIntent, mSpotifyConnection, Context.BIND_AUTO_CREATE);
    }

    // ChatGPT Usage: Partial
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

    // ChatGPT Usage: Partial
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

    // ChatGPT Usage: Partial
    public SpotifyService getSpotifyService() {
        if (mSpotifyBound) {
            return mSpotifyService;
        } else {
            return null;
        }
    }

    // ChatGPT Usage: Partial
    public void sendMessageViaWebSocket(String message) {
        if (isServiceBound && webSocketService != null) {
            webSocketService.sendMessage(message);
        }
    }

    // ChatGPT Usage: No
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        model = ReduxStore.getInstance();

        model.getCurrentSong().observe(this, song -> {
//          execute the function only if the song name changes
            if (song != null) {
                // Check if the playing state has changed
                if (lastSongState == null || lastSongState.getSongID().equals(song.getSongID())) {
                    if (webSocketService != null) {
                        JSONObject messageToSend = new JSONObject();
                        try {
                            messageToSend.put("method", "FRIENDS");
                            messageToSend.put("action", "update");
                            JSONObject body = new JSONObject();
                            JSONObject songInfo = new JSONObject();
                            songInfo.put("uri", song.getSongID());
                            songInfo.put("name", song.getSongName());
                            body.put("song", songInfo);
                            messageToSend.put("body", body);
                            webSocketService.sendMessage(messageToSend.toString());
                        } catch (JSONException e) {
                            Log.e(TAG, "Failed to create JSON message for updating friends about song change", e);
                        }
                    } else {
                        Log.e(TAG, "WebSocketService is not available or session is not active");
                    }
                }
                lastSongState = new Song(song.getSongID(), song.getSongName(), song.getSongArtist(), song.getDuration());
                lastSongState.setCurrentPosition(song.getCurrentPosition());
                lastSongState.setIsPLaying(song.isPlaying());
            }
        });

        // Retrieve the Spotify User ID from the Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("spotifyUserId")) {
            String spotifyUserId = intent.getStringExtra("spotifyUserId");

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
        }

        // Check if notification permission is granted
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            // If permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},0);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavi);
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.stop();
        }
    }

    // ChatGPT Usage: No
    public BackendClient getBackend() {return backend;}


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

