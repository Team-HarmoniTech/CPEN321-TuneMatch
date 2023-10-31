package com.cpen321.tunematch;

import android.util.Log;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class ReduxStore extends ViewModel {
    private static ReduxStore instance;
    private final MutableLiveData<List<Friend>> friendsList;
    private final MutableLiveData<List<Session>> sessionList;
    private final MutableLiveData<List<Song>> songQueue;
    private final MutableLiveData<List<SearchUser>> searchList;
    private final MutableLiveData<CurrentSession> currentSession;
    private final MutableLiveData<MediaControllerState> mediaControllerState;
    private final MutableLiveData<List<Message>> chatMessages;
    public ReduxStore() {
        friendsList = new MutableLiveData<>();
        sessionList = new MutableLiveData<>();
        songQueue = new MutableLiveData<>();
        searchList = new MutableLiveData<>();
        currentSession = new MutableLiveData<>();
        mediaControllerState = new MutableLiveData<>();
        chatMessages = new MutableLiveData<>();
    }
    public static synchronized ReduxStore getInstance() {
        if (instance == null) {
            instance = new ReduxStore();
        }
        return instance;
    }
    public MutableLiveData<List<Friend>> getFriendsList() {
        return friendsList;
    }
    public MutableLiveData<List<SearchUser>> getSearchList() {return searchList;}
    public ArrayList<String> friendsNameList() {
        ArrayList<String> nameList = new ArrayList<>();
        for (Friend f : friendsList.getValue()) {
            nameList.add(f.getName());
        }
        return nameList;
    }

    public String getFriendId(String name) {
        String id = new String();
        for (Friend f : friendsList.getValue()) {
            if ((f.getName()).equals(name)) {
                id = f.getId();
                break;
            }
        }
        return id;
    }

    public MutableLiveData<CurrentSession> getCurrentSession() {
        return currentSession;
    }

    public boolean checkCurrentSessionActive() {
        CurrentSession session = currentSession.getValue();
        if (session != null) {
            Log.d("redux store", "session is as such" + session.getSessionId());
            return !session.getSessionId().equals("null");
        } else {
            Log.d("redux store", "session is null");
            return false;
        }
    }
    public void setFriendsList(List<Friend> friends) {
        friendsList.setValue(friends);
    }

    public MutableLiveData<List<Session>> getSessionList() {
        return sessionList;
    }

    public void setSessionList(List<Session> sessions) {
        sessionList.setValue(sessions);
    }

    public MutableLiveData<List<Song>> getSongQueue() {
        return songQueue;
    }

    public void setSongQueue(List<Song> songs) {
        songQueue.setValue(songs);
    }

    // Additional methods in ReduxStore for easier management of state
    public void addMessage(Message message) {
        List<Message> currentMessages = chatMessages.getValue();
        currentMessages.add(message);
        chatMessages.setValue(currentMessages);
    }

    public void addSongToQueue(Song song) {
        List<Song> currentQueue = songQueue.getValue();
        currentQueue.add(song);
        songQueue.setValue(currentQueue);
    }

    public void removeSongFromQueue(Song song) {
        List<Song> currentQueue = songQueue.getValue();
        currentQueue.remove(song);
        songQueue.setValue(currentQueue);
    }
}


