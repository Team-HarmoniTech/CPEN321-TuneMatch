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

import java.util.List;

public class ListFragment<T> extends Fragment {

    protected final String listTitle;
    protected ReduxStore model;
    protected List<T> listItems;
    protected WebSocketService webSocketService;

    // ChatGPT Usage: Yes
    public ListFragment(List<T> items, String listTitle) {
        this.listItems = items;
        this.listTitle = listTitle;
    }

    // ChatGPT Usage: Yes
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ((MainActivity) requireActivity()).getModel();
        webSocketService = ((MainActivity) requireActivity()).getWebSocketService();
    }

    // ChatGPT Usage: Partial
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_listview, container, false);

        TextView titleTextView = view.findViewById(R.id.titleText);
        titleTextView.setText(listTitle);

        // Find the ListView
        ListView listView = view.findViewById(R.id.listView);
        TextView emptyText = view.findViewById(R.id.no_content);
        listView.setEmptyView(emptyText);

        // Set the adapter
        setAdapter(listView);
        return view;
    }

    protected void setAdapter(ListView listView) {
        ArrayAdapter<T> adapter = new ArrayAdapter<T>(requireActivity(), android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);
    }
}

