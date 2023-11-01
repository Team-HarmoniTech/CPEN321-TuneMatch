// Wrote by team member following online tutorial regarding BottomNavigationView usage
package com.cpen321.tunematch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class ListFragment extends Fragment {

    ReduxStore model;
    private String listTitle;
    private ArrayList<String> listItems;

    private WebSocketService webSocketService;
    private boolean isServiceBound = false;

    // ChatGPT Usage: Partial
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            WebSocketService.LocalBinder binder = (WebSocketService.LocalBinder) service;
            webSocketService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
        }
    };

    // ChatGPT Usage: No
    public ListFragment() {}

    // ChatGPT Usage: Yes
    public static ListFragment newInstance(ArrayList<String> listItem, String listTitle) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("itemList", listItem);
        args.putString("listTitle", listTitle);
        fragment.setArguments(args);
        return fragment;
    }

    // ChatGPT Usage: Yes
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            listItems = getArguments().getStringArrayList("itemList");
            listTitle = getArguments().getString("listTitle");
        }

        model = ((MainActivity) getActivity()).getModel();
    }

    // ChatGPT Usage: Partial
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_listview, container, false);

        TextView titleTextView = view.findViewById(R.id.titleText);
        titleTextView.setText(listTitle);

        // Find the ListView and set the adapter
        ListView listView = view.findViewById(R.id.listView);

        // Create an ArrayAdapter to populate the ListView
        if (listTitle.equals("Friends List")) {

            CustomListAdapter adapter = new CustomListAdapter(getContext(), null, "EditFriendsList", listItems, webSocketService, isServiceBound);
            listView.setAdapter(adapter);
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, listItems);
            listView.setAdapter(adapter);
        }

        return view;
    }

    // ChatGPT Usage: No
    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getActivity(), WebSocketService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    // ChatGPT Usage: No
    @Override
    public void onPause() {
        super.onPause();
        if (isServiceBound) {
            getActivity().unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    // ChatGPT Usage: No
    public WebSocketService getWebSocketService() {
        return webSocketService;
    }

    // ChatGPT Usage: No
    public boolean isServiceBound() {
        return isServiceBound;
    }
}

