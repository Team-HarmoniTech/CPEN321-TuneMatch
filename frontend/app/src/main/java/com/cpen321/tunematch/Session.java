package com.cpen321.tunematch;

public class Session {
    private final String sessionId;
    private final Friend member;

    // ChatGPT Usage: No
    public Session(String sessionId, Friend member) {
        this.member = member;
        this.sessionId = sessionId;
    }

    // ChatGPT Usage: No
    public String getSessionId() {
        return sessionId;
    }

    // ChatGPT Usage: No
    public Friend getSessionMember() {
        return member;
    }
}
