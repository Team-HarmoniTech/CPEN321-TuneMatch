package com.cpen321.tunematch;

import android.util.Log;

import org.json.JSONObject;

public class Friend extends User {
    private String currentSong;
    private JSONObject currentSource;

    // ChatGPT Usage: No
    public Friend(String userId, String userName, String profileImageUrl) {
        super(userId, userName, profileImageUrl);
        currentSong = null;
        currentSource = null;
    }

    // ChatGPT Usage: No
    public String getName() {
        return getUserName();
    }

    // ChatGPT Usage: No
    public String getId() { return getUserId(); }

    // ChatGPT Usage: No
    public String getProfilePic() { return getProfileImageUrl(); }

    // ChatGPT Usage: No
    public String getCurrentSong() { return currentSong; }

    // ChatGPT Usage: No
    public JSONObject getCurrentSource() { return currentSource; }

    // ChatGPT Usage: No
    public boolean getIsListening() {
        Log.d("Friend", "current song: " + currentSong);
        if (currentSong.equals("null")) {
            return false;
        }
        return true;
    }

    // ChatGPT Usage: No
    public void setCurrentSong(String currentSong) { this.currentSong = currentSong; }

    // ChatGPT Usage: No
    public void setCurrentSource(JSONObject currentSource) { this.currentSource = currentSource; }
}
