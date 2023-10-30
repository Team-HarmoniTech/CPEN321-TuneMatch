package com.cpen321.tunematch;

import android.util.Log;

public class Friend {
    private String id;
    private String username;
    private String profilePic;
    private String currentSong;
    private String currentSource;

    private String currentDuration;
    public Friend(String name, String id, String profilePic) {
        this.username = name;
        this.id = id;
        this.profilePic = profilePic;
    }

    public String getName() {
        return username;
    }
    public String getId() {return id;}
    public String getProfilePic() {return profilePic;}
    public String getCurrentSong() {return currentSong;}
    public String getCurrentSource() {return currentSource;}
    public boolean getIsListening() {
        Log.d("Friend", "current song: " + currentSong);
        if (currentSong.equals("null")) {
            return false;
        }
        return true;
    }
    public String getCurrentDuration() {return currentDuration;}

    public void setCurrentSong(String currentSong) {this.currentSong = currentSong;}
    public void setCurrentSource(String currentSource) {this.currentSource = currentSource;}
}
