package com.cpen321.tunematch;

import android.util.Log;

import org.json.JSONObject;

public class Friend {
    private String id;
    private String username;
    private String profilePic;
    private String currentSong;
    private JSONObject currentSource;
    private String currentDuration;

    // ChatGPT Usage: No
    public Friend(String name, String id, String profilePic) {
        this.username = name;
        this.id = id;
        this.profilePic = profilePic;
    }

    // ChatGPT Usage: No
    public String getName() {
        return username;
    }

    // ChatGPT Usage: No
    public String getId() {return id;}

    // ChatGPT Usage: No
    public String getProfilePic() {return profilePic;}

    // ChatGPT Usage: No
    public String getCurrentSong() {return currentSong;}

    // ChatGPT Usage: No
    public JSONObject getCurrentSource() {return currentSource;}

    // ChatGPT Usage: No
    public boolean getIsListening() {
        Log.d("Friend", "current song: " + currentSong);
        if (currentSong.equals("null")) {
            return false;
        }
        return true;
    }

    // ChatGPT Usage: No
    public String getCurrentDuration() {return currentDuration;}

    // ChatGPT Usage: No
    public void setCurrentSong(String currentSong) {this.currentSong = currentSong;}

    // ChatGPT Usage: No
    public void setCurrentSource(JSONObject currentSource) {this.currentSource = currentSource;}
}
