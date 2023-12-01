package com.cpen321.tunematch;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SessionListAdapter extends ListAdapter<Session> {

    // ChatGPT Usage: Partial
    public SessionListAdapter(Context context, Activity parentView, List<Session> itemList, WebSocketService webSocketService) {
        super(context, parentView, itemList, webSocketService);
    }

    // ChatGPT Usage: No
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listening_session_custom, parent, false);
        }

        TextView roomNameText = view.findViewById(R.id.roomNameText);
        Session session = itemList.get(position);
        Friend owner = session.getSessionMember();
        roomNameText.setText(owner.getUserName() + "'s Session");
        Button joinBtn = view.findViewById(R.id.joinBtn);

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject messageToSend = new JSONObject();
                JSONObject body = new JSONObject();
                model.checkSessionActive().postValue(true);

//                model.getSongQueue().postValue(null);
//                model.getCurrentSong().postValue(null);
                try {
                    body.put("userId", owner.getUserId());
                    messageToSend.put("method", "SESSION");
                    messageToSend.put("action", "join");
                    messageToSend.put("body", body);
                } catch (JSONException e) {
                    Log.e("JSONException", "Exception message: " + e.getMessage());
                }
                if (webSocketService != null) {
                    webSocketService.sendMessage(messageToSend.toString());
                }

                BottomNavigationView bottomNavigationView = parentView.findViewById(R.id.bottomNavi);
                bottomNavigationView.setSelectedItemId(R.id.navigation_room);
            }
        });
        return view;
    }
}


