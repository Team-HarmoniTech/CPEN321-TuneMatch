package com.cpen321.tunematch;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class SpotifyService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "SpotifyService";
    private static final String REDIRECT_URI = "cpen321tunematch://callback"; // your redirect uri
    private final IBinder binder = new LocalBinder();
    public boolean webSocketBound = false;
    ReduxStore model;
    private Song lastSong = null;
    private SpotifyAppRemote mSpotifyAppRemote;
    private String CLIENT_ID;
    private WebSocketService webSocketService;
    // ChatGPT Usage: Partial
    private final ServiceConnection webSocketConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.LocalBinder binder = (WebSocketService.LocalBinder) service;
            webSocketService = binder.getService();
            webSocketBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            webSocketService = null;
            webSocketBound = false;
        }
    };

    // ChatGPT Usage: No
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // ChatGPT Usage: Partial
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("SpotifyService", "Service is being started");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Your Channel Name",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new Notification.Builder(this, "SpotifyService")
                .setContentTitle("SpotifyService")
                .setContentText("Running in the foreground")
                .build();
        startForeground(NOTIFICATION_ID, notification);

        // Start spotify connection
        CLIENT_ID = Objects.requireNonNull(intent.getStringExtra("clientID"));
        model = ReduxStore.getInstance();
        if (mSpotifyAppRemote == null || !mSpotifyAppRemote.isConnected()) {
            connectToSpotify();
        }

        // Get websocket service
        Intent intention = new Intent(this, WebSocketService.class);
        bindService(intention, webSocketConnection, Context.BIND_AUTO_CREATE);

        return START_STICKY;
    }

    // ChatGPT Usage: Partial
    private void connectToSpotify() {
        SpotifyAppRemote.connect(this, new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build(),
                new Connector.ConnectionListener() {
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        Log.e("SpotifyService", "Connected! Yay!");
                        mSpotifyAppRemote = spotifyAppRemote;
//                                set the current song in the spotifyapp remote as the current song in the model
                        mSpotifyAppRemote.getPlayerApi().getPlayerState()
                                .setResultCallback(playerState -> {
                                    if (playerState != null && playerState.track != null) {
                                        Log.d("SpotifyService the current song resets on load", playerState.track.uri);
                                        String ID = playerState.track.uri.split(":")[2];
                                        Song currentSong = new Song(ID, playerState.track.name, playerState.track.artist.name, playerState.track.duration);
                                        currentSong.setCurrentPosition(playerState.playbackPosition);
                                        // currentSong.setIsPLaying(!playerState.isPaused);
                                        model.getCurrentSong().postValue(currentSong);
                                        model.getCurrentSongForFriends().postValue(currentSong);
                                    }
                                });
                        mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(new Subscription.EventCallback<PlayerState>() {
                            @Override
                            public void onEvent(PlayerState playerState) {
                                if (playerState != null && playerState.track != null && !playerState.isPaused) {
                                    String ID = playerState.track.uri.split(":")[2];
                                    Song currentSong = new Song(ID, playerState.track.name, playerState.track.artist.name, playerState.track.duration);
                                    currentSong.setSongAlbum(playerState.track.album.name);
                                    if (lastSong == null || !lastSong.getSongID().equals(currentSong.getSongID())) {
                                        lastSong = currentSong;
                                        if (webSocketService != null) {
                                            JSONObject messageToSend = new JSONObject();
                                            try {
                                                messageToSend.put("method", "FRIENDS");
                                                messageToSend.put("action", "update");
                                                JSONObject song = new JSONObject();
                                                JSONObject body = new JSONObject();
                                                song.put("name", currentSong.getSongName());
                                                song.put("durationMs", currentSong.getDuration());
                                                song.put("album", currentSong.getSongAlbum());
                                                song.put("artist", currentSong.getSongArtist());
                                                song.put("uri", currentSong.getSongID());
                                                body.put("song", song);
                                                messageToSend.put("body", body);
                                                Log.d("CurrentSong", "Sending friend update with song " + currentSong.getSongName());
                                                webSocketService.sendMessage(messageToSend.toString());
                                            } catch (JSONException e) {
                                                Log.e("CurrentSong", "Failed to create JSON message for updating friends about song change", e);
                                            }
                                        } else {
                                            Log.e("CurrentSong", "WebSocketService is not available or session is not active");
                                        }
                                    }
                                }
                            }
                        });

                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("SpotifyService", throwable.getMessage());
                    }
                });
    }

    // ChatGPT Usage: No
    @Override
    public void onDestroy() {
        Log.e("SpotifyService", "Service is being destroyed");
        super.onDestroy();
        if (mSpotifyAppRemote != null && mSpotifyAppRemote.isConnected()) {
            mSpotifyAppRemote.getPlayerApi().pause();
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
            mSpotifyAppRemote = null;
        }

    }

    // ChatGPT Usage: No
    public SpotifyAppRemote getSpotifyAppRemote() {
        return mSpotifyAppRemote;
    }

    // ChatGPT Usage: No
    public class LocalBinder extends Binder {
        SpotifyService getService() {
            return SpotifyService.this;
        }
    }
}
