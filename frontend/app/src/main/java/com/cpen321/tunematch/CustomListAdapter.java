
// Written by ChatGPT and add changes to fit my purpose
package com.cpen321.tunematch;


import static android.text.method.TextKeyListener.clear;
import static java.util.Collections.addAll;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

public class CustomListAdapter extends BaseAdapter {
    private Context context;
    private Activity parentView;
    private String listType;

    private List<String> itemList;

    public CustomListAdapter(Context context, Activity parentView, String listType, List<String> itemList) {
        this.context = context;
        this.parentView = parentView;
        this.listType = listType;
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
            if (listType.equals("FriendsList")) {
                convertView = inflater.inflate(R.layout.friend_activity_custom, parent, false);
            } else if (listType.equals("SessionsList")) {
                convertView = inflater.inflate(R.layout.listening_session_custom, parent, false);
            } else if (listType.equals("EditFriendsList")) {
                convertView = inflater.inflate(R.layout.friend_remove_custom, parent, false);
            }
        }

        if (listType.equals("FriendsList")) {                                               // in the HomeFragment
            String[] item = itemList.get(position).split(";");                        // items = "name;song"

            // Set friend name
            TextView friendNameText = convertView.findViewById(R.id.friendNameText);
            friendNameText.setText(item[0]);

            // Set name of the song the friend is listening
            TextView songText = convertView.findViewById(R.id.songTitleText);
            songText.setText(item[1]);
        } else if (listType.equals("SessionsList")) {                                       // in the HomeFragment
            // Set room name                                                                // items = "owner"
            TextView roomNameText = convertView.findViewById(R.id.roomNameText);
            String ownerText = itemList.get(position);
            roomNameText.setText(String.format("%s's room", ownerText));


            Button joinBtn = convertView.findViewById(R.id.joinBtn);
            // TODO: Whatever is required to join to existing listening session, need to send info through itemlist
            int sessionId = 0;

            joinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO: send join request (query reduxstore)
                    BottomNavigationView bottomNavigationView = parentView.findViewById(R.id.bottomNavi);
                    bottomNavigationView.setSelectedItemId(R.id.navigation_room);
                }
            });
        } else if (listType.equals("EditFriendsList")) {                                    // in the ProfileFragment
            // Set friend name                                                              // items = "friendName"
            TextView friendNameText = convertView.findViewById(R.id.friendNameText);
            String nameText = itemList.get(position);
            friendNameText.setText(nameText);

            Button rmBtn = convertView.findViewById(R.id.removeBtn);
            rmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: 1.Get friend ID by querying ReduxStore, 2.Query server to remove friend, 3.update redux store if 200OK
                    Log.d("Edit Friends List", "Remove clicked");
                }
            });
        }
        return convertView;
    }
    public void setData(List<String> data) {
        this.itemList = data;
        notifyDataSetChanged();
    }
}

