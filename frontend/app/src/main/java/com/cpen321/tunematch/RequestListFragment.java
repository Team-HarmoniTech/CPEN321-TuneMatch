// Wrote by team member following online tutorial regarding BottomNavigationView usage
package com.cpen321.tunematch;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class RequestListFragment extends ListFragment<SearchUser> {

    // ChatGPT Usage: No
    public RequestListFragment(List<SearchUser> items, String listTitle) {
        super(items, listTitle);
    }

    // ChatGPT Usage: No
    @Override
    protected void setAdapter(ListView listView) {
        Log.d("RequestList", model.getReceivedRequests().getValue().toString());

        RequestListAdapter adapter = new RequestListAdapter(getContext(), model.getReceivedRequests().getValue(), webSocketService);
        listView.setAdapter(adapter);

        model.getReceivedRequests().observe(getViewLifecycleOwner(), adapter::setData);
    }
}

