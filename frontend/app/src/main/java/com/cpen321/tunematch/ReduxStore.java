package com.cpen321.tunematch;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ReduxStore extends ViewModel {
    private static ReduxStore instance;
    private final MutableLiveData<List<Friend>> friendsList;
    private final MutableLiveData<List<SearchUser>> sentRequests;
    private final MutableLiveData<List<SearchUser>> receivedRequests;
    private final MutableLiveData<List<Session>> sessionList;
    private final MutableLiveData<List<Song>> songQueue;
    private final MutableLiveData<List<SearchUser>> searchList;
    private final MutableLiveData<CurrentSession> currentSession;
    private final MutableLiveData<List<Message>> chatMessages;
    private final MutableLiveData<Boolean> sessionCreatedByMe;
    private final MutableLiveData<Song> currentSong;
    private final MutableLiveData<Boolean> sessionActive;
    private final MutableLiveData<User> currentUser;

    // ChatGPT Usage: Partial
    public ReduxStore() {
        friendsList = new MutableLiveData<>();
        sentRequests = new MutableLiveData<>();
        receivedRequests = new MutableLiveData<>();
        sessionList = new MutableLiveData<>();
        songQueue = new MutableLiveData<>();
        searchList = new MutableLiveData<>();
        currentSession = new MutableLiveData<>();
        chatMessages = new MutableLiveData<>();
        currentSong = new MutableLiveData<>();
        sessionActive = new MutableLiveData<>(false);
        currentUser = new MutableLiveData<>(null);
        sessionCreatedByMe = new MutableLiveData<>(true);
    }

    // ChatGPT Usage: Partial
    public static synchronized ReduxStore getInstance() {
        if (instance == null) {
            instance = new ReduxStore();
        }
        return instance;
    }

    public MutableLiveData<Boolean> checkSessionCreatedByMe() {
        return sessionCreatedByMe;
    }

    // ChatGPT Usage: No
    public MutableLiveData<User> getCurrentUser() {
        return currentUser;
    }

    // ChatGPT Usage: No
    public void setCurrentUser(User user) {
        currentUser.setValue(user);
    }

    // ChatGPT Usage: No
    public MutableLiveData<List<Friend>> getFriendsList() {
        return friendsList;
    }

    // ChatGPT Usage: No
    public MutableLiveData<List<Session>> getSessionList() {
        return sessionList;
    }

    // ChatGPT Usage: No
    public MutableLiveData<List<Song>> getSongQueue() {
        return songQueue;
    }

    // ChatGPT Usage: No
    public MutableLiveData<Song> getCurrentSong() {
        return currentSong;
    }

    // ChatGPT Usage: No
    public void addFriend(Friend friendToAdd) {
        List<Friend> currFriendList = friendsList.getValue();
        if (currFriendList == null) {
            currFriendList = new ArrayList<Friend>();
        }
        currFriendList.add(friendToAdd);
        friendsList.postValue(currFriendList);
    }

    // ChatGPT Usage: No
    public MutableLiveData<List<SearchUser>> getSentRequests() { return sentRequests; }

    // ChatGPT Usage: No
    public MutableLiveData<List<SearchUser>> getReceivedRequests() { return receivedRequests; }

    // ChatGPT Usage: No
    public void setSentRequestsList(List<SearchUser> newRequests) { sentRequests.postValue(newRequests); }

    // ChatGPT Usage: No
    public void addSentRequest(SearchUser userToAdd) {
        List<SearchUser> sentRequestList = sentRequests.getValue();
        if (sentRequestList == null) {
            sentRequestList = new ArrayList<SearchUser>();
        }
        sentRequestList.add(userToAdd);
        sentRequests.postValue(sentRequestList);
    }

    // ChatGPT Usage: No
    public void setReceivedRequestsList(List<SearchUser> newRequests) { receivedRequests.postValue(newRequests); }

    // ChatGPT Usage: No
    public MutableLiveData<Boolean> checkSessionActive() {
        return sessionActive;
    }

    // ChatGPT Usage: No
    public MutableLiveData<List<SearchUser>> getSearchList() {return searchList;}

    // ChatGPT Usage: No
    public MutableLiveData<CurrentSession> getCurrentSession() {
        return currentSession;
    }

    // ChatGPT Usage: Partial
    public void setFriendsList(List<Friend> friends) {
        friendsList.setValue(friends);
    }

    // ChatGPT Usage: Partial
    public void setSessionList(List<Session> sessions) {
        sessionList.setValue(sessions);
    }

    // ChatGPT Usage: Partial
    public void setSongQueue(List<Song> songs) {
        songQueue.setValue(songs);
    }

    // ChatGPT Usage: No
    public void addMessage(Message message, boolean background) {
        List<Message> currentMessages = chatMessages.getValue();
        if (currentMessages == null) {
            currentMessages = new ArrayList<Message>();
        }
        currentMessages.add(message);
        if (background) {
            chatMessages.postValue(currentMessages);
        } else {
            chatMessages.setValue(currentMessages);
        }
    }

    // ChatGPT Usage: No
    public MutableLiveData<List<Message>> getMessages() {
        return chatMessages;
    }
    public void addSongToQueue(Song song) {
        List<Song> currentQueue = songQueue.getValue();
        if (currentQueue == null) {
            currentQueue = new ArrayList<Song>();
        }
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
    public void removeFriend(String friendIdToRemove) {

        List<Friend> currFriendList = friendsList.getValue();

        for (int i = 0; i < currFriendList.size(); i++) {
            Friend f = currFriendList.get(i);
            if (f.getId().equals(friendIdToRemove)) {
                currFriendList.remove(i);
                Log.d("ReduxStore", "Removed "+friendIdToRemove);
                break;
            }
        }
        friendsList.postValue(currFriendList);
    }

    // ChatGPT Usage: No
    public void removeRequest(MutableLiveData<List<SearchUser>> requestList, String userIdToRemove) {
        List<SearchUser> currRequestList = requestList.getValue();
        if (currRequestList != null) {
            for (int i = 0; i < currRequestList.size(); i++) {
                SearchUser f = currRequestList.get(i);
                if (f.getId().equals(userIdToRemove)) {
                    currRequestList.remove(i);
                    Log.d("ReduxStore", "Removed request of:" + userIdToRemove);
                    break;
                }
            }
            requestList.postValue(currRequestList);
        }
    }

    // ChatGPT Usage: No
    public Boolean inReceivedRequest(SearchUser user) {
        List<SearchUser> currRequestList = receivedRequests.getValue();
        if (currRequestList != null) {
            for (SearchUser u : currRequestList) {
                if (u.getId().equals(user.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    // ChatGPT Usage: No
    public void setCurrentSongPlaying(Boolean isPlaying) {
        Song song = currentSong.getValue();
        song.setIsPLaying(isPlaying);
        currentSong.setValue(song);
    }

    public void setCurrentSongPosition(String timeStarted) {
        Song song = currentSong.getValue();
        song.setCurrentPosition(timeStarted);
        currentSong.setValue(song);
    }
}


