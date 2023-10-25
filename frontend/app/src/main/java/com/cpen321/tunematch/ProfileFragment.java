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
                ListFragment friendsListFragment = ListFragment.newInstance("Friends List");

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
                ListFragment topArtistFragment = ListFragment.newInstance("Top Artists");

                // Get the parent activity's fragment manager
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                // Begin a fragment transaction
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                transaction.replace(R.id.mainFrame, topArtistFragment);
                transaction.addToBackStack(null);       // If you want to add the transaction to the back stack
                transaction.commit();

            }

        });

        Button topGenresBtn = view.findViewById(R.id.topGenresBtn);
        topGenresBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ListFragment topArtistFragment = ListFragment.newInstance("Top Genres");

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
