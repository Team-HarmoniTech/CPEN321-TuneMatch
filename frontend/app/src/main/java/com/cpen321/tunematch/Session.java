package com.cpen321.tunematch;

import java.util.List;

public class Session {
    private String sessionId;
    private String roomName;

    // ChatGPT Usage: No
    public Session(String sessionId, String roomName) {
        this.roomName = roomName;
        this.sessionId = sessionId;
    }

    // ChatGPT Usage: No
    public String getSessionId() {
        return sessionId;
    }

    // ChatGPT Usage: No
    public String getRoomName() {
        return roomName;
    }

}
