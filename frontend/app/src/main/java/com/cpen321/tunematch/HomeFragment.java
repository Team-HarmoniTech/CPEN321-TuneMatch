package com.cpen321.tunematch;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    ReduxStore model;
    Button createSessionButton;
    MainActivity mainActivity;
    BottomNavigationView bottomNavigationView;
    private View view;
    private WebSocketService webSocketService;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // ChatGPT Usage: Partial
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ReduxStore.getInstance();
        mainActivity = (MainActivity) getActivity();
        webSocketService = mainActivity.getWebSocketService();
        bottomNavigationView = mainActivity.findViewById(R.id.bottomNavi);
    }

    // ChatGPT Usage: No
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_home, container, false);
        return view;
    }

    // ChatGPT Usage: Partial
    @Override
    public void onStart() {
        super.onStart();
        createSessionButton = view.findViewById(R.id.createListeningSessionBtn);

        RecyclerView friendsActivityList = view.findViewById(R.id.friendsList);
        MutableLiveData<List<Friend>> friendsList = model.getFriendsList();
        FriendActivityListAdapter friendsAdapter = new FriendActivityListAdapter(friendsList.getValue(), handler);
        friendsActivityList.setAdapter(friendsAdapter);
        friendsActivityList.setLayoutManager(new LinearLayoutManager(requireContext()));

        friendsList.observe(getViewLifecycleOwner(), friendsAdapter::updateData);

        // Add existing listening session
        ListView sessionList = view.findViewById(R.id.listeningSessionList);
        MutableLiveData<List<Session>> sessions = model.getSessionList();
        SessionListAdapter sessionAdapter = new SessionListAdapter(getContext(), getActivity(), sessions.getValue(), webSocketService);
        sessionList.setAdapter(sessionAdapter);
        model.getSessionList().observe(getViewLifecycleOwner(), sessionAdapter::updateData);

        createSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject messageToCreateSession = new JSONObject();
                try {
                    messageToCreateSession.put("method", "SESSION");
                    messageToCreateSession.put("action", "join");
                } catch (JSONException e) {
                    Log.e("JSONException", "Exception message: " + e.getMessage());
                }
                if (webSocketService != null) {
                    webSocketService.sendMessage(messageToCreateSession.toString());
                }
                model.checkSessionActive().postValue(true);
                bottomNavigationView.setSelectedItemId(R.id.navigation_room);
            }
        });
    }

    @Override
    public void onDestroy() {
        // Remove any pending callbacks when the activity is destroyed
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
