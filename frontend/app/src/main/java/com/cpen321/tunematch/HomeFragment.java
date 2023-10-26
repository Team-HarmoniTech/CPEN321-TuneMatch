package com.cpen321.tunematch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {
    private View view;
    private Activity mainActivity;

    public HomeFragment(Activity parent) {
        this.mainActivity = parent;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_home, container, false);

        // Add friends activity
        ListView friendsActivityList = view.findViewById(R.id.friendsList);

        ArrayList<String> friendsListItems = new ArrayList<>();
        // TODO: Need to query server to get the list of friends and what they are currently listening
        for (int i = 0; i < 20; i++) {
            friendsListItems.add(String.format("Friend %d;Song", i));
        }
        CustomListAdapter friendsAdapter = new CustomListAdapter(getContext(), getActivity(), "FriendsList", friendsListItems);

        friendsActivityList.setAdapter(friendsAdapter);

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
