package com.cpen321.tunematch;

import java.util.ArrayList;

// ChatGPT Usage: No
public class User {
    private String userId;
    private String userName;
    private String profileImageUrl;
    private String bio;
    private ArrayList<String> topArtists;
    private ArrayList<String> topGenres;

    // Constructor
    // ChatGPT Usage: No
    public User(String userId, String userName, String profileImageUrl) {
        this.userId = userId;
        this.userName = userName;
        this.profileImageUrl = profileImageUrl;
    }

    // ChatGPT Usage: No
    public User(String userId, String userName, String profileImageUrl, String bio, ArrayList<String> topArtists, ArrayList<String> topGenres) {
        this.userId = userId;
        this.userName = userName;
        this.profileImageUrl = profileImageUrl;
        this.bio = bio;
        this.topArtists = topArtists;
        this.topGenres = topGenres;
    }

    // Getters and Setters
    // ChatGPT Usage: No
    public String getUserId() {
        return userId;
    }
    public String getUserName() {
        return userName;
    }
    public String getProfilePic() {
        return profileImageUrl;
    }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setTopArtists(ArrayList<String> topArtists) {this.topArtists = topArtists;}
    public void setTopGenres(ArrayList<String> topGenres) {this.topGenres = topGenres;}
    public ArrayList<String> getTopArtists() { return topArtists; }
    public ArrayList<String> getTopGenres() { return topGenres; }
    public void setBio(String bio) {
        this.bio = bio;
    }
    public String getBio() {
        return bio;
    }
}
