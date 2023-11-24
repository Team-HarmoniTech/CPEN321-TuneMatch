package com.cpen321.tunematch;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonElement;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FriendActivityListAdapter extends RecyclerView.Adapter<FriendActivityListAdapter.FriendViewHolder> {
    private static final long MIN_IN_MS = 60000;
    private final long REFRESH_MS = MIN_IN_MS;
    private List<Friend> friends;

    // ChatGPT Usage: No
    public FriendActivityListAdapter(List<Friend> friends, Handler handler) {
        this.friends = friends;

        // Update the UI every 3 minutes
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
                Log.d("FriendsList", "refresh");
                handler.postDelayed(this, REFRESH_MS);
            }
        }, REFRESH_MS);
    }

    // ChatGPT Usage: No
    private static String formatDuration(Date first) {
        long ms = (System.currentTimeMillis() - first.getTime());
        if (ms < MIN_IN_MS) {
            return "1 min";
        } else if (ms < 60 * MIN_IN_MS) {
            long minutes = ms / MIN_IN_MS;
            return minutes + " min";
        } else if (ms < 24 * 60 * MIN_IN_MS) {
            long hours = ms / 60 * MIN_IN_MS;
            return hours + " hr";
        } else {
            long days = ms / 24 * 60 * MIN_IN_MS;
            return days + " d";
        }
    }

    public void updateData(List<Friend> friends) {
        this.friends = friends;
        this.friends.sort(new Comparator<Friend>() {
            @Override
            public int compare(Friend f1, Friend f2) { // TODO: Double check
                return f1.getLastUpdated().compareTo(f2.getLastUpdated());
            }
        });
        this.notifyDataSetChanged();
    }

    // ChatGPT Usage: No
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.friend_activity_custom, parent, false)
        );
    }

    // ChatGPT Usage: No
    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder view, int position) {
        Friend friend = friends.get(position);
        view.name.setText(friend.getUserName());

        new DownloadProfilePicture(view.profilePicture, friend.getProfileImageUrl()).run();

        Song currSong = friend.getCurrentSong();
        if (currSong == null) {
            view.notPlaying.setVisibility(View.VISIBLE);
            view.sourceText.setVisibility(View.GONE);
            view.sourceIcon.setVisibility(View.GONE);
            view.playing.setVisibility(View.GONE);
            view.songText.setVisibility(View.GONE);
            view.endLayout.setVisibility(View.GONE);
            return;
        } else {
            view.notPlaying.setVisibility(View.GONE);
            view.sourceText.setVisibility(View.VISIBLE);
            view.sourceIcon.setVisibility(View.VISIBLE);
            view.playing.setVisibility(View.VISIBLE);
            view.songText.setVisibility(View.VISIBLE);
            view.endLayout.setVisibility(View.VISIBLE);
        }

        view.songText.setText(currSong.getSongName() + " • " + currSong.getSongArtist());
        JsonElement currSource = friend.getCurrentSource();
        if (!currSource.isJsonNull()) { // TODO: This is a terrible check
            view.sourceText.setText("Session");
            view.sourceIcon.setImageResource(R.drawable.session_icon);
        } else {
            view.sourceText.setText(currSong.getSongAlbum());
            view.sourceIcon.setImageResource(R.drawable.album_icon);
        }

        if (System.currentTimeMillis() - friend.getLastUpdated().getTime() > currSong.getDuration()) {
            view.playing.setVisibility(View.GONE);
            view.lastPlayed.setVisibility(View.VISIBLE);
            view.lastPlayed.setText(formatDuration(friend.getLastUpdated()));
        } else {
            view.lastPlayed.setVisibility(View.GONE);
            view.playing.setVisibility(View.VISIBLE);
        }
    }

    // ChatGPT Usage: No
    @Override
    public int getItemCount() {
        return friends.size();
    }

    // ChatGPT Usage: No
    static class FriendViewHolder extends RecyclerView.ViewHolder {
        private final TextView sourceText;
        private final ImageView sourceIcon;
        private final TextView songText;
        private final TextView lastPlayed;
        private final WaveView playing;
        private final TextView name;
        private final TextView notPlaying;
        private final ConstraintLayout endLayout;
        private final ImageView profilePicture;

        public FriendViewHolder(@NonNull View view) {
            super(view);
            sourceText = view.findViewById(R.id.source_text);
            sourceIcon = view.findViewById(R.id.source_icon);
            songText = view.findViewById(R.id.song_text);
            lastPlayed = view.findViewById(R.id.last_played);
            playing = view.findViewById(R.id.waveView);
            name = view.findViewById(R.id.friend_name_text);
            notPlaying = view.findViewById(R.id.not_playing);
            endLayout = view.findViewById(R.id.endLayout);
            profilePicture = view.findViewById(R.id.profileImage);
        }
    }
}

