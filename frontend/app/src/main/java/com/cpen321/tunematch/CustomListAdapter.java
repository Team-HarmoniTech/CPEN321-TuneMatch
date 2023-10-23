// For friend activity list
package com.cpen321.tunematch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomListAdapter extends BaseAdapter {
    private Context context;
    private List<List<String>> itemList;            // List = [(f1, s1), (f2, s2) ... ]

    public CustomListAdapter(Context context, List<List<String>> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.friend_activity_custom, parent, false);
        }

        TextView friendNameText = convertView.findViewById(R.id.friendNameText);
        String nameText = itemList.get(position).get(0);
        friendNameText.setText(nameText);

        // Configure other views in the layout as needed
        TextView songText = convertView.findViewById(R.id.songTitleText);
        String titleText = itemList.get(position).get(1);
        songText.setText(titleText);

        return convertView;
    }
}

