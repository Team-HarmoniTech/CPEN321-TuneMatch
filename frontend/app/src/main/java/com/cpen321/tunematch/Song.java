package com.cpen321.tunematch;

public class Song {
    private String songID;
    private String duration;

    // ChatGPT Usage: No
    public Song(String songID, String duration) {
        this.songID = songID;
        this.duration = duration;
    }

    public String getSongID() {
        return songID;
    }

    public String getDuration() {
        return duration;
    }
}

