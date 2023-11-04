package com.cpen321.tunematch;


import android.os.Handler;
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
    private Handler handler;
    private static final int PING_INTERVAL = 20000;  // 30 seconds
    public WebSocketClient(ReduxStore model) {
        client = new OkHttpClient();
        this.model = model;
        handler = new Handler();
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
                super.onOpen(webSocket, response);
                startPing();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("WebSocketClient", "Received message: " + text);
                try {
                    JSONObject json = new JSONObject(text);
                    String method = json.getString("method");
                    if (method.equals("FRIENDS")) {
                        handleFriends(json);
                    } else if (method.equals("SESSION")) {
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
                    JSONObject friendJson = body.getJSONObject(i);
                    String id = friendJson.getString("userId");
                    String username = friendJson.getString("username");
                    String profilePic = friendJson.getString("profilePic");
                    String currentSong = friendJson.optString("currentSong");
                    JSONObject currentSource = friendJson.optJSONObject("currentSource");
                    Friend friend = new Friend(username, id, profilePic);
                    friend.setCurrentSong(currentSong);
                    friend.setCurrentSource(currentSource);
                    friends.add(friend);
                    if (currentSource != null) {
                        String sourceType = currentSource.optString("type");
                        if (sourceType.equals("session")) {
                            sessions.add(new Session(friend.getId(), friend.getName() + "'s Room"));
                        }
                    }
                }
                // Update the Redux store.
                model.getFriendsList().postValue(friends);
                model.getSessionList().postValue(sessions);
            } else if (action.equals("update")) {
                String from = json.optString("from");
                if (from != null) {
                    List<Friend> existingFriendList = model.getFriendsList().getValue();
                    List<Session> existingSessionList = model.getSessionList().getValue();
                    for (Friend f : existingFriendList) {
                        if (f.getId().equals(from)) {
                            JSONObject body = json.getJSONObject("body");
                            String currentSong = body.optString("currentSong", null);
                            JSONObject currentSource = body.optJSONObject("currentSource");
                            f.setCurrentSong(currentSong);
                            f.setCurrentSource(currentSource);
                            if (currentSource != null) {
                                String sourceType = currentSource.optString("type");
                                if (sourceType.equals("session")) {
                                    boolean sessionExists = false;
                                    for (Session s : existingSessionList) {
                                        if (s.getSessionId().equals(from)) {
                                            sessionExists = true;
                                            break;
                                        }
                                    }
                                    if (!sessionExists) {
                                        existingSessionList.add(new Session(f.getId(), f.getName() + "'s Room"));
                                    }
                                }
                            } else {
                                for (Session s : existingSessionList) {
                                    if (s.getSessionId().equals(from)) {
                                        existingSessionList.remove(s);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    model.getFriendsList().postValue(existingFriendList);
                    model.getSessionList().postValue(existingSessionList);
                }
            }
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
            if (action.equals("join")) {
                JSONObject body = json.getJSONObject("body");
                String userId = body.getString("userId");
                String username = body.optString("username", null);
                String profilePic = body.optString("profilePic", null);
                CurrentSession currentSession = model.getCurrentSession().getValue();
                List<SessionUser> sessionMembers = currentSession.getSessionMembers();
                SessionUser sessionUser = new SessionUser(username, userId, profilePic);
                sessionMembers.add(sessionUser);
                currentSession.setSessionMembers(sessionMembers);
                model.getCurrentSession().postValue(currentSession);
            }
            else if (action.equals("leave")) {
                String from = json.optString("from");
                if (from != null) {
                    JSONObject body = json.getJSONObject("body");
                    String userId = body.getString("userId");
                    CurrentSession currentSession = model.getCurrentSession().getValue();
                    List<SessionUser> sessionMembers = currentSession.getSessionMembers();
                    for (SessionUser s : sessionMembers) {
                        if (s.getUserId().equals(userId)) {
                            sessionMembers.remove(s);
                            break;
                        }
                    }
                    currentSession.setSessionMembers(sessionMembers);
                    model.getCurrentSession().postValue(currentSession);
                }else{
                    model.checkSessionActive().postValue(false);
                }
            }
            else if (action.equals("refresh")) {
                JSONObject body = json.getJSONObject("body");
                JSONArray members = body.getJSONArray("members");
                JSONObject currentlyPlaying = body.optJSONObject("currentlyPlaying");
                JSONArray queue = body.getJSONArray("queue");

                CurrentSession currentSession = model.getCurrentSession().getValue();
                if(currentSession == null){
                    currentSession = new CurrentSession("session", "MySession");
                }
                List<SessionUser> sessionMembers = new ArrayList<>();
                List<Song> sessionQueue = new ArrayList<>();

                for (int i = 0; i < members.length(); i++) {
                    JSONObject member = members.getJSONObject(i);
                    String id = member.getString("userId");
                    String username = member.getString("username");
                    String profilePic = member.getString("profilePic");
                    SessionUser sessionUser = new SessionUser(username, id, profilePic);
                    sessionMembers.add(sessionUser);
                }

                if(currentlyPlaying != null){
                    String songId = currentlyPlaying.getString("uri");
                    String songName = currentlyPlaying.getString("title");
                    String songArtist = currentlyPlaying.getString("artist");
                    String duration = currentlyPlaying.getString("durationMs");
                    String Timestamp = currentlyPlaying.getString("timeStarted");
                    Song currentSong = new Song(songId, songName, songArtist, duration);
                    currentSong.setCurrentTimestamp(Timestamp);
                    currentSong.setIsPLaying(true);
                    currentSession.setCurrentSong(currentSong);
                }

                if(members.length() == 0){
                    List<Song> ExisitngSongQueue = model.getSongQueue().getValue();
                    if(ExisitngSongQueue==null){
                        ExisitngSongQueue = new ArrayList<>();
                    }
                    JSONArray songQueue = new JSONArray();
                    try {
                        for (Song s : ExisitngSongQueue) {
                            JSONObject song = new JSONObject();
                            song.put("uri", s.getSongID());
                            song.put("durationMs", s.getDuration());
                            song.put("title", s.getSongName());
                            song.put("artist", s.getSongArtist());
                            songQueue.put(song);
                        }
                        JSONObject messageToReplaceQueue = new JSONObject();
                        messageToReplaceQueue.put("method", "SESSION");
                        messageToReplaceQueue.put("action", "queueReplace");
                        messageToReplaceQueue.put("body", songQueue);
                        if (webSocket != null) {
                            webSocket.send(messageToReplaceQueue.toString());
                            currentSession.setSessionQueue(ExisitngSongQueue);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                else{
                    for (int i = 0; i < queue.length(); i++) {
                        JSONObject song = queue.getJSONObject(i);
                        String songId = song.getString("uri");
                        String duration = song.getString("durationMs");
                        String songName = song.getString("title");
                        String songArtist = song.getString("artist");
                        Song songToAdd = new Song(songId, songName, songArtist, duration);
                        sessionQueue.add(songToAdd);
                    }
                    currentSession.setSessionQueue(sessionQueue);
                    model.getSongQueue().postValue(sessionQueue);
                }
                currentSession.setSessionMembers(sessionMembers);
                currentSession.setSessionId("session");
                model.getCurrentSession().postValue(currentSession);
                model.checkSessionActive().postValue(true);
            }
            else if (action.equals("queueReplace")) {
                JSONArray queue = json.getJSONArray("body");
            }
            else if (action.equals("queueAdd")) {
                JSONObject songDetails = json.getJSONObject("body");
                String songId = songDetails.getString("uri");
                String duration = songDetails.getString("durationMs");
                String songName = songDetails.getString("title");
                String songArtist = songDetails.getString("artist");
                Song songToAdd = new Song(songId, songName, songArtist, duration);
                List<Song> sessionQueue = model.getSongQueue().getValue();
                if(sessionQueue == null){
                    sessionQueue = new ArrayList<>();
                }
                sessionQueue.add(songToAdd);
                model.getSongQueue().postValue(sessionQueue);
                CurrentSession currentSession = model.getCurrentSession().getValue();
                currentSession.setSessionQueue(sessionQueue);
            }
            else if (action.equals("queueSkip")) {
                CurrentSession currentSession = model.getCurrentSession().getValue();
                List<Song> sessionQueue = currentSession.getSessionQueue();
                Song currentSong = sessionQueue.get(0);
                sessionQueue.remove(0);
                currentSession.setSessionQueue(sessionQueue);
                currentSession.setCurrentSong(currentSong);
                model.getCurrentSession().postValue(currentSession);
                model.getCurrentSong().postValue(currentSong);
            }
            else if (action.equals("queueDrag")) {
                JSONObject body = json.getJSONObject("body");
                int startIndex =  body.getInt("startIndex");
                int endIndex = body.getInt("endIndex");
                CurrentSession currentSession = model.getCurrentSession().getValue();
                List<Song> sessionQueue = currentSession.getSessionQueue();
                Song songToMove = sessionQueue.get(startIndex);
                sessionQueue.remove(startIndex);
                sessionQueue.add(endIndex, songToMove);
                currentSession.setSessionQueue(sessionQueue);
                model.getCurrentSession().postValue(currentSession);
                model.getSongQueue().postValue(sessionQueue);
            }
            else if (action.equals("queuePause")) {
                Song currentSong = model.getCurrentSong().getValue();
                currentSong.setIsPLaying(false);
                model.getCurrentSong().postValue(currentSong);
            }
            else if (action.equals("queueResume")) {
                Song currentSong = model.getCurrentSong().getValue();
                currentSong.setIsPLaying(true);
                model.getCurrentSong().postValue(currentSong);
            }
            else if (action.equals("queueSeek")) {
                JSONObject body = json.getJSONObject("body");
                int seekPosition = body.getInt("seekPosition");
                seekPosition = seekPosition / 1000;
                Song currentSong = model.getCurrentSong().getValue();
                currentSong.setCurrentTimestamp(seekPosition+"");
                model.getCurrentSong().postValue(currentSong);
            }
            else if (action.equals("message")) {
                JSONObject messageDetails = json.getJSONObject("body");
                String from = json.getString("from");
            }
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
                SearchUser newRequest = new SearchUser(username, userId, profilePic);
                model.addFriendRequest(newRequest);

                Friend newFriend = new Friend(username, userId, profilePic);
                newFriend.setCurrentSong(currentSong);
                newFriend.setCurrentSource(new JSONObject(currentSource));
                model.addFriend(newFriend);
            }

            // Handling the remove action
            else if (action.equals("remove")) {
                JSONObject body = json.getJSONObject("body");
                String userId = body.getString("userId");
                String username = body.optString("username", null);
                String profilePic = body.optString("profilePic", null);

                // TODO: Update the Redux store to remove the friend or friend request
                SearchUser rmvRequest = new SearchUser(username, userId, profilePic);
                model.removeFriendRequest(rmvRequest);

                Friend friendToRemove = new Friend(username, userId, profilePic);
                model.removeFriend(friendToRemove);
            }

            // Handling any other unexpected actions
            else {
                Log.w("WebSocketClient", "Unknown request action: " + action);
            }

        } catch (JSONException e) {
            Log.e("WebSocketClient", "Error processing request message", e);
        }
    }
    private void startPing() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (webSocket != null) {
                    webSocket.send(okio.ByteString.EMPTY);  // Send an empty Ping frame
                }
                handler.postDelayed(this, PING_INTERVAL);
            }
        }, PING_INTERVAL);
    }
}
