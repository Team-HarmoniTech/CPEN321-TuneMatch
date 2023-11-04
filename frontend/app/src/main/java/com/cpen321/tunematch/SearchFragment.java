package com.cpen321.tunematch;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import kotlin.text.Charsets;

public class SearchFragment extends Fragment {

    private View view;
    private ArrayAdapter<String> listAdapter;
    private AlertDialog profileDialog;
    ReduxStore model;
    BackendClient backend;
    private WebSocketService webSocketService;

    // ChatGPT Usage: No
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ViewModel and ApiClient here.
        model = ((MainActivity) getActivity()).getModel();
        webSocketService = ((MainActivity) getActivity()).getWebSocketService();
        backend = ((MainActivity) getActivity()).getBackend();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String response;
                try {
                    List<SearchUser> newSearchList = backend.getMatches();
                    // Update LiveData.
                    model.getSearchList().postValue(newSearchList);
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // ChatGPT Usage: No
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_search, container, false);
        SearchView searchFriend = view.findViewById(R.id.searchFriend);
        ListView recommendedList = view.findViewById(R.id.recommendedList);

        listAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        recommendedList.setAdapter(listAdapter);

        recommendedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View dialogView = getLayoutInflater().inflate(R.layout.friend_profile_dialog, null);
                builder.setView(dialogView);

                // Find views in the dialog layout
                TextView nameText = dialogView.findViewById(R.id.nameText);
                Button addButton = dialogView.findViewById(R.id.addButton);

                // Set information
                String selectedUserWithScore = (String) parent.getItemAtPosition(position);
                nameText.setText(selectedUserWithScore);
                String username = selectedUserWithScore.split(" \\(")[0];

                SearchUser[] friend = new SearchUser[1];
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // only one user has to be returned
                            SearchUser user = backend.searchUser(username).get(0);
                            Log.d("SearchFragment", "selected user info: " + user);

                            String profileUrl = user.getProfilePic();
                            friend[0] = user;
                            if (profileUrl != null) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Update your UI components here
                                        ImageView profilePic = dialogView.findViewById(R.id.profileImage);
                                        Picasso.get()
                                                .load(profileUrl)
                                                .placeholder(R.drawable.default_profile_image) // Set the default image
                                                .error(R.drawable.default_profile_image) // Use the default image in case of an error
                                                .into(profilePic);
                                    }
                                });
                            }

                        } catch (ApiException | RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                // Show the dialog
                profileDialog = builder.create();
                profileDialog.show();
                profileDialog.getWindow().setLayout(1000, 500);

                // Set button click listener
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (friend[0] != null) {
                            Log.d("Profile dialog addButton", "Send friend request");
                            JSONObject messageToSend = new JSONObject();
                            JSONObject body = new JSONObject();
                            try {
                                messageToSend.put("method", "REQUESTS");
                                messageToSend.put("action", "add");

                                body.put("userId", friend[0].getId());
                                messageToSend.put("body", body);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }

                            if (webSocketService != null) {
                                Log.d("SearchFragment", "Send friend request to:" + friend[0].getName());
                                webSocketService.sendMessage(messageToSend.toString());
                                model.addSentRequest(friend[0]);
                            }
                            profileDialog.dismiss();
                        } else {
                            Log.d("SearchFragment","FriendId was not retrieved yet. Try again.");
                        }
                    }
                });
            }
        });

        searchFriend.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                return updateQuery(query);
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return updateQuery(newText);
            }
        });

        model.getSearchList().observe(getViewLifecycleOwner(), new Observer<List<SearchUser>>() {
            @Override
            public void onChanged(List<SearchUser> SearchedUsers) {
                listAdapter.clear();
                for (SearchUser user : SearchedUsers) {
                    listAdapter.add(user.getName() + " (" + user.getMatchPercent() + "%)");
                }
                listAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    private boolean updateQuery(String query) {
        String encodedQuery;
        try {
            encodedQuery = URLEncoder.encode(query, Charsets.UTF_8.toString());
            Log.d("SearchFragment", "onQueryTextSubmit: " + encodedQuery);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<SearchUser> newSearchList;
                try {
                    if(query.isEmpty()){
                        newSearchList = backend.getMatches();
                    }
                    else{
                        newSearchList = backend.searchUser(encodedQuery);
                    }

                    model.getSearchList().postValue(newSearchList);
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return true;
    }
}
