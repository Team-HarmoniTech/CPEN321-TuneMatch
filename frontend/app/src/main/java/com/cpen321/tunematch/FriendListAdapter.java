package com.cpen321.tunematch;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FriendListAdapter extends ListAdapter<Friend> {

    // ChatGPT Usage: Partial
    public FriendListAdapter(Context context, Activity parentView, List<Friend> friendList, WebSocketService webSocketService) {
        super(context, parentView, friendList, webSocketService);
    }

    // ChatGPT Usage: No
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.friend_remove_custom, parent, false);
        }

        Friend friend = itemList.get(position);

        // Set name
        TextView friendNameText = view.findViewById(R.id.requestNameText);
        friendNameText.setText(friend.getUserName());

        // Set profile pic
        ImageView profilePic = view.findViewById(R.id.requestPfpImgView);
        new DownloadProfilePicture(profilePic, friend.getProfileImageUrl()).run();

        Button rmBtn = view.findViewById(R.id.removeBtn);
        rmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject messageToSend = new JSONObject();
                JSONObject body = new JSONObject();
                try {
                    messageToSend.put("method", "REQUESTS");
                    messageToSend.put("action", "remove");

                    body.put("userId", friend.getUserId());
                    messageToSend.put("body", body);
                } catch (JSONException e) {
                    Log.e("JSONException", "Exception message: " + e.getMessage());
                }

                if (webSocketService != null) {
                    Log.d("ProfileFragment", "Remove friend: " + friend.getUserId());
                    webSocketService.sendMessage(messageToSend.toString());
                    model.removeFriend(friend.getUserId());
                }
            }
        });
        return view;
    }
}