package com.cpen321.tunematch;

public class CurrentSong {
    private String songID;
    private String duration;
    private String timeStarted;
    // ChatGPT Usage: No
    public CurrentSong(String songID, String duration, String timeStarted) {
        this.songID = songID;
        this.duration = duration;
        this.timeStarted = timeStarted;
    }
}
