package com.cpen321.tunematch;

import android.os.Bundle;

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
        ListView friendsActivityList = view.findViewById(R.id.friendsList);
        CustomListAdapter friendsAdapter = new CustomListAdapter(getContext(), getActivity(), "FriendsList", new ArrayList<>(), webSocketService);

        friendsActivityList.setAdapter(friendsAdapter);

        model.getFriendsList().observe(getViewLifecycleOwner(), friends -> {
            ArrayList<String> friendsListItems = new ArrayList<>();
            for (Friend f : friends) {
                friendsListItems.add(f.getName()+";"+ (f.getIsListening() ? f.getCurrentSong() : "Not Listening"));
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
                JSONObject messageToCreateSession = new JSONObject();
                try {
                    messageToCreateSession.put("method", "SESSION");
                    messageToCreateSession.put("action", "join");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                if (webSocketService != null) {
                    webSocketService.sendMessage(messageToCreateSession.toString());
                }
                model.checkSessionActive().postValue(true);
                bottomNavigationView.setSelectedItemId(R.id.navigation_room);
            }
        });
    }
}
