package com.cpen321.tunematch;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
<<<<<<< HEAD
=======
import android.util.Log;
>>>>>>> c9f7044b072594cf84a51a4f35c208f5e81fc61a

import okhttp3.Headers;

public class WebSocketService extends Service {
    private final IBinder binder = new LocalBinder();
    private WebSocketClient webSocketClient;

    // ChatGPT Usage: No
    public class LocalBinder extends Binder {
        WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    // ChatGPT Usage: No
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // ChatGPT Usage: No
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("WebsocketServicebruh", "Service is being started");
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

    // ChatGPT Usage: No
    @Override
    public void onDestroy() {
        Log.e("WebsocketService", "our app has been closed:( ");
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.stop();
        }
    }

    // ChatGPT Usage: No
    // Method to send a message using WebSocketClient
    public void sendMessage(String message) {
        if (webSocketClient != null) {
            webSocketClient.sendMessage(message);
        }
    }

    // You can add other potential methods related to WebSocket functionality here if needed.
}

