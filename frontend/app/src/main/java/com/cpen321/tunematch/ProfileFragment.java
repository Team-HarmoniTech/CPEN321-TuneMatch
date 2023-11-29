// Wrote by team member following online tutorial regarding BottomNavigationView usage
package com.cpen321.tunematch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {
    ReduxStore model;
    ApiClient apiClient;
    SpotifyService spotifyService;
    FragmentManager fm;
    FragmentTransaction ft;
    private View view;

    // ChatGPT Usage: No
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = ReduxStore.getInstance();
        apiClient = ((MainActivity) getActivity()).getBackend();
        spotifyService = ((MainActivity) getActivity()).getSpotifyService();
        fm = getActivity().getSupportFragmentManager();
    }

    // ChatGPT Usage: Partial
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_profile, container, false);
        setupMyProfile();

        Button friendsListBtn = view.findViewById(R.id.friendsListBtn);
        friendsListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendListFragment friendsListFragment = new FriendListFragment(model.getFriendsList().getValue(), "Friends List");

                ft = fm.beginTransaction();

                ft.replace(R.id.mainFrame, friendsListFragment);
                ft.addToBackStack(null);
                ft.commit();
            }

        });

        Button requestListBtn = view.findViewById(R.id.requestListBtn);
        requestListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestListFragment requestListFragment = new RequestListFragment(model.getReceivedRequests().getValue(), "Request List");

                ft = fm.beginTransaction();
                ft.replace(R.id.mainFrame, requestListFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        Button topArtistBtn = view.findViewById(R.id.topArtistsBtn);
        topArtistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> topArtistsList = model.getCurrentUser().getValue().getTopArtists();
                Log.d("ProfileFragment", "topArtistsList:" + topArtistsList);
                ListFragment<String> topArtistFragment = new ListFragment<>(topArtistsList, "Top Artists");

                // Begin a fragment transaction
                ft = fm.beginTransaction();
                ft.replace(R.id.mainFrame, topArtistFragment);
                ft.addToBackStack(null);
                ft.commit();
            }

        });

        Button topGenresBtn = view.findViewById(R.id.topGenresBtn);
        topGenresBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> topGenreList = model.getCurrentUser().getValue().getTopGenres();
                Log.d("ProfileFragment", "topGenresList:" + topGenreList);
                ListFragment<String> topArtistFragment = new ListFragment<>(topGenreList, "Top Genres");

                // Begin a fragment transaction
                ft = fm.beginTransaction();
                ft.replace(R.id.mainFrame, topArtistFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        view.findViewById(R.id.logoutBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ProfileFragment", "Logout button clicked");
                spotifyService.logoutSpotify();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    // ChatGPT Usage: Partial
    private void setupMyProfile() {
        User current = model.getCurrentUser().getValue();
        TextView nameView = view.findViewById(R.id.profileNameText);
        nameView.setText(current.getUserName());

        TextView idView = view.findViewById(R.id.searchIdText);
        idView.setText(current.getUserId());

        ImageView profileView = view.findViewById(R.id.pfpImageView);
        new DownloadProfilePicture(profileView, current.getProfileImageUrl()).run();
    }
}
