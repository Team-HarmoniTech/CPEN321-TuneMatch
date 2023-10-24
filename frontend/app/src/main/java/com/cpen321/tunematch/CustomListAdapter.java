// For friend activity list
// Written by ChatGPT and add changes to fit my purpose
package com.cpen321.tunematch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

public class CustomListAdapter extends BaseAdapter {
    private Context context;
    private String listType;
    private List<List<String>> itemList;            // List = [(f1, s1), (f2, s2) ... ]

    public CustomListAdapter(Context context, String listType, List<List<String>> itemList) {
        this.context = context;
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
            } else {
                convertView = inflater.inflate(R.layout.listening_session_custom, parent, false);
            }
        }

        TextView friendNameText = convertView.findViewById(R.id.friendNameText);
        String nameText = itemList.get(position).get(0);
        friendNameText.setText(nameText);

        if (listType.equals("FriendsList")) {
            TextView songText = convertView.findViewById(R.id.songTitleText);
            String titleText = itemList.get(position).get(1);
            songText.setText(titleText);
        }

        if (listType.equals("SessionsList")) {
            Button joinBtn = convertView.findViewById(R.id.joinBtn);
            // TODO: Whatever is required to join to existing listening session, need to send info through
            // TODO: itemlist
            int sessionId = 0;

            joinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO: send join request
                    RoomFragment roomFragment = new RoomFragment();

                    // Get the parent activity's fragment manager
                    FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();

                    // Begin a fragment transaction
                    FragmentTransaction transaction = fragmentManager.beginTransaction();

                    // open room fragment
                    transaction.replace(R.id.mainFrame, roomFragment);
                    transaction.addToBackStack(null);       // If you want to add the transaction to the back stack
                    transaction.commit();
                }
            });
        }

        return convertView;
    }
}

