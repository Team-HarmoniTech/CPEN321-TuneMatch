package com.cpen321.tunematch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class QueueFragment extends Fragment {

    private RecyclerView recyclerView;
    private QueueAdapter queueAdapter;
    private WebSocketService webSocketService;
    private boolean isServiceBound = false;
    ReduxStore model;

    // ChatGPT Usage: Partial
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            WebSocketService.LocalBinder binder = (WebSocketService.LocalBinder) service;
            webSocketService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getActivity(), WebSocketService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    // ChatGPT Usage: No
    @Override
    public void onPause() {
        super.onPause();
        if (isServiceBound) {
            getActivity().unbindService(serviceConnection);
            isServiceBound = false;
        }
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_queue, container, false);
        model = ReduxStore.getInstance();
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        queueAdapter = new QueueAdapter(webSocketService, isServiceBound);
        recyclerView.setAdapter(queueAdapter);

        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                queueAdapter.onItemMove(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Implement if needed
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
        addSongsToQueue();
        return view;
    }
    private void addSongsToQueue() {
//        model.getSongQueue().observe(getViewLifecycleOwner(), songs -> {
//            queueAdapter.setSongs(songs);
//            queueAdapter.notifyDataSetChanged();
//        });
        queueAdapter.addSong(new Song("Song 1", "Artist 1"));
        queueAdapter.addSong(new Song("Song 2", "Artist 2"));
        queueAdapter.addSong(new Song("Song 3", "Artist 3"));
        queueAdapter.addSong(new Song("Song 4", "Artist 4"));
        queueAdapter.addSong(new Song("Song 5", "Artist 5"));

    }
}
