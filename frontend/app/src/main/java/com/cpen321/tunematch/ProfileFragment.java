// Wrote by team member following online tutorial regarding BottomNavigationView usage
package com.cpen321.tunematch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Headers;

public class ProfileFragment extends Fragment {
    private View view;
    ReduxStore model;
    ApiClient apiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = new ViewModelProvider(requireActivity()).get(ReduxStore.class);
        apiClient = new ApiClient();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_profile, container, false);

        Button friendsListBtn = view.findViewById(R.id.friendsListBtn);
        friendsListBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ArrayList<String> friendsNameList = model.friendsNameList();
                Log.d("ProfileFragment", friendsNameList.toString());
                ListFragment friendsListFragment = ListFragment.newInstance(friendsNameList, "Friends List");

                // Get the parent activity's fragment manager
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                // Begin a fragment transaction
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                transaction.replace(R.id.mainFrame, friendsListFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

        });

        Button topArtistBtn = view.findViewById(R.id.topArtistsBtn);
        topArtistBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("ProfileFragment", "line80:in run() thread");
                        Headers customHeaders = new Headers.Builder()
                                .add("user-id", "queryTestId2")
                                .build();

                        try {
                            String response = apiClient.doGetRequest("/me?fullProfile=true", customHeaders);
                            ArrayList<String> topArtistsList = parseList(response, "topArtists");

                            ListFragment topArtistFragment = ListFragment.newInstance(topArtistsList, "Top Artists");

                            // Get the parent activity's fragment manager
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                            // Begin a fragment transaction
                            FragmentTransaction transaction = fragmentManager.beginTransaction();

                            transaction.replace(R.id.mainFrame, topArtistFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
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
                transaction.addToBackStack(null);
                transaction.commit();
            }

        });

        return view;
    }

    public ArrayList<String> parseList(String response, String key) {
        Log.d("ProfileFragment", "parseList: "+key);

        ArrayList<String> parsedList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            String tempList = jsonObject.getString(key).replace("[", "").replace("]","").trim();
            Log.d("ProfileFragment", "tempList:"+tempList);

            for (String item : tempList.split(",")) {
                parsedList.add(item.replace("\"", ""));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parsedList;
    }
}
