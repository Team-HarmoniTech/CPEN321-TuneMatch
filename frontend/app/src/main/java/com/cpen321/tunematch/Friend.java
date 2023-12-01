package com.cpen321.tunematch;

import com.google.gson.JsonElement;

import java.util.Date;

public class Friend extends User {
    private Song currentSong;
    private JsonElement currentSource;
    private Date lastUpdated;

    // ChatGPT Usage: No
    public Friend(String userId, String userName, String profileImageUrl, Song currentSong, JsonElement currentSource, Date lastUpdated) {
        super(userId, userName, profileImageUrl);
        this.currentSong = currentSong;
        this.currentSource = currentSource;
        this.lastUpdated = lastUpdated;
    }

    // ChatGPT Usage: No
    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }

    public JsonElement getCurrentSource() {
        return currentSource;
    }

    public void setCurrentSource(JsonElement currentSource) {
        this.currentSource = currentSource;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
