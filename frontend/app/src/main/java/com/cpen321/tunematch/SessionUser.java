package com.cpen321.tunematch;

public class SessionUser {
    private String userId;
    private String userName;
    private boolean isHost; // Indicates if this user is the host of the session
    private boolean isListening; // Indicates if this user is currently listening to the session music

    // Constructor
    public SessionUser(String userId, String userName, boolean isHost, boolean isListening) {
        this.userId = userId;
        this.userName = userName;
        this.isHost = isHost;
        this.isListening = isListening;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean isHost) {
        this.isHost = isHost;
    }

    public boolean isListening() {
        return isListening;
    }

    public void setListening(boolean isListening) {
        this.isListening = isListening;
    }

    // Additional Methods
    // For example, you may have methods to manage user's actions within a session.
    public void toggleListening() {
        isListening = !isListening;
    }
}
