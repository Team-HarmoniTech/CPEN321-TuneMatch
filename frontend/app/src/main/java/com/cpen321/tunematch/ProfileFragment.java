// Wrote by team member following online tutorial regarding BottomNavigationView usage
package com.cpen321.tunematch;

import android.os.Bundle;
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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {
    private View view;
    ReduxStore model;
    ApiClient apiClient;
    FragmentManager fm;
    FragmentTransaction ft;

    // ChatGPT Usage: No
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = ReduxStore.getInstance();
        apiClient = ((MainActivity) getActivity()).getBackend();;
        fm = getActivity().getSupportFragmentManager();
    }

    // ChatGPT Usage: Partial
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_profile, container, false);
        setupMyProfile();

        Button friendsListBtn = view.findViewById(R.id.friendsListBtn);
        friendsListBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ListFragment friendsListFragment = ListFragment.newInstance(new ArrayList<>(), "Friends List");

                ft = fm.beginTransaction();

                ft.replace(R.id.mainFrame, friendsListFragment);
                ft.addToBackStack(null);
                ft.commit();
            }

        });

        Button topArtistBtn = view.findViewById(R.id.topArtistsBtn);
        topArtistBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ArrayList<String> topArtistsList = model.getCurrentUser().getValue().getTopArtists();
                ListFragment topArtistFragment = ListFragment.newInstance(topArtistsList, "Top Artists");

                // Begin a fragment transaction
                ft = fm.beginTransaction();
                ft.replace(R.id.mainFrame, topArtistFragment);
                ft.addToBackStack(null);
                ft.commit();
            }

        });

        Button topGenresBtn = view.findViewById(R.id.topGenresBtn);
        topGenresBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ArrayList<String> topGenreList = model.getCurrentUser().getValue().getTopGenres();
                ListFragment topArtistFragment = ListFragment.newInstance(topGenreList, "Top Genres");

                // Begin a fragment transaction
                ft = fm.beginTransaction();
                ft.replace(R.id.mainFrame, topArtistFragment);
                ft.addToBackStack(null);
                ft.commit();
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
        Picasso.get()
                .load(current.getProfilePic())
                .placeholder(R.drawable.default_profile_image)      // Set the default image
                .error(R.drawable.default_profile_image)            // Use the default image in case of an error
                .into(profileView);
    }
}
