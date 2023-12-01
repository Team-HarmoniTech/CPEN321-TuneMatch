package com.cpen321.tunematch;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.cpen321.tunematch.Message.timestampFormat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.MutableLiveData;

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
    private final OkHttpClient client;
    private final Context context;
    private final NotificationManager notification;
    ReduxStore model;
    private WebSocket webSocket;

    // ChatGPT Usage: Partial
    public WebSocketClient(ReduxStore model, Context context, NotificationManager notification) {
        this.context = context;
        this.client = new OkHttpClient();
        this.model = model;
        this.notification = notification;
    }

    // ChatGPT Usage: Partial
    public static String getStringOrNull(JsonElement jsonElement) {
        if (jsonElement != null && !jsonElement.isJsonNull() && jsonElement.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            if (jsonPrimitive.isString()) {
                return jsonPrimitive.getAsString();
            }
        }
        return null;
    }

    // ChatGPT Usage: Partial
    public void start(Headers customHeader) {
//        String url = "ws://10.0.2.2:3000/socket";
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
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("WebSocketClient", "Received message: " + text);
                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(text).getAsJsonObject();
                String method = json.get("method").getAsString();
                if (method.equals("FRIENDS")) {
                    handleFriends(json);
                } else if (method.equals("SESSION")) {
                    handleSession(json);
                } else if (method.equals("REQUESTS")) {
                    handleRequests(json);
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

    // ChatGPT Usage: Partial
    public void stop() {
        if (webSocket != null) {
            webSocket.close(1000, "Goodbye!");
        }
    }

    // ChatGPT Usage: Partial
    public void sendMessage(String message) {
        if (webSocket != null) {
            Log.d("WebSocketClient", "Sending message: " + message);
            webSocket.send(message);
        }
    }

    // ChatGPT Usage: Partial
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
                    Date lastUpdated = timestampFormat.parse(friendJson.get("lastUpdated").getAsString());
                    JsonElement currentSong = friendJson.get("currentSong");
                    JsonElement currentSource = friendJson.get("currentSource");

                    Song song = null;
                    if (!currentSong.isJsonNull()) {
                        song = new Song(
                                currentSong.getAsJsonObject().get("uri").getAsString(),
                                currentSong.getAsJsonObject().get("name").getAsString(),
                                currentSong.getAsJsonObject().get("artist").getAsString(),
                                currentSong.getAsJsonObject().get("durationMs").getAsLong()
                        );
                        song.setSongAlbum(currentSong.getAsJsonObject().get("album").getAsString());
                    }

                    Friend friend = new Friend(id, username, profilePic, song, currentSource, lastUpdated);
                    friends.add(friend);

                    if (!currentSource.isJsonNull()) {
                        String sourceType = getStringOrNull(currentSource.getAsJsonObject().get("type"));
                        if (sourceType.equals("session")) {
                            sessions.add(new Session(friend.getUserId(), friend));
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
//              check if from exists in the message
                JsonObject body = json.get("body").getAsJsonObject();
                String userId = body.get("userId").getAsString();
                List<Friend> friendList = model.getFriendsList().getValue();
                List<Session> sessionList = model.getSessionList().getValue();
                for (Friend f : friendList) {
                    if (f.getUserId().equals(userId)) {
                        JsonElement currentSong = body.get("currentSong");
                        JsonElement currentSource = body.get("currentSource");
                        Date lastUpdated = timestampFormat.parse(body.get("lastUpdated").getAsString());
                        if (!currentSong.isJsonNull()) {
                            Song song = new Song(
                                    currentSong.getAsJsonObject().get("uri").getAsString(),
                                    currentSong.getAsJsonObject().get("name").getAsString(),
                                    currentSong.getAsJsonObject().get("artist").getAsString(),
                                    currentSong.getAsJsonObject().get("durationMs").getAsLong()
                            );
                            song.setSongAlbum(currentSong.getAsJsonObject().get("album").getAsString());
                            f.setCurrentSong(song);
                        }
                        f.setCurrentSource(currentSource);
                        f.setLastUpdated(lastUpdated);
                        if (!currentSource.isJsonNull()) {
                            String sourceType = getStringOrNull(currentSource.getAsJsonObject().get("type"));
                            if (sourceType.equals("session")) {
                                boolean sessionExists = false;

                                for (Session s : sessionList) {
                                    if (s.getSessionId().equals(userId)) {
                                        sessionExists = true;
                                        break;
                                    }
                                }
                                if (!sessionExists) {
                                   sessionList.add(new Session(f.getUserId(), f));
                                }
                            }
                        } else {
                            for (Session s : sessionList) {
                                if (s.getSessionId().equals(userId)) {
                                    sessionList.remove(s);
                                    break;
                                }
                            }
                        }
                        model.getFriendsList().postValue(friendList);
                        model.getSessionList().postValue(sessionList);
                        return;
                    }
                }

                // Add the friend
                JsonObject friendJson = json.get("body").getAsJsonObject();
                String id = friendJson.get("userId").getAsString();
                String username = friendJson.get("username").getAsString();
                String profilePic = getStringOrNull(friendJson.get("profilePic"));
                Date lastUpdated = timestampFormat.parse(friendJson.get("lastUpdated").getAsString());
                JsonElement currentSong = friendJson.get("currentSong");
                JsonElement currentSource = friendJson.get("currentSource");

                Song song = null;
                if (!currentSong.isJsonNull()) {
                    song = new Song(
                            currentSong.getAsJsonObject().get("uri").getAsString(),
                            currentSong.getAsJsonObject().get("name").getAsString(),
                            currentSong.getAsJsonObject().get("artist").getAsString(),
                            currentSong.getAsJsonObject().get("durationMs").getAsLong()
                    );
                    song.setSongAlbum(currentSong.getAsJsonObject().get("album").getAsString());
                }

                Friend friend = new Friend(id, username, profilePic, song, currentSource, lastUpdated);
                friendList.add(friend);

                if (!currentSource.isJsonNull()) {
                    String sourceType = getStringOrNull(currentSource.getAsJsonObject().get("type"));
                    if (sourceType.equals("session")) {
                        sessionList.add(new Session(friend.getUserId(), friend));
                    }
                }

                model.getFriendsList().postValue(friendList);
                model.getSessionList().postValue(sessionList);
            }
            // Handling any other unexpected actions
            else {
                Log.w("WebSocketClient", "Unknown friend action: " + action);
            }

        } catch (IllegalStateException | ParseException e) {
            Log.e("WebSocketClient", "Error processing friend message", e);
        }
    }

    // ChatGPT Usage: Partial
    private void handleSession(JsonObject json) {

        try {
            String action = json.get("action").getAsString();
            if (action.equals("join")) {
                JsonObject body = json.get("body").getAsJsonObject();
                String userId = body.get("userId").getAsString();
                String username = body.get("username").getAsString();
                String profilePic = getStringOrNull(body.get("profilePic"));
                CurrentSession currentSession = model.getCurrentSession().getValue();
                if (currentSession == null) {
                    currentSession = new CurrentSession("session", "MySession");
                }
                List<User> sessionMembers = currentSession.getSessionMembers();
                if (sessionMembers == null) {
                    sessionMembers = new ArrayList<>();
                }
                User user = new User(userId, username, profilePic);
                sessionMembers.add(user);
                currentSession.setSessionMembers(sessionMembers);
                model.getCurrentSession().postValue(currentSession);
            } else if (action.equals("leave")) {
                JsonObject body = json.get("body").getAsJsonObject();
                String userId = body.get("userId").getAsString();
                CurrentSession currentSession = model.getCurrentSession().getValue();
                if (currentSession == null) {
                    currentSession = new CurrentSession("session", "MySession");
                }
                List<User> sessionMembers = currentSession.getSessionMembers();
                if (sessionMembers == null) {
                    sessionMembers = new ArrayList<>();
                }
                for (User s : sessionMembers) {
                    if (s.getUserId().equals(userId)) {
                        sessionMembers.remove(s);
                        break;
                    }
                }
                currentSession.setSessionMembers(sessionMembers);
                model.getCurrentSession().postValue(currentSession);
            } else if (action.equals("refresh")) {
                JsonObject body = json.get("body").getAsJsonObject();
                JsonArray members = body.get("members").getAsJsonArray();
                JsonArray queue = body.get("queue").getAsJsonArray();
                CurrentSession currentSession = model.getCurrentSession().getValue();

                if (currentSession == null) {
                    currentSession = new CurrentSession("session", "MySession");
                }

                List<User> sessionMembers = new ArrayList<>();
                List<Song> sessionQueue = new ArrayList<>();

                for (int i = 0; i < members.size(); i++) {
                    JsonObject member = members.get(i).getAsJsonObject();
                    String id = member.get("userId").getAsString();
                    String username = member.get("username").getAsString();
                    String profilePic = getStringOrNull(member.get("profilePic"));
                    User user = new User(id, username, profilePic);
                    sessionMembers.add(user);
                }
//                session was created by current user
                if (members.size() == 0) {
                    model.checkSessionCreatedByMe().postValue(true);
                    Log.d(TAG, "handleSession: you created a session instead of joining one");
                    List<Song> ExisitngSongQueue = model.getSongQueue().getValue();
                    if (ExisitngSongQueue == null) {
                        ExisitngSongQueue = new ArrayList<>();
                    }
                    JSONArray songQueue = new JSONArray();
                    try {
                        Song currentSong = model.getCurrentSong().getValue();
                        JSONObject currSong = new JSONObject();
                        if (currentSong != null) {
                            currSong.put("uri", currentSong.getSongID());
                            currSong.put("durationMs", currentSong.getDuration());
                            currSong.put("title", currentSong.getSongName());
                            currSong.put("artist", currentSong.getSongArtist());
                            songQueue.put(currSong);
                        }
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
                            Log.e(TAG, "handleSession: replace queue now:: " + messageToReplaceQueue);
                            webSocket.send(messageToReplaceQueue.toString());
                            currentSession.setSessionQueue(ExisitngSongQueue);
                        }
                    } catch (JSONException e) {
                        Log.e("JSONException", "Exception message: " + e.getMessage());
                    }
                } else { //session was joined by current user
                    model.checkSessionCreatedByMe().postValue(false);
                    Log.e(TAG, "handleSession: you joined a session instead of creating one");
                    model.getCurrentSong().postValue(null);
                    Log.e(TAG, "handleSession: current song is null now");
                    for (int i = 0; i < queue.size(); i++) {
                        JsonObject song = queue.get(i).getAsJsonObject();
                        String songId = song.get("uri").getAsString();
                        long duration = song.get("durationMs").getAsLong();
                        String songName = song.get("title").getAsString();
                        String songArtist = song.get("artist").getAsString();
                        Song songToAdd = new Song(songId, songName, songArtist, duration);
                        Log.e(TAG, "handleSession: adding song to queue:: " + songToAdd.getSongName() + " :: " + songToAdd.getSongArtist() + " :: " + songToAdd.getSongID() + " :: " + songToAdd.getDuration() + " :: " + songToAdd.getCurrentPosition());
                        sessionQueue.add(songToAdd);
                    }
                    currentSession.setSessionQueue(sessionQueue);
                    model.getSongQueue().postValue(sessionQueue);
                }
                currentSession.setSessionMembers(sessionMembers);
                currentSession.setSessionId("session");
                model.checkSessionActive().postValue(true);
            } else if (action.equals("queueAdd")) {
                JsonObject songDetails = json.get("body").getAsJsonObject();
                String songId = songDetails.get("uri").getAsString();
                long duration = songDetails.get("durationMs").getAsLong();
                String songName = songDetails.get("title").getAsString();
                String songArtist = songDetails.get("artist").getAsString();
                Song songToAdd = new Song(songId, songName, songArtist, duration);
                List<Song> sessionQueue = model.getSongQueue().getValue();
                if (sessionQueue == null) {
                    sessionQueue = new ArrayList<>();
                }
                sessionQueue.add(songToAdd);
                model.getSongQueue().postValue(sessionQueue);
                CurrentSession currentSession = model.getCurrentSession().getValue();
                if (currentSession == null) {
                    currentSession = new CurrentSession("session", "MySession");
                }
                currentSession.setSessionQueue(sessionQueue);
                model.getCurrentSession().postValue(currentSession);
            } else if (action.equals("queueSkip")) {
                Log.d(TAG, "handleSession: queueSkip has been initiated");
                Log.d(TAG, "handleSession: queueSkip: " + model.getCurrentSong().getValue().getSongName() + " :: " + model.getSongQueue().getValue().get(0).getSongName());
                CurrentSession currentSession = model.getCurrentSession().getValue();
                if (currentSession == null) {
                    currentSession = new CurrentSession("session", "MySession");
                }
                List<Song> sessionQueue = model.getSongQueue().getValue();
                if (sessionQueue == null) {
                    sessionQueue = new ArrayList<>();
                }
                Song currentSong = sessionQueue.get(0);
                sessionQueue.remove(0);
                currentSession.setSessionQueue(sessionQueue);
                currentSession.setCurrentSong(currentSong);
                model.getCurrentSession().postValue(currentSession);
                model.getCurrentSong().postValue(currentSong);
                model.getSongQueue().postValue(sessionQueue);
            } else if (action.equals("queueDrag")) {
                JsonObject body = json.get("body").getAsJsonObject();
                int startIndex = body.get("startIndex").getAsInt();
                int endIndex = body.get("endIndex").getAsInt();
                CurrentSession currentSession = model.getCurrentSession().getValue();
                if (currentSession == null) {
                    currentSession = new CurrentSession("session", "MySession");
                }
                List<Song> sessionQueue = currentSession.getSessionQueue();
                if (sessionQueue == null) {
                    sessionQueue = new ArrayList<>();
                }
                Song songToMove = sessionQueue.get(startIndex);
                sessionQueue.remove(startIndex);
                sessionQueue.add(endIndex, songToMove);
                currentSession.setSessionQueue(sessionQueue);
                model.getCurrentSession().postValue(currentSession);
                model.getSongQueue().postValue(sessionQueue);
            } else if (action.equals("queuePause")) {
                Song currentSong = model.getCurrentSong().getValue();
                currentSong.setIsPLaying(false);
                model.getCurrentSong().postValue(currentSong);
            } else if (action.equals("queueResume")) {
                Song currentSong = model.getCurrentSong().getValue();
                currentSong.setIsPLaying(true);
                model.getCurrentSong().postValue(currentSong);
            } else if (action.equals("queueSeek")) {
                JsonObject body = json.get("body").getAsJsonObject();
                long seekPosition = body.get("seekPosition").getAsLong();
                Log.d(TAG, "handleSession: seekPosition: " + seekPosition);
                Song currentSong = model.getCurrentSong().getValue();
                currentSong.setCurrentPosition(seekPosition);
                model.getCurrentSong().postValue(currentSong);
            } else if (action.equals("message")) {
                JsonObject messageDetails = json.get("body").getAsJsonObject();
                String from = json.get("from").getAsString();

                CurrentSession currentSession = model.getCurrentSession().getValue();
                if (currentSession == null) {
                    currentSession = new CurrentSession("session", "MySession");
                }
                List<User> sessionMembers = currentSession.getSessionMembers();
                if (sessionMembers == null) {
                    sessionMembers = new ArrayList<>();
                }
                User sender = sessionMembers.stream()
                        .filter(member -> member.getUserId().equals(from))
                        .findFirst()
                        .orElse(null);

                Message message = new Message(sender,
                        messageDetails.get("message").getAsString(),
                        timestampFormat.parse(messageDetails.get("timestamp").getAsString()));
                Log.d("WebSocket", message.toString());

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
                } catch (IllegalStateException ignored) {
                }

                // Show the notification
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(1, builder.build());

            } else {
                Log.w("WebSocketClient", "Unknown session action: " + action);
            }

        } catch (ParseException | IOException e) {
            Log.e("WebSocketClient", "Error processing session message", e);
        }
    }

    // ChatGPT Usage: Partial
    private void handleRequests(JsonObject json) {
        try {
            String action = json.get("action").getAsString();
            JsonObject body = json.get("body").getAsJsonObject();

            // Handling the refresh action
            switch (action) {
                case "refresh":
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
                    break;

                // Handling the add action
                case "add": {
                    String userId = body.get("userId").getAsString();
                    String username = body.get("username").getAsString();
                    String profilePic = getStringOrNull(body.get("profilePic"));
                    JsonElement currentSong = body.get("currentSong");
                    JsonElement currentSource = body.get("currentSource");
                    Date lastUpdated = timestampFormat.parse(body.get("lastUpdated").getAsString());

                    Log.d("WebSocketClient", "Add friend: " + username);
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
                            Log.d("WebSocketClient", "Friend " + username + " added");
                            model.setSentRequestsList(updatedSent);
                            friendAdded = true;

                            Song song = null;
                            if (!currentSong.isJsonNull()) {
                                song = new Song(
                                        currentSong.getAsJsonObject().get("uri").getAsString(),
                                        currentSong.getAsJsonObject().get("name").getAsString(),
                                        currentSong.getAsJsonObject().get("artist").getAsString(),
                                        currentSong.getAsJsonObject().get("durationMs").getAsLong()
                                );
                                song.setSongAlbum(currentSong.getAsJsonObject().get("album").getAsString());
                            }


                            List<Friend> friendList = model.getFriendsList().getValue();
                            friendList.add(new Friend(userId, username, profilePic, song, currentSource, lastUpdated));
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
                                        .setContentText(username + " accepted your friend request")
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
                    break;
                }

                // Handling the remove action
                case "remove": {
                    String userId = body.get("userId").getAsString();

                    model.removeRequest(model.getSentRequests(), userId);
                    model.removeRequest(model.getReceivedRequests(), userId);
                    model.removeFriend(userId);
                    break;
                }

                // Handling any other unexpected actions
                default:
                    Log.w("WebSocketClient", "Unknown request action: " + action);
                    break;
            }

        } catch (IllegalStateException | ParseException e) {
            Log.e("WebSocketClient", "Error processing request message", e);
        }
    }
}
