package com.cpen321.tunematch;

import android.app.AlertDialog;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_home, container, false);

        ListView friendsActivityList = view.findViewById(R.id.friendsList);
        List<List<String>> itemList = new ArrayList<>();
        // TODO: Need to query server to get the list of friends and what they are currently listening
        for (int i = 0; i < 5; i++) {
            itemList.add(Arrays.asList(String.format("Item %d", i), String.format("Song %d", i)));
        }
        CustomListAdapter adapter = new CustomListAdapter(getContext(), itemList);
        friendsActivityList.setAdapter(adapter);

        return view;
    }
}
