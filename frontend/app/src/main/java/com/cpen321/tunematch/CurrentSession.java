package com.cpen321.tunematch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurrentSession {
    private String sessionId;
    private final String sessionName;
    private final List<Message> messages;
    private List<User> sessionMembers;
    private List<Song> sessionQueue;
    private Song currentSong;

    // ChatGPT Usage: No
    public CurrentSession(String name, String id) {
        sessionName = name;
        sessionId = id;
        sessionMembers = new ArrayList<>();
        sessionQueue = new ArrayList<>();
        messages = new ArrayList<>();
        currentSong = null;
    }

    // ChatGPT Usage: No
    public String getSessionName() {
        return sessionName;
    }

    // ChatGPT Usage: No
    public String getSessionId() {
        return sessionId;
    }

    // ChatGPT Usage: No
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    // ChatGPT Usage: No
    public List<User> getSessionMembers() {
        return sessionMembers;
    }

    // ChatGPT Usage: No
    public void setSessionMembers(List<User> sessionMembers) {
        this.sessionMembers = sessionMembers;
    }

    // ChatGPT Usage: No
    public List<Song> getSessionQueue() {
        return sessionQueue;
    }

    // ChatGPT Usage: No
    public void setSessionQueue(List<Song> sessionQueue) {
        this.sessionQueue = sessionQueue;
    }

    // ChatGPT Usage: No
    public Song getCurrentSong() {
        return currentSong;
    }

    // ChatGPT Usage: No
    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }

    // ChatGPT Usage: No
    public List<Message> getMessages() {
        return messages;
    }

    // ChatGPT Usage: No
    public void addMessage(Message message) {
        messages.add(message);
        Collections.sort(messages);
    }

    // ... (Other Getters, Setters, and methods to manage sessionMembers and sessionQueue)
}
