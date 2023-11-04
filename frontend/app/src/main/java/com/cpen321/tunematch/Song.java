package com.cpen321.tunematch;

public class Song {
    private String songName;
    private String songID;
    private String duration;
    private String songArtist;
    private String currentPosition;
    private Boolean isPLaying;

    // ChatGPT Usage: No
    public Song(String songID, String songName, String Artist, String duration) {
        this.songID = songID;
        this.songName = songName;
        this.songArtist = Artist;
        this.duration = duration;
        this.isPLaying = false;
        this.currentPosition = "0";
    }
    public void setCurrentPosition(String timeStarted) {
        this.currentPosition = timeStarted;
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
    public String getCurrentPosition() {
        return currentPosition;
    }
    public Boolean getIsPLaying() {
        return isPLaying;
    }
    @Override
    public String toString() {
        return this.getSongName();
    }

}

