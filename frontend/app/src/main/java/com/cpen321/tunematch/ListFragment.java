// Wrote by team member following online tutorial regarding BottomNavigationView usage
package com.cpen321.tunematch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment{

    ReduxStore model;
    private String listTitle;
    private ArrayList<String> listItems;
    private WebSocketService webSocketService;

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
        webSocketService = ((MainActivity) getActivity()).getWebSocketService();
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
            // Updated the CustomListAdapter instantiation to pass `this` as the SessionJoinListener
            CustomListAdapter adapter = new CustomListAdapter(getContext(), getActivity(), "EditFriendsList", listItems, webSocketService);
            listView.setAdapter(adapter);

            model.getFriendsList().observe(getViewLifecycleOwner(), friends -> {
                ArrayList<String> friendsListItems = new ArrayList<>();
                for (Friend f : friends) {
                    friendsListItems.add(f.getName()+";"+f.getId()+";"+f.getProfilePic());
                }
                adapter.setData(friendsListItems);
                adapter.notifyDataSetChanged();
            });
        } else if (listTitle.equals("Request List")) {
            List<SearchUser> requestList = new ArrayList<SearchUser>();
            RequestListAdapter adapter = new RequestListAdapter(getContext(), requestList, webSocketService);
            listView.setAdapter(adapter);
            model.getReceivedRequests().observe(getViewLifecycleOwner(), request -> {
                for (SearchUser r: request) {
                    requestList.add(r);
                }
                adapter.setData(requestList);
                adapter.notifyDataSetChanged();
            });

        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, listItems);
            listView.setAdapter(adapter);
        }

        return view;
    }
}

