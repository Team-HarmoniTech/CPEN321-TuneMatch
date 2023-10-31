// Wrote by team member following online tutorial regarding BottomNavigationView usage
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
import java.util.List;

public class ListFragment extends Fragment {

    ReduxStore model;
    private String listTitle;
    private ArrayList<String> listItems;

    public ListFragment() {}

    public static ListFragment newInstance(ArrayList<String> listItem, String listTitle) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("itemList", listItem);
        args.putString("listTitle", listTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            listItems = getArguments().getStringArrayList("itemList");
            listTitle = getArguments().getString("listTitle");
        }

        model = ((MainActivity) getActivity()).getModel();
    }

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
            CustomListAdapter friendsAdapter = new CustomListAdapter(getContext(), getActivity(), "EditFriendsList", new ArrayList<>());
            listView.setAdapter(friendsAdapter);

            model.getFriendsList().observe(getViewLifecycleOwner(), friends -> {
                ArrayList<String> friendsListItems = new ArrayList<>();
                for (Friend f : friends) {
                    if(f.getIsListening()==false){
                        f.setCurrentSong("Not Listening");
                    }
                    friendsListItems.add(f.getName()+";"+f.getId()+";"+f.getProfilePic());
                }
                friendsAdapter.setData(friendsListItems);
                friendsAdapter.notifyDataSetChanged();
            });
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, listItems);
            listView.setAdapter(adapter);
        }

        return view;
    }
}

