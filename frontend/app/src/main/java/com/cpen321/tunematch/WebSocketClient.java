package com.cpen321.tunematch;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketClient {
    private OkHttpClient client;
    private WebSocket webSocket;

    public WebSocketClient() {
        client = new OkHttpClient();
    }

    public void start() {
        String url = "ws://35.87.48.140:3000/socket";
        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // Handle when the WebSocket connection is opened.
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // Handle incoming messages as text.
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // Handle incoming messages as binary data.
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                // Handle when the server is going to close the connection.
                webSocket.close(1000, null);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                // Handle when an error occurs.
            }
        });
    }

    public void stop() {
        if (webSocket != null) {
            webSocket.close(1000, "Goodbye!");
        }
    }

    public void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }
}
