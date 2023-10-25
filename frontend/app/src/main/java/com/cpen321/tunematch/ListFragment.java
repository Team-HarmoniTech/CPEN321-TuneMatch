package com.cpen321.tunematch;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListFragment extends Fragment {
    private ArrayAdapter<String> adapter;
    private String listTitle;

    public ListFragment() {}

    public static ListFragment newInstance(String listTitle) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putString("listTitle", listTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            listTitle = getArguments().getString("listTitle");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_listview, container, false);

        TextView titleTextView = view.findViewById(R.id.titleText);
        titleTextView.setText(listTitle);

        // Create an ArrayAdapter to populate the ListView
        if (listTitle.equals("Friends List")) {
            // TODO: Query list of friends
//            adapter = new CustomListAdapter(getContext(), null, "EditFriendsList", itemList);
        } else if (listTitle.equals("Top Artists")) {
            // TODO: Query list of top artists
//            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, itemList);
        } else if (listTitle.equals("Top Genres")) {
            // TODO: Query list of top songs
        }

        // Find the ListView and set the adapter
        ListView listView = view.findViewById(R.id.listView);
        listView.setAdapter(adapter);

        return view;
    }
}

