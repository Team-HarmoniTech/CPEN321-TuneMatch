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

import java.util.ArrayList;
import java.util.List;

public class FriendListFragment extends ListFragment<Friend> {

    // ChatGPT Usage: No
    public FriendListFragment(List<Friend> items, String listTitle) {
        super(items, listTitle);
    }

    // ChatGPT Usage: No
    @Override
    protected void setAdapter(ListView listView) {
        FriendListAdapter adapter = new FriendListAdapter(getContext(), getActivity(), listItems, webSocketService);
        listView.setAdapter(adapter);

        model.getFriendsList().observe(getViewLifecycleOwner(), adapter::updateData);
    }
}

