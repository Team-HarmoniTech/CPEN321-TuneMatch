package com.cpen321.tunematch;

import java.util.Date;

public class Message implements Comparable<Message> {
    private User sender;
    private String messageText;
    private Date timestamp;

    // ChatGPT Usage: No
    public Message(User sender, String messageText, Date timestamp) {
        this.sender = sender;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    // ChatGPT Usage: No
    public Date getTimeSent() {
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

    @Override
    public int compareTo(Message otherMessage) {
        return otherMessage.getTimeSent().compareTo(this.getTimeSent());
    }
}

