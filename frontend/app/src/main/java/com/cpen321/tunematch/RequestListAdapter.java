package com.cpen321.tunematch;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RequestListAdapter extends ArrayAdapter<SearchUser> {
    private List<SearchUser> dataList;
    private final Context context;
    private final WebSocketService webSocketService;
    ReduxStore model = ReduxStore.getInstance();

    // ChatGPT Usage: No
    public RequestListAdapter(Context context, List<SearchUser> dataList, WebSocketService webSocketService) {
        super(context, R.layout.request_list_item, dataList);
        this.context = context;
        this.webSocketService = webSocketService;
        this.dataList = dataList;
    }

    // ChatGPT Usage: Partial
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.request_list_item, parent, false);

        ImageView imageView = rowView.findViewById(R.id.requestPfpImgView);
        TextView textView = rowView.findViewById(R.id.requestNameText);
        Button acceptBtn = rowView.findViewById(R.id.acceptBtn);
        Button declineBtn = rowView.findViewById(R.id.declineBtn);

        // Set data for each item
        SearchUser currentItem = dataList.get(position);
        Picasso.get()
                .load(currentItem.getProfilePic())
                .placeholder(R.drawable.default_profile_image)      // Set the default image
                .error(R.drawable.default_profile_image)            // Use the default image in case of an error
                .into(imageView);
        textView.setText(currentItem.getName());

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject messageToSend = new JSONObject();
                JSONObject body = new JSONObject();
                try {
                    messageToSend.put("method", "REQUESTS");
                    messageToSend.put("action", "add");

                    body.put("userId", currentItem.getId());
                    messageToSend.put("body", body);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                if (webSocketService != null) {
                    Log.d("RequestList", "Accept friend:" + currentItem.getName());
                    webSocketService.sendMessage(messageToSend.toString());
                    model.removeRequest(model.getReceivedRequests(), currentItem.getId());
                    model.addFriend(new Friend(currentItem.getId(), currentItem.getName(), currentItem.getProfilePic()));
                }
            }
        });

        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject messageToSend = new JSONObject();
                JSONObject body = new JSONObject();
                try {
                    messageToSend.put("method", "REQUESTS");
                    messageToSend.put("action", "remove");

                    body.put("userId", currentItem.getId());
                    messageToSend.put("body", body);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                if (webSocketService != null) {
                    Log.d("RequestList", "Decline friend: " + currentItem.getName());
                    webSocketService.sendMessage(messageToSend.toString());
                    model.removeRequest(model.getReceivedRequests(), currentItem.getId());
                }
            }
        });

        return rowView;
    }

    // ChatGPT Usage: No
    public void setData(List<SearchUser> data) {
        this.dataList = data;
        notifyDataSetChanged();
    }
}
