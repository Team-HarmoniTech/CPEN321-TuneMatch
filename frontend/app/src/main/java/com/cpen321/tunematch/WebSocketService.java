package com.cpen321.tunematch;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import okhttp3.Headers;

public class WebSocketService extends Service {
    private final IBinder binder = new LocalBinder();
    private WebSocketClient webSocketClient;

    public class LocalBinder extends Binder {
        WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // You can initialize your WebSocketClient here and start the connection.
        // Assuming you get your ReduxStore instance from MainActivity or another source:
        if (intent != null) {
            ReduxStore model = ReduxStore.getInstance();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            webSocketClient = new WebSocketClient(model, this, notificationManager);
            Headers customheaders = new Headers.Builder().add("user-id", intent.getStringExtra("spotifyUserId")).build();
            webSocketClient.start(customheaders); // You can provide custom headers if needed
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.stop();
        }
    }

    // Method to send a message using WebSocketClient
    public void sendMessage(String message) {
        if (webSocketClient != null) {
            webSocketClient.sendMessage(message);
        }
    }

    // You can add other potential methods related to WebSocket functionality here if needed.
}

