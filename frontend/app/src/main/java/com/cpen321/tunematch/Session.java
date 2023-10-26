package com.cpen321.tunematch;

import java.util.List;

public class Session {
    private String roomName;
    private List<Message> messages;

    public Session(String roomName, List<Message> messages) {
        this.roomName = roomName;
        this.messages = messages;
    }

    // getters and setters
}
