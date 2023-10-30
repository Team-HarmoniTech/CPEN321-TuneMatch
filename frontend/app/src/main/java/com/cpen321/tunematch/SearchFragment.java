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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import kotlin.text.Charsets;
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
        model = ((MainActivity) getActivity()).getModel();
        apiClient = ((MainActivity) getActivity()).getApiClient();;

        new Thread(new Runnable() {
            @Override
            public void run() {
                String response;
                try {
                    response = apiClient.doGetRequest("/me/matches", true);
                    // Parse the response.
                    List<Users> newSearchList = parseResponse(response);
                    // Update LiveData.
                    model.getSearchList().postValue(newSearchList);
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
                        Log.d("Friend profile dialog addButton","Send friend request");
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
                String encodedQuery;
                try {
                     encodedQuery= URLEncoder.encode(query, Charsets.UTF_8.toString());
                    Log.d("SearchFragment", "onQueryTextSubmit: " + encodedQuery);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String response;
                        try {
                            if(query.isEmpty()){
                                response = apiClient.doGetRequest("/me/matches", true);
                            }
                            else{
                                response = apiClient.doGetRequest("/users/search/" + encodedQuery, true);
                            }
                            List<Users> newSearchList = parseResponse(response);
                            model.getSearchList().postValue(newSearchList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String encoded_newText;
                try {
                    encoded_newText = URLEncoder.encode(newText, Charsets.UTF_8.toString());
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String response;
                        try {
                            if(newText.isEmpty()){
                                response = apiClient.doGetRequest("/me/matches", true);
                            }
                            else{
                                response = apiClient.doGetRequest("/users/search/" + encoded_newText, true);
                            }
                            List<Users> newSearchList = parseResponse(response);
                            model.getSearchList().postValue(newSearchList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                return true;
            }
        });

        model.getSearchList().observe(getViewLifecycleOwner(), new Observer<List<Users>>() {
            @Override
            public void onChanged(List<Users> SearchedUsers) {
                listAdapter.clear();
                for (Users user : SearchedUsers) {
                    listAdapter.add(user.getName() + " (" + user.getMatchPercent() + "%)");
                }
                listAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }
    public List<Users> parseResponse(String response) {
        Log.d("SearchFragment", "parseResponse: " + response);
        List<Users> searchedUser = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("username");
                String id = jsonObject.getString("id");
                String match_score = jsonObject.getString("match_percent");
                String profilePic = jsonObject.getString("profilePic");
                Users user = new Users(name, id, profilePic);
                user.setMatchPercent(match_score);
                searchedUser.add(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return searchedUser;
    }
}
