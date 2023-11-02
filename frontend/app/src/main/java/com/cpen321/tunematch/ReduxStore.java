package com.cpen321.tunematch;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class ReduxStore extends ViewModel {
    private static ReduxStore instance;
    private final MutableLiveData<List<Friend>> friendsList;
    private final MutableLiveData<List<SearchUser>> friendRequests;
    private final MutableLiveData<List<Session>> sessionList;
    private final MutableLiveData<List<Song>> songQueue;
    private final MutableLiveData<List<SearchUser>> searchList;
    private final MutableLiveData<CurrentSession> currentSession;
    private final MutableLiveData<MediaControllerState> mediaControllerState;
    private final MutableLiveData<List<Message>> chatMessages;
    private final MutableLiveData<Boolean> SessionActive;
    public ReduxStore() {
        friendsList = new MutableLiveData<>();
        friendRequests = new MutableLiveData<>();
        sessionList = new MutableLiveData<>();
        songQueue = new MutableLiveData<>();
        searchList = new MutableLiveData<>();
        currentSession = new MutableLiveData<>();
        mediaControllerState = new MutableLiveData<>();
        chatMessages = new MutableLiveData<>();
        SessionActive = new MutableLiveData<>(false);
    }
    public static synchronized ReduxStore getInstance() {
        if (instance == null) {
            instance = new ReduxStore();
        }
        return instance;
    }

    // ChatGPT Usage: No
    public MutableLiveData<List<Friend>> getFriendsList() {
        return friendsList;
    }

    // ChatGPT Usage: No
    public MutableLiveData<List<SearchUser>> getFriendsRequest() {return friendRequests;}

    public MutableLiveData<Boolean> checkSessionActive() {
        return SessionActive;
    }
    // ChatGPT Usage: No
    public void setFriendsRequestList(List<SearchUser> newRequests) {friendRequests.setValue(newRequests);}

    // ChatGPT Usage: No
    public MutableLiveData<List<SearchUser>> getSearchList() {return searchList;}

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

    // ChatGPT Usage: No
    public void setFriendsList(List<Friend> friends) {
        friendsList.setValue(friends);
    }

    // ChatGPT Usage: No
    public MutableLiveData<List<Session>> getSessionList() {
        return sessionList;
    }

    // ChatGPT Usage: No
    public void setSessionList(List<Session> sessions) {
        sessionList.setValue(sessions);
    }

    // ChatGPT Usage: No
    public MutableLiveData<List<Song>> getSongQueue() {
        return songQueue;
    }

    // ChatGPT Usage: No
    public void setSongQueue(List<Song> songs) {
        songQueue.setValue(songs);
    }

    // Additional methods in ReduxStore for easier management of state
    // ChatGPT Usage: No
    public void addMessage(Message message) {
        List<Message> currentMessages = chatMessages.getValue();
        currentMessages.add(message);
        chatMessages.setValue(currentMessages);
    }

    // ChatGPT Usage: No
    public void addSongToQueue(Song song) {
        List<Song> currentQueue = songQueue.getValue();
        currentQueue.add(song);
        songQueue.setValue(currentQueue);
    }

    // ChatGPT Usage: No
    public void removeSongFromQueue(Song song) {
        List<Song> currentQueue = songQueue.getValue();
        currentQueue.remove(song);
        songQueue.setValue(currentQueue);
    }

    // ChatGPT Usage: No
    public String getFriendName(String id) {
        String name = "";
        for (Friend f : friendsList.getValue()) {
            if ((f.getId()).equals(id)) {
                name = f.getName();
                break;
            }
        }
        return name;
    }

    // ChatGPT Usage: No
    public void addFriend(Friend friendToAdd) {
        List<Friend> currFriendList = friendsList.getValue();
        currFriendList.add(friendToAdd);
        friendsList.setValue(currFriendList);
    }

    // ChatGPT Usage: No
    public void removeFriend(Friend friendToRemove) {
        List<Friend> currFriendList = friendsList.getValue();

        for (int i = 0; i < currFriendList.size(); i++) {
            Friend f = currFriendList.get(i);
            if (f.getId().equals(friendToRemove.getId()) & f.getName().equals(friendToRemove.getName())) {
                currFriendList.remove(i);
                break;
            }
        }

        setFriendsList(currFriendList);
    }

    // ChatGPT Usage: No
    public void addFriendRequest(SearchUser userToAdd) {
        List<SearchUser> currRequestList = friendRequests.getValue();
        currRequestList.add(userToAdd);
        setFriendsRequestList(currRequestList);
    }

    // ChatGPT Usage: No
    public void removeFriendRequest(SearchUser userToRemove) {
        List<SearchUser> currRequestList = friendRequests.getValue();
        for (int i = 0; i < currRequestList.size(); i++) {
            SearchUser f = currRequestList.get(i);
            if (f.getId().equals(userToRemove.getId()) & f.getName().equals(userToRemove.getName())) {
                currRequestList.remove(i);
                break;
            }
        }
        setFriendsRequestList(currRequestList);
    }


//    Handle sessions

}


