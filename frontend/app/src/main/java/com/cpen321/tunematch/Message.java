package com.cpen321.tunematch;

public class Message {
    private String sender;
    private String content;
    private long timestamp;

    public Message(String sender, String content, long timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    // getters and setters
}

