package com.cpen321.tunematch;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {
    private List<Song> items;

    WebSocketService webSocketService;


    public QueueAdapter(WebSocketService webSocketService) {
        items = new ArrayList<>();  // Initialize the items list
        this.webSocketService = webSocketService;
    }
    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.queue_item, parent, false);
        return new QueueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        Song song = items.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(items, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
//        sendQueueDragMessage(fromPosition, toPosition);
    }

//    private void sendQueueDragMessage(int fromPosition, int toPosition) {
//        JSONObject message = new JSONObject();
//        try {
//            message.put("method", "SESSION");
//            message.put("action", "queueDrag");
//
//            JSONObject body = new JSONObject();
//            body.put("startIndex", fromPosition);
//            body.put("endIndex", toPosition);
//
//            message.put("body", body);
//
//            // Send the message via WebSocket
//            if (webSocketService != null) {
//                webSocketService.sendMessage(message.toString());
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    public static class QueueViewHolder extends RecyclerView.ViewHolder {
        private TextView songIdView;  // Assume you have a TextView to display the song ID
        private TextView durationView;  // Assume you have a TextView to display the duration

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            songIdView = itemView.findViewById(R.id.song_id);
            durationView = itemView.findViewById(R.id.duration);
        }

        public void bind(Song song) {
            songIdView.setText(song.getSongName()+" - "+song.getSongArtist());
//            convert song in ms to mm:ss
            durationView.setText(String.format("%d:%02d",
                    Integer.parseInt(song.getDuration()) / 1000 / 60,
                    Integer.parseInt(song.getDuration()) / 1000 % 60));
        }
    }

    public void addSong(Song song) {
        items.add(song);
        notifyItemInserted(items.size() - 1);  // Notify the adapter that an item was added
    }

    public void setSongs(List<Song> songs) {
        this.items = songs;
        notifyDataSetChanged();  // Notify the adapter that the data set has changed
    }
}
