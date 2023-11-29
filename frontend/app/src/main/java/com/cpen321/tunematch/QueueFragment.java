package com.cpen321.tunematch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;

public class QueueFragment extends Fragment {
    ReduxStore model;
    MainActivity mainActivity;
    private QueueAdapter queueAdapter;
    private WebSocketService webSocketService;

    // ChatGPT Usage: Partial
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ReduxStore.getInstance();
        mainActivity = (MainActivity) getActivity();
        webSocketService = mainActivity.getWebSocketService();
    }

    // ChatGPT Usage: Partial
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_queue, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        queueAdapter = new QueueAdapter(webSocketService);
        recyclerView.setAdapter(queueAdapter);

        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            int draggedFromPosition = -1;
            int draggedToPosition = -1;

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                final int fromPosition = viewHolder.getAdapterPosition();
                final int toPosition = target.getAdapterPosition();

                if (draggedFromPosition == -1) {
                    draggedFromPosition = fromPosition;
                }
                draggedToPosition = toPosition;
                queueAdapter.onItemMove(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Required function
                // Not used in our app
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);

                // Send the WebSocket message only after the item is dropped.
                if (actionState == ItemTouchHelper.ACTION_STATE_IDLE
                        && draggedFromPosition != -1 && draggedToPosition != -1
                        && draggedFromPosition != draggedToPosition && webSocketService != null
                        && model.checkSessionActive().getValue()) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("method", "SESSION");
                        jsonObject.put("action", "queueDrag");
                        JSONObject body = new JSONObject();
                        body.put("startIndex", draggedFromPosition);
                        body.put("endIndex", draggedToPosition);
                        jsonObject.put("body", body);
                        webSocketService.sendMessage(jsonObject.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Reset the positions
                    draggedFromPosition = -1;
                    draggedToPosition = -1;
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
        addSongsToQueue();
        return view;
    }

    // ChatGPT Usage: No
    private void addSongsToQueue() {
        model.getSongQueue().observe(getViewLifecycleOwner(), songs -> {
            if (songs == null) {
                songs = new ArrayList<>();
            }
            queueAdapter.setSongs(songs);
            queueAdapter.notifyDataSetChanged();
        });
//        List<Song> songs = new ArrayList<>();
//        songs.add(new Song("1BxfuPKGuaTgP7aM0Bbdwr", "2:58"));
//        songs.add(new Song("0FDzzruyVECATHXKHFs9eJ", "3:00"));
//        songs.add(new Song("7D0RhFcb3CrfPuTJ0obrod", "3:00"));
//        songs.add(new Song("3AJwUDP919kvQ9QcozQPxg", "3:00"));
//        songs.add(new Song("1p80LdxRV74UKvL8gnD7ky", "3:00"));
//
//        queueAdapter.setSongs(songs);
//        model.getSongQueue().postValue(songs);
//        JSONObject message = new JSONObject();
//        try {
//            JSONArray bodyArray = new JSONArray();
//            for (Song song : songs) {
//                JSONObject songJson = new JSONObject();
//                songJson.put("uri", "spotify:track:" + song.getSongID());
//                String[] timeParts = song.getDuration().split(":");
//                long durationMs = (Integer.parseInt(timeParts[0]) * 60 + Integer.parseInt(timeParts[1])) * 1000;
//                songJson.put("durationMs", durationMs);
//                bodyArray.put(songJson);
//            }
//
//            message.put("method", "SESSION");
//            message.put("action", "queueReplace");
//            message.put("body", bodyArray);
//
//
//            Log.e("QueueFragment", "Does webSocketService exist?" + (webSocketService != null));
//            if(webSocketService != null && model.checkSessionActive().getValue()) {
//                Log.d("QueueFragment", "Sending message to WebSocketService"+ message.toString());
//                webSocketService.sendMessage(message.toString());
//            }
//        } catch (JSONException e) {
//            Log.e("JSONException", "Exception message: "+e.getMessage());
//        }
    }
}
