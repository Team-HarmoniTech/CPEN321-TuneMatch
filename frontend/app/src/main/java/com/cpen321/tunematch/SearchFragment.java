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
import androidx.lifecycle.ViewModel;
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
    ReduxStore model;
    ApiClient apiClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ViewModel and ApiClient here.
        model = new ViewModelProvider(requireActivity()).get(ReduxStore.class);
        apiClient = ((MainActivity) getActivity()).getApiClient();;

        new Thread(new Runnable() {
            @Override
            public void run() {
                String response;
                try {
                    response = apiClient.doGetRequest("/me/matches", true);
                    // Parse the response.
                    List<Friend> newFriendsList = parseResponse(response);
                    // Update LiveData.
                    model.getFriendsList().postValue(newFriendsList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

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
                String selectedUser = (String) parent.getItemAtPosition(position);
                nameText.setText(selectedUser + " (80%)");
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
                Log.d("SearchFragment", "onQueryTextSubmit: " + query);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String response;
                        try {
                            if(query.isEmpty()){
                                Log.d("SearchFragment", "its triggered");
                                response = apiClient.doGetRequest("/me/matches", true);
                                // Parse the response.
                            }
                            else{
                                // TODO: broken due to changes in the method. need to add query to body
                                response = apiClient.doGetRequest("/users/search/" + query, true);
                            }

                            // Parse the response.
//                            Log.d("SearchFragment", "djvhbjshdbjs: " + response);
                            List<Friend> newFriendsList = parseResponse(response);
                            // Update LiveData.
                            model.getFriendsList().postValue(newFriendsList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();



                return true; // Return true to indicate that you've handled the event
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("SearchFragment", "onQueryTextSubmit: " + newText);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String response;
                        try {
                            if(newText.isEmpty()){
                                Log.d("SearchFragment", "its triggered");
                                response = apiClient.doGetRequest("/me/matches", true);
                                // Parse the response.
                            }
                            else{
                                response = apiClient.doGetRequest("/users/search/" + newText, true);
                            }

                            // Parse the response.
                            List<Friend> newFriendsList = parseResponse(response);
                            // Update LiveData.
                            model.getFriendsList().postValue(newFriendsList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();


                return true; // Return true to indicate that you've handled the event
            }
        });

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

        return view;
    }
    public List<Friend> parseResponse(String response) {
        Log.d("SearchFragment", "parseResponse: " + response);
        List<Friend> friends = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("username");
                String id = jsonObject.getString("id");
                String match_score = "";
                if(jsonObject.has("match")){
                    match_score = jsonObject.getString("match");
                    friends.add(new Friend(name + " ("+match_score+"%)", id));
                }
                else {
                    friends.add(new Friend(name, id));
                }

                // Create a new Friend object and add it to the list.
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return friends;
    }
}
