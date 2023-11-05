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

    // ChatGPT Usage: No
    public void setTopArtists(ArrayList<String> topArtists) {this.topArtists = topArtists;}

    // ChatGPT Usage: No
    public void setTopGenres(ArrayList<String> topGenres) {this.topGenres = topGenres;}

    // ChatGPT Usage: No
    public ArrayList<String> getTopArtists() { return topArtists; }

    // ChatGPT Usage: No
    public ArrayList<String> getTopGenres() { return topGenres; }

    // ChatGPT Usage: No
    public void setBio(String bio) {
        this.bio = bio;
    }

    // ChatGPT Usage: No
    public String getBio() {
        return bio;
    }
}
