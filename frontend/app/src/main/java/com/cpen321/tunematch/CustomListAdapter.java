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
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CustomListAdapter extends BaseAdapter {
    private Context context;
    private Activity parentView;
    private String listType;
    ;
    ReduxStore model= ReduxStore.getInstance();
    private List<String> itemList;
    private WebSocketService webSocketService;

    // ChatGPT Usage: Partial

    public CustomListAdapter(Context context, Activity parentView, String listType, List<String> itemList, WebSocketService webSocketService){
        this.context = context;
        this.parentView = parentView;
        this.listType = listType;
        this.itemList = itemList;
        this.webSocketService = webSocketService;
    }

    // ChatGPT Usage: Yes
    @Override
    public int getCount() {
        return itemList.size();
    }

    // ChatGPT Usage: Yes
    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    // ChatGPT Usage: Yes
    @Override
    public long getItemId(int position) {
        return position;
    }

    // ChatGPT Usage: No
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (listType.equals("FriendsList")) {
                view = inflater.inflate(R.layout.friend_activity_custom, parent, false);
            } else if (listType.equals("SessionsList")) {
                view = inflater.inflate(R.layout.listening_session_custom, parent, false);
            } else if (listType.equals("EditFriendsList")) {
                view = inflater.inflate(R.layout.friend_remove_custom, parent, false);
            }
        }

        if (listType.equals("FriendsList")) {                                               // in the HomeFragment
            String[] item = itemList.get(position).split(";");                        // items = "name;song"

            // Set friend name
            TextView friendNameText = view.findViewById(R.id.friendNameText);
            friendNameText.setText(item[0]);

            // Set name of the song the friend is listening
            TextView songText = view.findViewById(R.id.songTitleText);
            songText.setText(item[1]);
        } else if (listType.equals("SessionsList")) {                                       // in the HomeFragment
            // Set room name                                                                // items = "owner"
            TextView roomNameText = view.findViewById(R.id.roomNameText);
            String ownerId = itemList.get(position);
            String ownerText = model.getFriendName(ownerId);
            roomNameText.setText(String.format("%s's room", ownerText));
            Button joinBtn = view.findViewById(R.id.joinBtn);
            // TODO: Whatever is required to join to existing listening session, need to send info through itemlist

            joinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    JSONObject messageToSend = new JSONObject();
                    JSONObject body = new JSONObject();
                    try {
                        body.put("userId", ownerId);
                        messageToSend.put("method", "SESSION");
                        messageToSend.put("action", "join");
                        messageToSend.put("body", body);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    Log.d("CustomListAdapter", "Sending message to server: " + messageToSend.toString());


                    if (webSocketService != null) {
                        webSocketService.sendMessage(messageToSend.toString());
                    }
//                    CurrentSession currSession = model.getCurrentSession().getValue();
//                    currSession.setSessionId(ownerId);
//                    model.getCurrentSession().postValue(currSession);
                    BottomNavigationView bottomNavigationView = parentView.findViewById(R.id.bottomNavi);
                    bottomNavigationView.setSelectedItemId(R.id.navigation_room);
                }
            });
        } else if (listType.equals("EditFriendsList")) {                                    // in the ProfileFragment
            // Parse input                                                                  // items = "friendName;id;profilePicUrl"
            String[] item = itemList.get(position).split(";");
            String nameText = item[0];
            String id = item[1];
            String profilePicUrl = item[2];

            // Set name
            TextView friendNameText = view.findViewById(R.id.friendNameText);
            friendNameText.setText(nameText);

            // Set profile pic
            ImageView profilePic = view.findViewById(R.id.profileImageView);
            Picasso.get()
                    .load(profilePicUrl)
                    .placeholder(R.drawable.default_profile_image)      // Set the default image
                    .error(R.drawable.default_profile_image)            // Use the default image in case of an error
                    .into(profilePic);

            Button rmBtn = view.findViewById(R.id.removeBtn);
            rmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject messageToSend = new JSONObject();
                    JSONObject body = new JSONObject();
                    try {
                        messageToSend.put("method", "REQUESTS");
                        messageToSend.put("action", "remove");

                        body.put("userId", id);
                        messageToSend.put("body", body);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    if (webSocketService != null) {
                        webSocketService.sendMessage(messageToSend.toString());
                    }
                }
            });
        }
        return view;
    }

    // ChatGPT Usage: No
    public void setData(List<String> data) {
        this.itemList = data;
        notifyDataSetChanged();
    }


}

