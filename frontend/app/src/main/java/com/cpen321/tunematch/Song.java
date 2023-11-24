package com.cpen321.tunematch;

import java.util.Objects;

public class Song {
    private final String songName;
    private final String songID;
    private final long duration;
    private final String songArtist;
    private String songAlbum;
    private long currentPosition;
    private Boolean isPlaying;

    // ChatGPT Usage: No
    public Song(String songID, String songName, String artist, long duration) {
        this.songID = songID;
        this.songName = songName;
        this.songArtist = artist;
        this.duration = duration;
        this.isPlaying = false;
        this.currentPosition = 0;
    }

    public String getSongAlbum() {
        return songAlbum;
    }

    public void setSongAlbum(String songAlbum) {
        this.songAlbum = songAlbum;
    }

    // ChatGPT Usage: No
    public void setIsPLaying(Boolean isPLaying) {
        this.isPlaying = isPLaying;
    }

    // ChatGPT Usage: No
    public String getSongID() {
        return songID;
    }

    // ChatGPT Usage: No
    public long getDuration() {
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
    public long getCurrentPosition() {
        return currentPosition;
    }

    // ChatGPT Usage: No
    public void setCurrentPosition(long timeStarted) {
        this.currentPosition = timeStarted;
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

    // ChatGPT Usage: No
    @Override
    public String toString() {
        return this.getSongName();
    }

}

