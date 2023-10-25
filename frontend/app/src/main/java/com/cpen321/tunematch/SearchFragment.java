package com.cpen321.tunematch;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.internal.http2.Header;

public class SearchFragment extends Fragment {
    private View view;
    private ArrayAdapter<String> listAdapter;
    private AlertDialog profileDialog;
    ReduxStore model = new ViewModelProvider(requireActivity()).get(ReduxStore.class);
    ApiClient apiClient = new ApiClient();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_search, container, false);

        SearchView searchFriend = view.findViewById(R.id.searchFriend);

        ListView recommendedList = view.findViewById(R.id.recommendedList);
        recommendedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View dialogView = getLayoutInflater().inflate(R.layout.friend_profile_dialog, null);
                builder.setView(dialogView);

                // Find views in the dialog layout
//                ImageView profilePic = dialogView.findViewById(R.id.profileImage);
                TextView nameText = dialogView.findViewById(R.id.nameText);
                TextView favArtistText = dialogView.findViewById(R.id.favArtistText);
                TextView favSongText = dialogView.findViewById(R.id.favSongText);
                Button addButton = dialogView.findViewById(R.id.addButton);

                // Set information
                // TODO: Need to query server to get these information about matched user
//                profilePic.setImageResource(R.drawable.ic_profile_gray_24dp);
                nameText.setText("Cassiel"+"(80%)");
                favArtistText.setText("Favorite Artist: "+"Yoonha");
                favSongText.setText("Favorite Song: "+"Event Horizon");

                // Show the dialog
                profileDialog = builder.create();
                profileDialog.show();
                profileDialog.getWindow().setLayout(1000, 500);

                // Set button click listener
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle the "Add" button click
                        // You can add the desired functionality here
                        Log.d("Friend profile dialog addButton","Send friend request");
                        // TODO: send friend request
                        profileDialog.dismiss();
                    }
                });
            }
        });


        listAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        recommendedList.setAdapter(listAdapter);

        searchFriend.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Headers customHeaders = new Headers.Builder()
                                .add("user-id", "queryTestId2")
                                .build();
                        try {
                            String response = apiClient.doGetRequest("/users/search/" + query,customHeaders);
                            // Parse the response and update LiveData.
                            List<Friend> newFriendsList = parseResponse(response);
                            model.setFriendsList(newFriendsList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                model.getFriendsList().observe(getViewLifecycleOwner(), new Observer<List<Friend>>() {
                    @Override
                    public void onChanged(List<Friend> friends) {
                        // Update the UI.
                        listAdapter.clear();
                        for (Friend friend : friends) {
                            listAdapter.add(friend.getName());
                        }
                        listAdapter.notifyDataSetChanged();
                    }
                });

                return true; // Return true to indicate that you've handled the event
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Called when the query text changes (e.g., as the user types)
                // You can perform real-time filtering or updates here
                return true; // Return true to indicate that you've handled the event
            }
        });






        return view;
    }
    public List<Friend> parseResponse(String response) {
        List<Friend> friends = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                // Create a new Friend object and add it to the list.
                friends.add(new Friend(name));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return friends;
    }
}
