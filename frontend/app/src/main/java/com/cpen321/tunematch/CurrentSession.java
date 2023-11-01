package com.cpen321.tunematch;

import java.util.ArrayList;
import java.util.List;

public class CurrentSession {
    private String sessionId;
    private String sessionName;
    private List<SessionUser> sessionMembers;
    private List<Song> sessionQueue;

    public CurrentSession(String name, String id) {
        sessionName = name;
        sessionId = id;
        sessionMembers = new ArrayList<>();
        sessionQueue = new ArrayList<>();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    // ... (Other Getters, Setters, and methods to manage sessionMembers and sessionQueue)
}
