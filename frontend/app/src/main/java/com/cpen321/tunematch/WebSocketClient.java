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
                Log.d("WebSocketClient", "Received message: " + text);
                try {
                    JSONObject json = new JSONObject(text);
                    String method = json.getString("method");
                    if (method.equals("FRIENDS")) {
                        handleFriends(json);
                    } else if (method.equals("SESSIONS")) {
                        handleSession(json);
                    } else if (method.equals("REQUESTS")){
                        handleRequests(json);
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
            Log.d("WebSocketClient", "Sending message: " + message);
            webSocket.send(message);
        }
    }

    private void handleFriends(JSONObject json) {
        try {
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
            else if (action.equals("update")) {
                JSONObject body = json.getJSONObject("body");
                String song = body.optString("song", null);
                if (song != null) {
                    // This is an update from the current user
                    // TODO: Update the Redux store with the new currently playing song
                } else {
                    // This is an update from another user
                    String userId = body.getString("userId");
                    String username = body.getString("username");
                    String profilePic = body.getString("profilePic");
                    String currentSong = body.optString("currentSong", null);
                    JSONObject currentSource = body.optJSONObject("currentSource");
                    // TODO: Update the Redux store with the updated friend's activity
                }
            }

            // Handling any other unexpected actions
            else {
                Log.w("WebSocketClient", "Unknown friend action: " + action);
            }

        } catch (JSONException e) {
            Log.e("WebSocketClient", "Error processing friend message", e);
        }
    }

    private void handleSession(JSONObject json){
        try {
            String action = json.getString("action");

            // Handling the join action
            if (action.equals("join")) {
                JSONObject body = json.getJSONObject("body");
                String userId = body.getString("userId");
                String username = body.optString("username", null);
                String profilePic = body.optString("profilePic", null);
                // TODO: Update the Redux store with the new member's details
            }

            // Handling the leave action
            else if (action.equals("leave")) {
                JSONObject body = json.getJSONObject("body");
                String userId = body.getString("userId");
                // TODO: Update the Redux store to remove the member's details
            }

            // Handling the refresh action
            else if (action.equals("refresh")) {
                JSONObject body = json.getJSONObject("body");
                JSONArray members = body.getJSONArray("members");
                JSONObject currentlyPlaying = body.optJSONObject("currentlyPlaying");
                JSONArray queue = body.getJSONArray("queue");
                // TODO: Update the Redux store with the refreshed data
            }

            // Handling the queueReplace action
            else if (action.equals("queueReplace")) {
                JSONArray queue = json.getJSONArray("body");
                // TODO: Replace the current song queue in the Redux store with the new queue
            }

            // Handling the queueAdd action
            else if (action.equals("queueAdd")) {
                JSONObject songDetails = json.getJSONObject("body");
                // TODO: Add the new song to the Redux store's song queue
            }

            // Handling the queueSkip action
            else if (action.equals("queueSkip")) {
                // TODO: Skip the currently playing song in the Redux store
            }

            // Handling the queueDrag action
            else if (action.equals("queueDrag")) {
                int startIndex = json.getInt("startIndex");
                int endIndex = json.getInt("endIndex");
                // TODO: Reorder the song queue in the Redux store based on the startIndex and endIndex
            }

            // Handling the queuePause action
            else if (action.equals("queuePause")) {
                // TODO: Pause the currently playing song in the Redux store
            }

            // Handling the queueResume action
            else if (action.equals("queueResume")) {
                // TODO: Resume the paused song in the Redux store
            }

            // Handling the queueSeek action
            else if (action.equals("queueSeek")) {
                int seekPosition = json.getInt("seekPosition");
                // TODO: Seek to the specified position in the currently playing song in the Redux store
            }

            // Handling the message action
            else if (action.equals("message")) {
                JSONObject messageDetails = json.getJSONObject("body");
                String from = json.getString("from");
                // TODO: Add the new message to the Redux store's chat messages
            }

            // Handling any other unexpected actions
            else {
                Log.w("WebSocketClient", "Unknown session action: " + action);
            }

        } catch (JSONException e) {
            Log.e("WebSocketClient", "Error processing session message", e);
        }
    }

    private void handleRequests(JSONObject json){
        try {
            String action = json.getString("action");

            // Handling the refresh action
            if (action.equals("refresh")) {
                JSONObject body = json.getJSONObject("body");
                JSONArray requesting = body.getJSONArray("requesting");
                JSONArray requested = body.getJSONArray("requested");
                // TODO: Update the Redux store with the refreshed lists of requesting and requested users
            }

            // Handling the add action
            else if (action.equals("add")) {
                JSONObject body = json.getJSONObject("body");
                String userId = body.getString("userId");
                String username = body.optString("username", null);
                String profilePic = body.optString("profilePic", null);
                String currentSong = body.optString("currentSong", null);
                String currentSource = body.optString("currentSource", null);
                // TODO: Update the Redux store with the new friend request or new friend
            }

            // Handling the remove action
            else if (action.equals("remove")) {
                JSONObject body = json.getJSONObject("body");
                String userId = body.getString("userId");
                String username = body.optString("username", null);
                String profilePic = body.optString("profilePic", null);
                // TODO: Update the Redux store to remove the friend or friend request
            }

            // Handling any other unexpected actions
            else {
                Log.w("WebSocketClient", "Unknown request action: " + action);
            }

        } catch (JSONException e) {
            Log.e("WebSocketClient", "Error processing request message", e);
        }
    }

}
