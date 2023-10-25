package com.cpen321.tunematch;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class ReduxStore extends ViewModel {
    private final MutableLiveData<List<Friend>> friendsList;
    private final MutableLiveData<List<Session>> sessionList;
    private final MutableLiveData<List<Song>> songQueue;

    public ReduxStore() {
        friendsList = new MutableLiveData<>();
        sessionList = new MutableLiveData<>();
        songQueue = new MutableLiveData<>();
    }

    public MutableLiveData<List<Friend>> getFriendsList() {
        return friendsList;
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
}
