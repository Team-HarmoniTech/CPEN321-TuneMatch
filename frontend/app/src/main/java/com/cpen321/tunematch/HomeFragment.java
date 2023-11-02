package com.cpen321.tunematch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private View view;
    ReduxStore model;
    Button createSessionButton;
    MainActivity mainActivity;
    BottomNavigationView bottomNavigationView;
    private WebSocketService webSocketService;


    // ChatGPT Usage: Partial
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ReduxStore.getInstance();
        mainActivity = (MainActivity) getActivity();
        webSocketService = mainActivity.getWebSocketService();
        bottomNavigationView = mainActivity.findViewById(R.id.bottomNavi);

    }

    // ChatGPT Usage: Partial
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_home, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        createSessionButton = view.findViewById(R.id.createListeningSessionBtn);
        // Add friends activity
        ListView friendsActivityList = view.findViewById(R.id.friendsList);

        CustomListAdapter friendsAdapter = new CustomListAdapter(getContext(), getActivity(), "FriendsList", new ArrayList<>(), webSocketService);

        friendsActivityList.setAdapter(friendsAdapter);

        model.getFriendsList().observe(getViewLifecycleOwner(), friends -> {
            ArrayList<String> friendsListItems = new ArrayList<>();
            for (Friend f : friends) {
                if(f.getIsListening()==false){
                    f.setCurrentSong("Not Listening");
                }
                friendsListItems.add(f.getName()+";"+f.getCurrentSong());
            }
            friendsAdapter.setData(friendsListItems);
            friendsAdapter.notifyDataSetChanged();
        });

        // Add existing listening session
        ListView sessionList = view.findViewById(R.id.listeningSessionList);
        List<String> sessionListItems = new ArrayList<>();
        CustomListAdapter sessionAdapter = new CustomListAdapter(getContext(), getActivity(), "SessionsList", sessionListItems, webSocketService);
        model.getSessionList().observe(getViewLifecycleOwner(), sessions -> {
            sessionListItems.clear();
            for (Session s : sessions) {
                sessionListItems.add(s.getSessionId());
            }
            sessionList.setAdapter(sessionAdapter);
        });


        createSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject messageToSend = new JSONObject();
                try {
                    messageToSend.put("method", "SESSION");
                    messageToSend.put("action", "join");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                if (webSocketService != null) {
                    webSocketService.sendMessage(messageToSend.toString());
                }

                model.getCurrentSession().postValue(new CurrentSession("session","My session"));

                // send to room fragment
                bottomNavigationView.setSelectedItemId(R.id.navigation_room);
            }
        });


    }

}
