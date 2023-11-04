package com.cpen321.tunematch;

import java.util.Date;

public class Message {
    private SessionUser sender;
    private String messageText;
    private Date timestamp;

    // ChatGPT Usage: No
    public Message(SessionUser sender, String messageText, Date timestamp) {
        this.sender = sender;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    // ChatGPT Usage: No
    public Date getTimestamp() {
        return timestamp;
    }

    // ChatGPT Usage: No
    public String getMessageText() {
        return messageText;
    }

    // ChatGPT Usage: No
    public String getSenderUserId() {
        return sender.getUserId();
    }

    // ChatGPT Usage: No
    public String getSenderProfileImageUrl() {
        return sender.getProfileImageUrl();
    }

    public String getSenderUsername() {
        return sender.getUserName();
    }
}

