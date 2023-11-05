package com.cpen321.tunematch;

import static android.app.PendingIntent.getActivity;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class SpotifyService extends Service {
    private final IBinder binder = new LocalBinder();
    private SpotifyAppRemote mSpotifyAppRemote;
    private String CLIENT_ID;
    private static final String REDIRECT_URI = "cpen321tunematch://callback"; // your redirect uri

    ReduxStore model;
    WebSocketService webSocketClient;
    MainActivity mainActivity;


    // ChatGPT Usage: No
    public class LocalBinder extends Binder {
        SpotifyService getService() {
            return SpotifyService.this;
        }
    }

    // ChatGPT Usage: No
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // ChatGPT Usage: Partial
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Initialize and connect to Spotify here.
        // Consider checking if mSpotifyAppRemote is already initialized before trying to reconnect.
        CLIENT_ID = intent.getStringExtra("clientID");
        model = ReduxStore.getInstance();
        if (mSpotifyAppRemote == null || !mSpotifyAppRemote.isConnected()) {
            connectToSpotify();
        }
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
                            if (playerState != null && playerState.track != null && model.getCurrentSong().getValue() != null) {
                                                Log.e("SpotifyService the current song resets on load", playerState.track.uri);
                                                String ID = playerState.track.uri.split(":")[2];
                                                Song currentSong = new Song(ID, playerState.track.name, playerState.track.artist.name, playerState.track.duration+"");
                                                currentSong.setCurrentPosition(String.valueOf(playerState.playbackPosition));
//                                                currentSong.setIsPLaying(!playerState.isPaused);
                                                model.getCurrentSong().postValue(currentSong);
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
        super.onDestroy();
        if (mSpotifyAppRemote != null && mSpotifyAppRemote.isConnected()) {
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
            mSpotifyAppRemote = null;
        }
    }

    // ChatGPT Usage: No
    public SpotifyAppRemote getSpotifyAppRemote() {
        return mSpotifyAppRemote;
    }

    // Additional methods related to Spotify functionality can be added here.
}
