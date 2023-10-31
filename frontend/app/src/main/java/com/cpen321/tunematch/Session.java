package com.cpen321.tunematch;

import java.util.List;

public class Session {
    private String sessionId;
    private String roomName;
    public Session(String sessionId, String roomName) {
        this.roomName = roomName;
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getRoomName() {
        return roomName;
    }

}
