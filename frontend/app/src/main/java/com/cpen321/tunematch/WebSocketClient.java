package com.cpen321.tunematch;


import static com.cpen321.tunematch.Message.timestampFormat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private Context context;
    private NotificationManager notification;
    private static final int PING_INTERVAL = 20000;  // 30 seconds

    public WebSocketClient(ReduxStore model, Context context, NotificationManager notification) {
        this.context = context;
        this.client = new OkHttpClient();
        this.model = model;
        this.handler = new Handler();
        this.notification = notification;
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
                    } else if (method.equals("REQUESTS")) {
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
                    String currentSong = friendJson.optString("currentSong");  // Using optString to avoid null values
                    JSONObject currentSource = friendJson.optJSONObject("currentSource");  // Using optJSONObject to avoid null values  // Using optString to avoid null values
                    Friend friend = new Friend(id, username, profilePic);
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
                List<Friend> oldFriends;
                if ((oldFriends = model.getFriendsList().getValue()) != null)
                    friends.addAll(oldFriends);
                model.getFriendsList().postValue(friends);

                List<Session> oldSessions;
                if ((oldSessions = model.getSessionList().getValue()) != null)
                    sessions.addAll(oldSessions);
                model.getSessionList().postValue(sessions);
            } else if (action.equals("update")) {
//                check if from exists in the message
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
            // Handling any other unexpected actions
            else {
                Log.w("WebSocketClient", "Unknown friend action: " + action);
            }

        } catch (JSONException e) {
            Log.e("WebSocketClient", "Error processing friend message", e);
        }
    }

    private void handleSession(JSONObject json) {
        try {
            String action = json.getString("action");
            // Handling the join action
            if (action.equals("join")) {
                JSONObject body = json.getJSONObject("body");
                String userId = body.getString("userId");
                String username = body.optString("username", null);
                String profilePic = body.optString("profilePic", null);
                // TODO: Update the Redux store with the new member's details
                CurrentSession currentSession = model.getCurrentSession().getValue();
                List<User> sessionMembers = currentSession.getSessionMembers();
                User user = new User(userId, username, profilePic);
                sessionMembers.add(user);
                currentSession.setSessionMembers(sessionMembers);
                model.getCurrentSession().postValue(currentSession);
            }

            // Handling the leave action
            else if (action.equals("leave")) {
                JSONObject body = json.getJSONObject("body");
                String userId = body.getString("userId");
                // TODO: Update the Redux store to remove the member's details
                CurrentSession currentSession = model.getCurrentSession().getValue();
                List<User> sessionMembers = currentSession.getSessionMembers();
                for (User s : sessionMembers) {
                    if (s.getUserId().equals(userId)) {
                        sessionMembers.remove(s);
                        break;
                    }
                }
                currentSession.setSessionMembers(sessionMembers);
                model.getCurrentSession().postValue(currentSession);
            }

            // Handling the refresh action
            else if (action.equals("refresh")) {
                JSONObject body = json.getJSONObject("body");
                JSONArray members = body.getJSONArray("members");
                JSONObject currentlyPlaying = body.optJSONObject("currentlyPlaying");
                JSONArray queue = body.getJSONArray("queue");
                // TODO: Update the Redux store with the refreshed data
                CurrentSession currentSession = model.getCurrentSession().getValue();
                if (currentSession == null) {
                    currentSession = new CurrentSession("session", "My Session");
                }
                List<User> sessionMembers = new ArrayList<>();
                List<Song> sessionQueue = new ArrayList<>();
                for (int i = 0; i < members.length(); i++) {
                    JSONObject member = members.getJSONObject(i);
                    String id = member.getString("userId");
                    String username = member.getString("username");
                    String profilePic = member.getString("profilePic");
                    User user = new User(id, username, profilePic);
                    sessionMembers.add(user);
                }
                for (int i = 0; i < queue.length(); i++) {
                    JSONObject song = queue.getJSONObject(i);
                    String songId = song.getString("uri");
                    String duration = song.getString("durationMs");
                    Song songToAdd = new Song(songId, duration);
                    sessionQueue.add(songToAdd);
                }
                if (currentlyPlaying != null) {
                    String songId = currentlyPlaying.getString("uri");
                    String duration = currentlyPlaying.getString("durationMs");
                    String timeStarted = currentlyPlaying.getString("timeStarted");
                    CurrentSong currentSong = new CurrentSong(songId, duration, timeStarted);
                    currentSession.setCurrentSong(currentSong);
                }
                currentSession.setSessionMembers(sessionMembers);
                currentSession.setSessionQueue(sessionQueue);
                currentSession.setSessionId("session");
                model.getCurrentSession().postValue(currentSession);
                model.getSongQueue().postValue(sessionQueue);
                model.checkSessionActive().postValue(true);
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
                User sender = model.getCurrentSession().getValue().getSessionMembers().stream()
                        .filter(member -> member.getUserId().equals(from))
                        .findFirst()
                        .orElse(null);
                Gson gson = new Gson();

                Message message = new Message(sender,
                        messageDetails.getString("message"),
                        timestampFormat.parse(messageDetails.getString("timestamp")));
                Log.d("WS", message.toString());

                // Add to redux
                model.addMessage(message, true);

                // Check permission
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                CharSequence name = "Messaging";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel("messaging_channel", name, importance);

                notification.createNotificationChannel(channel);

                // Build the notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel.getId())
                        .setSmallIcon(R.drawable.default_profile_image)
                        .setContentTitle(message.getSenderUserId())
                        .setContentText(message.getMessageText())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true); // Automatically removes the notification when the user taps it

                try {
                    builder.setLargeIcon(Picasso.get()
                            .load(message.getSenderProfileImageUrl())
                            .error(R.drawable.default_profile_image)
                            .get());
                } catch(IllegalStateException ignored)  { }

                // Show the notification
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(1, builder.build());
            }

            // Handling any other unexpected actions
            else {
                Log.w("WebSocketClient", "Unknown session action: " + action);
            }

        } catch (JSONException | ParseException | IOException e) {
            Log.e("WebSocketClient", "Error processing session message", e);
        }
    }

    // ChatGPT Usage: No
    private void handleRequests(JSONObject json){
        try {
            String action = json.getString("action");

            // Handling the refresh action
            if (action.equals("refresh")) {
                JSONObject body = json.getJSONObject("body");
                JSONArray requesting = body.getJSONArray("requesting");
                JSONArray requested = body.getJSONArray("requested");
                // TODO: Update the Redux store with the refreshed lists of requesting and requested users
                List<SearchUser> newRequests = model.getReceivedRequests().getValue();
                if (newRequests == null) {
                    newRequests = new ArrayList<SearchUser>();
                }
                for (int i = 0; i < requesting.length(); i++) {
                    JSONObject request = requesting.getJSONObject(i);
                    String userId = request.getString("userId");
                    String userName = request.getString("username");
                    String profilePic = request.getString("profilePic");
                    SearchUser requestingUser = new SearchUser(userName, userId, profilePic);
                    newRequests.add(requestingUser);
                }
                model.getReceivedRequests().postValue(newRequests);

                List<SearchUser> sentRequests = model.getSentRequests().getValue();
                if (sentRequests == null) {
                    sentRequests = new ArrayList<SearchUser>();
                }
                for (int i = 0; i < requested.length(); i++) {
                    JSONObject request = requested.getJSONObject(i);
                    String userId = request.getString("userId");
                    String userName = request.getString("username");
                    String profilePic = request.getString("profilePic");
                    SearchUser requestedUser = new SearchUser(userName, userId, profilePic);
                    sentRequests.add(requestedUser);
                }
                model.getSentRequests().postValue(sentRequests);
            }

            // Handling the add action
            else if (action.equals("add")) {
                JSONObject body = json.getJSONObject("body");
                String userId = body.getString("userId");
                String username = body.optString("username", null);
                String profilePic = body.optString("profilePic", null);

                Log.d("WebSocketClient", "Add friend: "+username);
                SearchUser newRequest = new SearchUser(username, userId, profilePic);

                // If request is from user that I sent request before remove from sent
                boolean friendAdded = false;
                List<SearchUser> sent = model.getSentRequests().getValue();
                if (sent != null) {
                    List<SearchUser> updatedSent = sent
                            .stream()
                            .filter(u -> !u.getId().equals(userId))
                            .collect(Collectors.toList());
                    if (updatedSent.size() < sent.size()) {
                        model.setSentRequestsList(updatedSent);
                        friendAdded = true;
                    }
                }

                // If request is from new user, add it to received request list
                if (!friendAdded) {
                    Log.d("WebSocketClient", "Friends Requested from "+username);
                    List<SearchUser> requestList = model.getReceivedRequests().getValue();
                    if (requestList == null) {
                        requestList = new ArrayList<>();
                    }
                    requestList.add(newRequest);
                    model.getReceivedRequests().postValue(requestList);
                }
            }

            // Handling the remove action
            else if (action.equals("remove")) {
                JSONObject body = json.getJSONObject("body");
                String userId = body.getString("userId");
                String username = body.optString("username", null);
                String profilePic = body.optString("profilePic", null);

                SearchUser rmvRequest = new SearchUser(username, userId, profilePic);
                model.removeRequest(model.getSentRequests(), rmvRequest);
                model.removeRequest(model.getReceivedRequests(), rmvRequest);

                Friend friendToRemove = new Friend(userId, username, profilePic);
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
                    webSocket.send(ByteString.EMPTY);  // Send an empty Ping frame
                }
                handler.postDelayed(this, PING_INTERVAL);
            }
        }, PING_INTERVAL);
    }
}
