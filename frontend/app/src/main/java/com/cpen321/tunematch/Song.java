package com.cpen321.tunematch;

import java.util.Objects;

public class Song {
    private String songName;
    private String songID;
    private String duration;
    private String songArtist;
    private String currentPosition;
    private Boolean isPlaying;

    // ChatGPT Usage: No
    public Song(String songID, String songName, String Artist, String duration) {
        this.songID = songID;
        this.songName = songName;
        this.songArtist = Artist;
        this.duration = duration;
        this.isPlaying = false;
        this.currentPosition = "0";
    }
    public void setCurrentPosition(String timeStarted) {
        this.currentPosition = timeStarted;
    }
    public void setIsPLaying(Boolean isPLaying) {
        this.isPlaying = isPLaying;
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
    public boolean isPlaying() {
        return isPlaying;
    }
    // Equals method to compare songs
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(songID, song.songID) &&
                Objects.equals(songName, song.songName) &&
                Objects.equals(duration, song.duration) &&
                Objects.equals(songArtist, song.songArtist) &&
                Objects.equals(currentPosition, song.currentPosition) &&
                Objects.equals(isPlaying, song.isPlaying);
    }

    @Override
    public int hashCode() {
        return Objects.hash(songID, songName, songArtist, duration, currentPosition, isPlaying);
    }

    @Override
    public String toString() {
        return this.getSongName();
    }



}

