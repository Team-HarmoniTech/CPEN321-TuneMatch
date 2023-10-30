package com.cpen321.tunematch;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kotlin.jvm.Synchronized;

public class HomeFragment extends Fragment {
    private View view;
    ReduxStore model;
    WebSocketClient webSocketClient;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ((MainActivity) getActivity()).getModel();
        webSocketClient = ((MainActivity) getActivity()).getWebSocketClient();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_home, container, false);

        // Add friends activity
        ListView friendsActivityList = view.findViewById(R.id.friendsList);
        CustomListAdapter friendsAdapter = new CustomListAdapter(getContext(), getActivity(), "FriendsList", new ArrayList<>());
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
        // TODO: get session IDs of existing sessions to send it to the button onclick handler
        for (int i = 0; i < 15; i++) {
            sessionListItems.add(String.format("Friends %d's Room", i));
        }
        CustomListAdapter sessionAdapter = new CustomListAdapter(getContext(), getActivity(), "SessionsList", sessionListItems);
        sessionList.setAdapter(sessionAdapter);


        return view;
    }
}
