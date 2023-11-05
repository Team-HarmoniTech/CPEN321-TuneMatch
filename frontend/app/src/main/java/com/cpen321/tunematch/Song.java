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

    // ChatGPT Usage: No
    public void setCurrentPosition(String timeStarted) {
        this.currentPosition = timeStarted;
    }

    // ChatGPT Usage: No
    public void setIsPLaying(Boolean isPLaying) {
        this.isPLaying = isPLaying;
    }

    // ChatGPT Usage: No
    public String getSongID() {
        return songID;
    }

    // ChatGPT Usage: No
    public String getDuration() {
        return duration;
    }

    // ChatGPT Usage: No
    public String getSongName() {
        return songName;
    }

    // ChatGPT Usage: No
    public String getSongArtist() {
        return songArtist;
    }

    // ChatGPT Usage: No
    public String getCurrentPosition() {
        return currentPosition;
    }

    // ChatGPT Usage: No
    public Boolean getIsPLaying() {
        return isPLaying;
    }

    // ChatGPT Usage: No
    @Override
    public String toString() {
        return this.getSongName();
    }

}

