package com.cpen321.tunematch;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.Synchronized;

public class ReduxStore extends ViewModel {
    private final MutableLiveData<List<Friend>> friendsList;
    private final MutableLiveData<List<Session>> sessionList;
    private final MutableLiveData<List<Song>> songQueue;
    private final MutableLiveData<List<Users>> SearchList;

    public ReduxStore() {
        friendsList = new MutableLiveData<>();
        sessionList = new MutableLiveData<>();
        songQueue = new MutableLiveData<>();
        SearchList = new MutableLiveData<>();
    }

    public MutableLiveData<List<Friend>> getFriendsList() {
        return friendsList;
    }
    public MutableLiveData<List<Users>> getSearchList() {return SearchList;}
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
