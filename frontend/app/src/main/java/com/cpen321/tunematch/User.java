package com.cpen321.tunematch;

import java.util.ArrayList;

// ChatGPT Usage: No
public class User {
    private String userId;
    private String userName;
    private String profileImageUrl;
    private ArrayList<String> topArtists;
    private ArrayList<String> topGenres;

    // Constructor
    // ChatGPT Usage: No
    public User(String userId, String userName, String profileImageUrl) {
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
    public String getProfilePic() {
        return profileImageUrl;
    }

    // ChatGPT Usage: No
    public String getProfileImageUrl() { return profileImageUrl; }

    public void setTopArtists(ArrayList<String> topArtists) {this.topArtists = topArtists;}
    public void setTopGenres(ArrayList<String> topGenres) {this.topGenres = topGenres;}
    public ArrayList<String> getTopArtists() { return topArtists; }
    public ArrayList<String> getTopGenres() { return topGenres; }

}
