package com.cpen321.tunematch;

public class Song {
    private String songName;
    private String songID;
    private String duration;
    private String songArtist;
    private String currentTimestamp;
    private Boolean isPLaying;

    // ChatGPT Usage: No
    public Song(String songID, String songName, String Artist, String duration) {
        this.songID = songID;
        this.songName = songName;
        this.songArtist = Artist;
        this.duration = duration;
        this.isPLaying = false;
        this.currentTimestamp = "0";
    }
    public void setCurrentTimestamp(String timeStarted) {
        this.currentTimestamp = timeStarted;
    }
    public void setIsPLaying(Boolean isPLaying) {
        this.isPLaying = isPLaying;
    }
    public String getSongID() {
        return songID;
    }
    public String getDuration() {
        return duration;
    }
    public String getSongName() {
        return songName;
    }
    public String getSongArtist() {
        return songArtist;
    }
    public String getCurrentTimestamp() {
        return currentTimestamp;
    }
    @Override
    public String toString() {
        return this.getSongName();
    }

}

