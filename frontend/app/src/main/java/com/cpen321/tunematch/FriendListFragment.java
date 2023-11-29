// Wrote by team member following online tutorial regarding BottomNavigationView usage
package com.cpen321.tunematch;

import android.widget.ListView;

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

