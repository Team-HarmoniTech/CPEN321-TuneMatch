package com.cpen321.tunematch;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_profile, container, false);

        Button friendsListBtn = view.findViewById(R.id.friendsListBtn);
        friendsListBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ArrayList<String> friendsList = new ArrayList<String>();
                friendsList.add("cassiel");                 //TODO: Need to query server to get list of friends
                friendsList.add("Aryan");

                ListFragment friendsListFragment = ListFragment.newInstance(friendsList, "Friends List");

                // Get the parent activity's fragment manager
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                // Begin a fragment transaction
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                transaction.replace(R.id.mainFrame, friendsListFragment);
                transaction.addToBackStack(null);       // If you want to add the transaction to the back stack
                transaction.commit();
            }

        });

        Button topArtistBtn = view.findViewById(R.id.topArtistsBtn);
        topArtistBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ArrayList<String> topArtistList = new ArrayList<String>();
                topArtistList.add("yoonha");                 //TODO: Need to query server to get list of top artists
                topArtistList.add("day6");

                ListFragment topArtistFragment = ListFragment.newInstance(topArtistList, "Top Artists");

                // Get the parent activity's fragment manager
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                // Begin a fragment transaction
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                transaction.replace(R.id.mainFrame, topArtistFragment);
                transaction.addToBackStack(null);       // If you want to add the transaction to the back stack
                transaction.commit();

            }

        });

        Button topSongsBtn = view.findViewById(R.id.topSongsBtn);
        topSongsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ArrayList<String> topSongList = new ArrayList<String>();
                topSongList.add("a");                      //TODO: Need to query server to get list of top songs
                topSongList.add("b");

                ListFragment topArtistFragment = ListFragment.newInstance(topSongList, "Top Songs");

                // Get the parent activity's fragment manager
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                // Begin a fragment transaction
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                transaction.replace(R.id.mainFrame, topArtistFragment);
                transaction.addToBackStack(null);           // If you want to add the transaction to the back stack
                transaction.commit();

            }

        });

        return view;
    }
}
