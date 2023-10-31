package com.cpen321.tunematch;

public class Message {
    private String userId;
    private String messageText;
    private long timestamp;

    public Message(String userId, String messageText, long timestamp) {
        this.userId = userId;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    // ... (Getters and Setters)
}

