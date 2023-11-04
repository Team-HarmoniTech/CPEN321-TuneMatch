package com.cpen321.tunematch;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements Comparable<Message> {
    private User sender;
    private String messageText;
    private Date timestamp;
    public static final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");;

    // ChatGPT Usage: No
    public Message(User sender, String messageText, Date timestamp) {
        this.sender = sender;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    // ChatGPT Usage: No
    public Date getTimestamp() {
        return timestamp;
    }

    public String getTimestampString() {
        return timestampFormat.format(timestamp);
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

    // ChatGPT Usage: No
    public String getSenderUsername() {
        return sender.getUserName();
    }

    // ChatGPT Usage: No
    @Override
    public int compareTo(Message otherMessage) {
        return otherMessage.getTimestamp().compareTo(this.getTimestamp());
    }
}

