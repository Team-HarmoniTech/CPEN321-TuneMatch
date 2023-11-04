package com.cpen321.tunematch;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.cpen321.tunematch.Message.timestampFormat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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
                    JsonParser parser = new JsonParser();
                    JsonObject json = parser.parse(text).getAsJsonObject();
                    String method = json.get("method").getAsString();
                    if (method.equals("FRIENDS")) {
                        handleFriends(json);
                    } else if (method.equals("SESSION")) {
                        handleSession(new JSONObject(text));
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


    private void handleFriends(JsonObject json) {

        try {
            String action = json.get("action").getAsString();
            if (action.equals("refresh")) {
                JsonArray body = json.get("body").getAsJsonArray();
                List<Friend> friends = new ArrayList<>();
                List<Session> sessions = new ArrayList<>();
                for (int i = 0; i < body.size(); i++) {
                    JsonObject friendJson = body.get(i).getAsJsonObject();
                    String id = friendJson.get("userId").getAsString();
                    String username = friendJson.get("username").getAsString();
                    String profilePic = getStringOrNull(friendJson.get("profilePic"));
                    String currentSong = getStringOrNull(friendJson.get("currentSong"));
                    JsonElement currentSource = friendJson.get("currentSource");

                    Friend friend = new Friend(id, username, profilePic);

                    friend.setCurrentSong(currentSong);
                    friend.setCurrentSource(currentSource);
                    friends.add(friend);

                    if (!currentSource.isJsonNull()) {
                        String sourceType = getStringOrNull(currentSource.getAsJsonObject().get("type"));
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
                String from = json.get("from").getAsString();
                List<Friend> existingFriendList = model.getFriendsList().getValue();
                List<Session> existingSessionList = model.getSessionList().getValue();
                for (Friend f : existingFriendList) {
                    if (f.getId().equals(from)) {
                        JsonObject body = json.get("body").getAsJsonObject();
                        String currentSong = getStringOrNull(body.get("currentSong"));
                        JsonElement currentSource = body.get("currentSource");
                        f.setCurrentSong(currentSong);
                        f.setCurrentSource(currentSource);
                        if (!currentSource.isJsonNull()) {
                            String sourceType = getStringOrNull(currentSource.getAsJsonObject().get("type"));
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

            // Handling any other unexpected actions
            else {
                Log.w("WebSocketClient", "Unknown friend action: " + action);
            }

        } catch (IllegalStateException e) {
            Log.e("WebSocketClient", "Error processing friend message", e);
        }
    }

    private void handleSession(JSONObject json) {

        try {
            String action = json.getString("action");
            if (action.equals("join")) {
                JSONObject body = json.getJSONObject("body");
                String userId = body.getString("userId");
                String username = body.optString("username", null);
                String profilePic = body.optString("profilePic", null);
                CurrentSession currentSession = model.getCurrentSession().getValue();
                List<User> sessionMembers = currentSession.getSessionMembers();
                User user = new User(userId, username, profilePic);
                sessionMembers.add(user);
                currentSession.setSessionMembers(sessionMembers);
                model.getCurrentSession().postValue(currentSession);
            }
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
                    currentSession.setSessionMembers(sessionMembers);
                    model.getCurrentSession().postValue(currentSession);
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


                if(members.length() == 0){
                    Log.d(TAG, "handleSession: you created a session instead of joining one");
                    List<Song> ExisitngSongQueue = model.getSongQueue().getValue();
                    if(ExisitngSongQueue==null){
                        ExisitngSongQueue = new ArrayList<>();
                    }
                    JSONArray songQueue = new JSONArray();
                    try {
                        Song currentSong = model.getCurrentSong().getValue();
                        JSONObject currSong = new JSONObject();
                        currSong.put("uri", currentSong.getSongID());
                        currSong.put("durationMs", currentSong.getDuration());
                        currSong.put("title", currentSong.getSongName());
                        currSong.put("artist", currentSong.getSongArtist());
                        songQueue.put(currSong);
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
                        JSONObject messageToResume = new JSONObject();
                        messageToResume.put("method", "SESSION");
                        messageToResume.put("action", "queueResume");
                        if (webSocket != null) {
                            webSocket.send(messageToReplaceQueue.toString());
                            webSocket.send(messageToResume.toString());
                            currentSession.setSessionQueue(ExisitngSongQueue);

                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                else{
                    if(currentlyPlaying != null){
                        Log.d(TAG, "handleSession: you joined a session instead of creating one");
                        String songId = currentlyPlaying.getString("uri");
                        String songName = currentlyPlaying.getString("title");
                        String songArtist = currentlyPlaying.getString("artist");
                        String duration = currentlyPlaying.getString("durationMs");
                        Date timeStarted = timestampFormat.parse(currentlyPlaying.getString("timeStarted"));
                        long elapsedMs = System.currentTimeMillis() - timeStarted.getTime();
                        Song currentSong = new Song(songId, songName, songArtist, duration);
                        currentSong.setCurrentPosition(elapsedMs+"");
                        currentSong.setIsPLaying(true);
                        currentSession.setCurrentSong(currentSong);
                        model.getCurrentSong().postValue(currentSong);
                    }
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
                Log.d(TAG, "handleSession: queueSkip has been initiated");
                Log.d(TAG, "handleSession: queueSkip: " + model.getCurrentSong().getValue().getSongName()+" :: "+model.getSongQueue().getValue().get(0).getSongName());
                CurrentSession currentSession = model.getCurrentSession().getValue();
                List<Song> sessionQueue = model.getSongQueue().getValue();
                Song currentSong = sessionQueue.get(0);
                sessionQueue.remove(0);
                currentSession.setSessionQueue(sessionQueue);
                currentSession.setCurrentSong(currentSong);
                model.getCurrentSession().postValue(currentSession);
                model.getCurrentSong().postValue(currentSong);
                model.getSongQueue().postValue(sessionQueue);
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
                Log.d(TAG, "handleSession: seekPosition: " + seekPosition);
                seekPosition = seekPosition / 1000;
                Song currentSong = model.getCurrentSong().getValue();
                currentSong.setCurrentPosition(seekPosition+"");
                model.getCurrentSong().postValue(currentSong);
            }
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
            else {
                Log.w("WebSocketClient", "Unknown session action: " + action);
            }

        } catch (JSONException | ParseException | IOException e) {
            Log.e("WebSocketClient", "Error processing session message", e);
        }
    }

    // ChatGPT Usage: No
    private void handleRequests(JsonObject json){
        try {
            String action = json.get("action").getAsString();
            JsonObject body = json.get("body").getAsJsonObject();

            // Handling the refresh action
            if (action.equals("refresh")) {
                JsonArray requesting = body.get("requesting").getAsJsonArray();
                JsonArray requested = body.get("requested").getAsJsonArray();
                // TODO: Update the Redux store with the refreshed lists of requesting and requested users
                List<SearchUser> newRequests = model.getReceivedRequests().getValue();
                if (newRequests == null) {
                    newRequests = new ArrayList<SearchUser>();
                }
                for (int i = 0; i < requesting.size(); i++) {
                    JsonObject request = requesting.get(i).getAsJsonObject();
                    String userId = request.get("userId").getAsString();
                    String userName = request.get("username").getAsString();
                    String profilePic = getStringOrNull(request.get("profilePic"));
                    SearchUser requestingUser = new SearchUser(userName, userId, profilePic);
                    newRequests.add(requestingUser);
                }
                model.getReceivedRequests().postValue(newRequests);

                List<SearchUser> sentRequests = model.getSentRequests().getValue();
                if (sentRequests == null) {
                    sentRequests = new ArrayList<SearchUser>();
                }
                for (int i = 0; i < requested.size(); i++) {
                    JsonObject request = requested.get(i).getAsJsonObject();
                    String userId = request.get("userId").getAsString();
                    String userName = request.get("username").getAsString();
                    String profilePic = getStringOrNull(request.get("profilePic"));
                    SearchUser requestedUser = new SearchUser(userName, userId, profilePic);
                    sentRequests.add(requestedUser);
                }
                model.getSentRequests().postValue(sentRequests);
            }

            // Handling the add action
            else if (action.equals("add")) {
                String userId = body.get("userId").getAsString();
                String username = body.get("username").getAsString();
                String profilePic = getStringOrNull(body.get("profilePic"));
                String currentSong = getStringOrNull(body.get("currentSong"));
                JsonElement currentSource = body.get("currentSource");

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
                        Log.d("WebSocketClient", "Friend "+username+" added");
                        model.setSentRequestsList(updatedSent);
                        friendAdded = true;

                        Friend newFriend = new Friend(userId, username, profilePic);
                        newFriend.setCurrentSong(currentSong);
                        if (!currentSource.isJsonNull()) {
                            newFriend.setCurrentSource(currentSource);
                        }
                        List<Friend> friendList = model.getFriendsList().getValue();
                        if (friendList == null) {
                            friendList = new ArrayList<Friend>();
                        }
                        friendList.add(newFriend);
                        model.getFriendsList().postValue(friendList);

                        // Check permission
                        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            CharSequence name = "New Friend";
                            int importance = NotificationManager.IMPORTANCE_DEFAULT;
                            NotificationChannel channel = new NotificationChannel("friend_request_channel", name, importance);
                            notification.createNotificationChannel(channel);

                            // Build the notification
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel.getId())
                                    .setSmallIcon(R.drawable.default_profile_image)
                                    .setContentTitle("New Friend")
                                    .setContentText(username+" accepted your friend request")
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setAutoCancel(true); // Automatically removes the notification when the user taps it

                            // Show the notification
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                            notificationManager.notify(1, builder.build());
                        }
                    }
                }

                // If request is from new user, add it to received request list
                if (!friendAdded && !model.inReceivedRequest(newRequest)) {
                    Log.d("WebSocketClient", "Friends Requested from " + username);
                    List<SearchUser> requestList = model.getReceivedRequests().getValue();
                    if (requestList == null) {
                        requestList = new ArrayList<SearchUser>();
                    }
                    requestList.add(newRequest);
                    model.getReceivedRequests().postValue(requestList);

                    // Check permission
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    CharSequence name = "New Friend";
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    NotificationChannel channel = new NotificationChannel("friend_request_channel", name, importance);
                    notification.createNotificationChannel(channel);

                    // Build the notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel.getId())
                            .setSmallIcon(R.drawable.default_profile_image)
                            .setContentTitle("New Friend")
                            .setContentText("You have received new request from " + username)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true); // Automatically removes the notification when the user taps it

                    // Show the notification
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(1, builder.build());
                }
            }

            // Handling the remove action
            else if (action.equals("remove")) {
                String userId = body.get("userId").getAsString();

                model.removeRequest(model.getSentRequests(), userId);
                model.removeRequest(model.getReceivedRequests(), userId);
                model.removeFriend(userId);
            }

            // Handling any other unexpected actions
            else {
                Log.w("WebSocketClient", "Unknown request action: " + action);
            }

        } catch (IllegalStateException e) {
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

    public static String getStringOrNull(JsonElement jsonElement) {
        if (jsonElement != null && !jsonElement.isJsonNull() && jsonElement.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            if (jsonPrimitive.isString()) {
                return jsonPrimitive.getAsString();
            }
        }
        return null;
    }
}
