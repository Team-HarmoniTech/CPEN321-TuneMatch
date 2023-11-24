package com.cpen321.tunematch;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.Objects;

import okhttp3.Headers;

public class WebSocketService extends Service {
    private final IBinder binder = new LocalBinder();
    private WebSocketClient webSocketClient;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "WebSocketService";

    // ChatGPT Usage: No
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // ChatGPT Usage: No
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("WebsocketService", "Service is being started");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Your Channel Name",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new Notification.Builder(this, "WebSocketService")
                .setContentTitle("WebSocketService")
                .setContentText("Running in the foreground")
                .build();
        startForeground(NOTIFICATION_ID, notification);

        ReduxStore model = ReduxStore.getInstance();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        webSocketClient = new WebSocketClient(model, this, notificationManager);
        Headers customheaders = new Headers.Builder().add("user-id", Objects.requireNonNull(intent.getStringExtra("spotifyUserId"))).build();
        webSocketClient.start(customheaders); // You can provide custom headers if needed

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

    // ChatGPT Usage: No
    public class LocalBinder extends Binder {
        WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    // You can add other potential methods related to WebSocket functionality here if needed.
}

