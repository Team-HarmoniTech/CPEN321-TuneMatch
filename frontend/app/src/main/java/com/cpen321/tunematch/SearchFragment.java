package com.cpen321.tunematch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SearchFragment extends Fragment {
    private View view;
    private ArrayAdapter<String> listAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_search, container, false);

        SearchView searchFriend = view.findViewById(R.id.searchFriend);

        ListView recommendedList = view.findViewById(R.id.recommendedList);
        listAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        recommendedList.setAdapter(listAdapter);

        searchFriend.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                listAdapter.clear();                    // Empty
                listAdapter.notifyDataSetChanged();

                listAdapter.add(query);
                listAdapter.notifyDataSetChanged();
                return true; // Return true to indicate that you've handled the event
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Called when the query text changes (e.g., as the user types)
                // You can perform real-time filtering or updates here
                return true; // Return true to indicate that you've handled the event
            }
        });

        return view;
    }
}
