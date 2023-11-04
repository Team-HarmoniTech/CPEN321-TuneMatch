package com.cpen321.tunematch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurrentSession {
    private String sessionId;
    private String sessionName;
    private List<Message> messages;
    private List<User> sessionMembers;
    private List<Song> sessionQueue;
    private Song currentSong;
    public CurrentSession(String name, String id) {
        sessionName = name;
        sessionId = id;
        sessionMembers = new ArrayList<>();
        sessionQueue = new ArrayList<>();
        messages = new ArrayList<>();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setSessionMembers(List<User> sessionMembers) {
        this.sessionMembers = sessionMembers;
    }

    public void setSessionQueue(List<Song> sessionQueue) {
        this.sessionQueue = sessionQueue;
    }

    public List<User> getSessionMembers() {
        return sessionMembers;
    }

    public List<Song> getSessionQueue() {
        return sessionQueue;
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message) {
        messages.add(message);
        Collections.sort(messages);
    }

    // ... (Other Getters, Setters, and methods to manage sessionMembers and sessionQueue)
}
