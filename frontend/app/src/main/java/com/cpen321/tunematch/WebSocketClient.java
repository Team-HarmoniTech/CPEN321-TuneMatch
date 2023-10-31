package com.cpen321.tunematch;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.Synchronized;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketClient {
    private OkHttpClient client;
    private WebSocket webSocket;
    ReduxStore model;

    public WebSocketClient(ReduxStore model) {
        client = new OkHttpClient();
        this.model = model;
    }

    public void start(Headers customHeader) {
        String url = "wss://tunematch-api.bhairawaryan.com/socket";
        Request request = new Request.Builder().url(url).build();
        if (customHeader != null) {
            request = new Request.Builder().url(url).headers(customHeader).build();
        }
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // Handle when the WebSocket connection is opened.
                Log.d("WebSocketClient", "Websocket Client connected");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // Parse the JSON text.
                Log.d("WebSocketClient", "Received message: " + text);
                try {
                    JSONObject json = new JSONObject(text);
                    String method = json.getString("method");
                    if (method.equals("FRIENDS")) {
                        String action = json.getString("action");
                        if (action.equals("refresh")) {
                            JSONArray body = json.getJSONArray("body");
                            List<Friend> friends = new ArrayList<>();
                            List<Session> sessions = new ArrayList<>();
                            for (int i = 0; i < body.length(); i++) {
                                Log.d("WebSocketClient", "friend number : " + body.getJSONObject(i));
                                JSONObject friendJson = body.getJSONObject(i);
                                String id = friendJson.getString("userId");
                                String username = friendJson.getString("username");
                                String profilePic = friendJson.getString("profilePic");
                                String currentSong = friendJson.optString("currentSong");  // Using optString to avoid null values
                                JSONObject currentSource = friendJson.optJSONObject("currentSource");  // Using optJSONObject to avoid null values  // Using optString to avoid null values
                                Friend friend = new Friend(username, id, profilePic);
                                friend.setCurrentSong(currentSong);
                                friend.setCurrentSource(currentSource);
                                friends.add(friend);
                                if (currentSource != null) {
                                    String sourceType = currentSource.optString("type");
                                    if (sourceType.equals("session")) {
                                        sessions.add(new Session(friend.getId(), friend.getName()+"'s Room"));
                                    }
                                }
                            }
                            // Update the Redux store.
                            model.getFriendsList().postValue(friends);
                            model.getSessionList().postValue(sessions);
                        }
                        if (action.equals("update")){

                        }
                    }

                } catch (JSONException e) {
                    Log.e("WebSocketClient", "Failed to parse JSON", e);
                }
            }


            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // Handle incoming messages as binary data.
                Log.d("WebSocketClient", "onMessage Bytes: " + bytes.hex() + " / " + bytes.utf8());

            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                // Handle when the server is going to close the connection.
                Log.d("WebSocketClient", "onClosing: " + code + " / " + reason);
                webSocket.close(1000, null);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.d("WebSocketClient", "onFailure: Error = " + t);
                Log.d("WebSocketClient", "onFailure: Response = " + response);
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
