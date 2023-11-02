package com.cpen321.tunematch;

// ChatGPT Usage: No
public class SessionUser {
    private String userId;
    private String userName;
    private String profileImageUrl;

    // Constructor
    // ChatGPT Usage: No
    public SessionUser(String userId, String userName, String profileImageUrl) {
        this.userId = userId;
        this.userName = userName;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters and Setters
    // ChatGPT Usage: No
    public String getUserId() {
        return userId;
    }

    // ChatGPT Usage: No
    public String getUserName() {
        return userName;
    }

    // ChatGPT Usage: No
    public void setUserName(String userName) {
        this.userName = userName;
    }

    // ChatGPT Usage: No
    public String getProfileImageUrl() { return profileImageUrl; }
}
