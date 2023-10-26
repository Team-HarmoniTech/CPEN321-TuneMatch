// Wrote by team member following online tutorial regarding BottomNavigationView usage
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
import java.util.List;

public class ProfileFragment extends Fragment {
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: query server to update redux store
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_profile, container, false);

        Button friendsListBtn = view.findViewById(R.id.friendsListBtn);
        friendsListBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO: query redux store to get list of friends
                ArrayList<String> friendsList =  new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    friendsList.add(String.format("Friend %d", i));
                }
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
                // TODO: query redux store to get list of top artists
                ArrayList<String> topArtistsList =  new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    topArtistsList.add(String.format("Artists %d", i));
                }
                ListFragment topArtistFragment = ListFragment.newInstance(topArtistsList, "Top Artists");

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
                // TODO: query redux store to get list of top genres
                ArrayList<String> topGenresList =  new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    topGenresList.add(String.format("Genre %d", i));
                }
                ListFragment topArtistFragment = ListFragment.newInstance(topGenresList,"Top Genres");

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
