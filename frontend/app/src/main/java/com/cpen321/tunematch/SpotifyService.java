package com.cpen321.tunematch;

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

    public class LocalBinder extends Binder {
        SpotifyService getService() {
            return SpotifyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Initialize and connect to Spotify here.
        // Consider checking if mSpotifyAppRemote is already initialized before trying to reconnect.
        CLIENT_ID = intent.getStringExtra("clientID");
        if (mSpotifyAppRemote == null || !mSpotifyAppRemote.isConnected()) {
            connectToSpotify();
        }
        return START_STICKY;
    }

    private void connectToSpotify() {
        SpotifyAppRemote.connect(this, new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build(),
                        new Connector.ConnectionListener() {
                            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                                mSpotifyAppRemote = spotifyAppRemote;
                            }
                            public void onFailure(Throwable throwable) {
                                Log.e("SpotifyService", throwable.getMessage());
                            }
                        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSpotifyAppRemote != null && mSpotifyAppRemote.isConnected()) {
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
            mSpotifyAppRemote = null;
        }
    }

    public SpotifyAppRemote getSpotifyAppRemote() {
        return mSpotifyAppRemote;
    }

    // Additional methods related to Spotify functionality can be added here.
}
