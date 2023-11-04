package com.cpen321.tunematch;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jp.wasabeef.blurry.Blurry;
import kotlin.text.Charsets;

public class RoomFragment extends Fragment {
    // Views
    private View view;
    private Button playpauseButton, nextButton, prevButton, chatBtn, queueBtn, exitBtn;
    private SeekBar seekBar;
    private ImageView songBanner;
    private TextView songTitle, songArtist, currentDuration, totalDuration;
    private SearchView songSearchBar;
    private ListView suggestionListView;

    // Services and other fields
    private SpotifyAppRemote mSpotifyAppRemote;
    private ChatFragment chatFrag;
    private QueueFragment queueFrag;
    private ArrayAdapter<Song> searchAdapter;
    private String authToken;
    private SpotifyClient spotifyClient;
    private MainActivity mainActivity;
    private ReduxStore model;
    private ApiClient apiClient;
    private CurrentSession currentSession;
    private WebSocketService webSocketService;
    private SpotifyService mSpotifyService;

    // ChatGPT Usage: Partial
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initServices();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_room, container, false);
        initializeViews();
        initializeEventListeners();
        initializeObservers();
        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        setUpPlayerControls();
    }

    private void initServices() {
        // Initialize ViewModel and ApiClient
        model = ReduxStore.getInstance();

        apiClient = ((MainActivity) getActivity()).getBackend();

        // Get instances of MainActivity, WebSocketService, and SpotifyService
        mainActivity = (MainActivity) getActivity();
        webSocketService = mainActivity.getWebSocketService();
        mSpotifyService = mainActivity.getSpotifyService();
        mSpotifyAppRemote = mSpotifyService.getSpotifyAppRemote();
    }
    private void initializeViews() {
        chatBtn = view.findViewById(R.id.chatBtn);
        queueBtn = view.findViewById(R.id.queueBtn);
        exitBtn = view.findViewById(R.id.exitBtn);
        playpauseButton = view.findViewById(R.id.play_button);
        nextButton = view.findViewById(R.id.next_button);
        prevButton = view.findViewById(R.id.previous_button);
        seekBar = view.findViewById(R.id.seekBar);
        songBanner = view.findViewById(R.id.song_banner_imageview);
        songTitle = view.findViewById(R.id.song_name_text);
        songArtist = view.findViewById(R.id.singer_name_text);
        currentDuration = view.findViewById(R.id.start_time_text);
        totalDuration = view.findViewById(R.id.end_time_text);
        songSearchBar = view.findViewById(R.id.songSearchBar);
        suggestionListView = view.findViewById(R.id.suggestionListView);

        // Initialize chat and queue fragments
        chatFrag = new ChatFragment();
        queueFrag = new QueueFragment();

        // Initially set chatBtn and exitBtn to GONE
        chatBtn.setVisibility(View.GONE);
        exitBtn.setVisibility(View.GONE);

        // Get the current session
        CurrentSession currentSession = model.getCurrentSession().getValue();

        // Set the queue fragment to be the default fragment in the subFrame
        switchFragment(R.id.subFrame, queueFrag);

        // Initialize the search adapter and set it to the suggestion list view
        searchAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<>());
        suggestionListView.setAdapter(searchAdapter);

        // Retrieve the authentication token
        SharedPreferences preferences = getActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        authToken = preferences.getString("auth_token", null);

        // Initialize the Spotify API client with the base URL and custom headers
        spotifyClient = new SpotifyClient(authToken);
    }
    private void initializeEventListeners() {
        chatBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switchFragment(R.id.subFrame, chatFrag);
            }
        });

        queueBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switchFragment(R.id.subFrame, queueFrag);
            }
        });

        songSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                suggestionListView.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    suggestionListView.setVisibility(View.GONE);
                } else {
                    filterSuggestions(newText);
                }
                return false;
            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpotifyAppRemote.getPlayerApi().pause();
                JSONObject messageToSend = new JSONObject();
                try {
                    messageToSend.put("method", "SESSION");
                    messageToSend.put("action", "leave");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                if(webSocketService != null) {
                    webSocketService.sendMessage(messageToSend.toString());
                    model.checkSessionActive().postValue(false);
                }
//                model.getCurrentSession().postValue(new CurrentSession(null,null));
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavi);
                bottomNavigationView.setSelectedItemId(R.id.navigation_home);
            }
        });

        suggestionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song selectedSuggestion = searchAdapter.getItem(position);
                addSongToQueue(selectedSuggestion);
            }
        });


    }
    private void setUpPlayerControls(){
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
                    currentDuration.setText(formatDuration(playerState.playbackPosition));
                    int progress = (int) ((float) playerState.playbackPosition / playerState.track.duration * 100);
                    seekBar.setProgress(progress);
                    if(playerState.track.duration - playerState.playbackPosition < 3000){
                        playNextSong();
                    }
                    handler.postDelayed(this, 1000);
                });
            }
        };
        final long[] trackDuration = {0};
        Log.d(TAG, "setUpPlayerControls: is it triggered on load??");
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {

                    final Track track = playerState.track;
                    trackDuration[0] = track.duration;
                    if (track != null) {
                        mSpotifyAppRemote.getImagesApi().getImage(track.imageUri).setResultCallback(bitmap -> {
                            // Create a new ImageView to hold the bitmap
                            ImageView tempImageView = new ImageView(getContext());
                            tempImageView.setImageBitmap(bitmap);

                            // Add the ImageView to a layout
                            FrameLayout layout = new FrameLayout(getContext());
                            layout.addView(tempImageView);

                            // Measure and layout the FrameLayout
                            layout.measure(View.MeasureSpec.makeMeasureSpec(bitmap.getWidth(), View.MeasureSpec.EXACTLY),
                                    View.MeasureSpec.makeMeasureSpec(bitmap.getHeight(), View.MeasureSpec.EXACTLY));
                            layout.layout(0, 0, bitmap.getWidth(), bitmap.getHeight());

                            // Capture the ImageView with Blurry
                            Bitmap blurryBitmap = Blurry.with(getContext())
                                    .radius(10)
                                    .sampling(5)
                                    .capture(layout)
                                    .get();

                            songBanner.setImageBitmap(blurryBitmap);
                        });

                        songTitle.setText(track.name);
                        songArtist.setText(track.artist.name);
                        seekBar.setMax(100); // Set max to 100 for percentage
                        int progress = (int) ((float) playerState.playbackPosition / track.duration * 100);
                        seekBar.setProgress(progress);
                        totalDuration.setText(formatDuration(track.duration));

                        String trackId = track.uri.substring(track.uri.lastIndexOf(":") + 1);
                        // Set as current song only if the Redux store's current song is null
                        Song currentSong = model.getCurrentSong().getValue();
                        if (currentSong == null || !currentSong.getSongID().equals(trackId)) {
                            Log.d(TAG, "setUpPlayerControls: is it triggered on load version 2 ??" + track);
                            Song curr = new Song(trackId, track.name, track.artist.name, String.valueOf(track.duration));
                            curr.setCurrentPosition(String.valueOf(playerState.playbackPosition));
                            curr.setIsPLaying(!playerState.isPaused);
                            model.getCurrentSong().postValue(curr);
                            if (webSocketService != null) {
                                sendCurrentSongToFriends(track.name);
                            }
                        }
                        handler.post(runnable);
                    }
                });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            long newProgress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                newProgress = (long) (trackDuration[0] * (progress / 100.0));
                currentDuration.setText(formatDuration(newProgress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(runnable);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSpotifyAppRemote.getPlayerApi().seekTo(newProgress);
                Song currSong = model.getCurrentSong().getValue();
                currSong.setCurrentPosition(String.valueOf(newProgress));
                model.getCurrentSong().postValue(currSong);

                handler.post(runnable);
                if(webSocketService!=null && model.checkSessionActive().getValue()){
                    JSONObject messageToSend = new JSONObject();
                    try {
                        messageToSend.put("method", "SESSION");
                        messageToSend.put("action", "queueSeek");
                        messageToSend.put("body", new JSONObject().put("seekPosition", newProgress));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    webSocketService.sendMessage(messageToSend.toString());
                }
            }
        });

        playpauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
                    if (playerState.isPaused) {
                        model.setCurrentSongPlaying(true);
                        mSpotifyAppRemote.getPlayerApi().resume();
                        if(webSocketService!=null && model.checkSessionActive().getValue()){
                            JSONObject messageToSend = new JSONObject();
                            try {
                                messageToSend.put("method", "SESSION");
                                messageToSend.put("action", "queueResume");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            webSocketService.sendMessage(messageToSend.toString());
                        }
                        playpauseButton.setBackgroundResource(R.drawable.pause_btn);

                    } else {
                        model.setCurrentSongPlaying(false);
                        mSpotifyAppRemote.getPlayerApi().pause();
                        if(webSocketService!=null && model.checkSessionActive().getValue()){
                            JSONObject messageToSend = new JSONObject();
                            try {
                                messageToSend.put("method", "SESSION");
                                messageToSend.put("action", "queuePause");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            webSocketService.sendMessage(messageToSend.toString());
                        }
                        playpauseButton.setBackgroundResource(R.drawable.play_btn);
                    }
                });
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextSong();
                updateFriendsAboutSongChange();
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpotifyAppRemote.getPlayerApi().seekTo(0);
                if(webSocketService!=null && model.checkSessionActive().getValue()){
                    JSONObject messageToSend = new JSONObject();
                    try {
                        messageToSend.put("method", "SESSION");
                        messageToSend.put("action", "queueSeek");
                        messageToSend.put("body", new JSONObject().put("seekPosition", 0));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    webSocketService.sendMessage(messageToSend.toString());
                }
            }
        });

        mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
            if (playerState.isPaused) {
                playpauseButton.setBackgroundResource(R.drawable.play_btn);
            } else {
                playpauseButton.setBackgroundResource(R.drawable.pause_btn);
            }
        });
    }
    private void initializeObservers() {
        model.getCurrentSong().observe(getViewLifecycleOwner(), currentSong -> {
            Log.d(TAG, "updateCurrentSongUI: name:: " + currentSong.getSongName() + " artist:: " + currentSong.getSongArtist() + " duration:: " + currentSong.getDuration() + " currentPos:: " + currentSong.getCurrentPosition() + " isPlaying:: " + currentSong.getIsPLaying());
            if (currentSong != null && mSpotifyAppRemote != null) {
//            songTitle.setText(currentSong.getSongName());
//            songArtist.setText(currentSong.getSongArtist());
//            getCurrentPosition is in milliseconds
            int progress = (int) ((float) Long.parseLong(currentSong.getCurrentPosition()) / Long.parseLong(currentSong.getDuration()) * 100);
            seekBar.setProgress(progress);
            currentDuration.setText(formatDuration(Long.parseLong(currentSong.getCurrentPosition())));
            totalDuration.setText(formatDuration(Long.parseLong(currentSong.getDuration())));
//                mSpotifyAppRemote.getPlayerApi().play("spotify:track:" + currentSong.getSongID());
//                mSpotifyAppRemote.getPlayerApi().seekTo(Long.parseLong(currentSong.getCurrentPosition()));
//                if(currentSong.getIsPLaying()){
//                    mSpotifyAppRemote.getPlayerApi().resume();
//                }else{
//                    mSpotifyAppRemote.getPlayerApi().pause();
//                }
            }
        });

        // Observe the song queue LiveData
        model.getSongQueue().observe(getViewLifecycleOwner(), songQueue -> {
            if (songQueue != null && model.getCurrentSong()==null) {
                if(songQueue.size()>0){
                    model.getCurrentSong().postValue(new Song(songQueue.get(0).getSongID(),songQueue.get(0).getSongName() , songQueue.get(0).getDuration(),String.valueOf(System.currentTimeMillis())));
                    songQueue.remove(0);
                    model.getSongQueue().postValue(songQueue);
                }
            }
        });

        // Observe the session active LiveData
        model.checkSessionActive().observe(getViewLifecycleOwner(), this::updateSessionUI);
    }


    // Update UI components related to the session active state
    private void updateSessionUI(Boolean isActive) {
        chatBtn.setVisibility(isActive ? View.VISIBLE : View.GONE);
        exitBtn.setVisibility(isActive ? View.VISIBLE : View.GONE);
    }


    // Utility Methods
    private String formatDuration(long duration) {
        int seconds = (int) (duration / 1000) % 60;
        int minutes = (int) ((duration / (1000 * 60)) % 60);

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
    private void switchFragment(int frameId, Fragment frag) {
        getFragmentManager()
                .beginTransaction()
                .replace(frameId, frag)
                .addToBackStack(null)
                .commit();
    }
    private void filterSuggestions(String newText) {
        ArrayList<Song> filteredSuggestions = new ArrayList<>();

        String query = "track:" + encodeSongTitle(newText);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    JsonObject rawResponse = spotifyClient.getSong(query);
                    JsonArray songItems = rawResponse.get("tracks").getAsJsonObject().get("items").getAsJsonArray();

                    for (int i = 0; i < songItems.size(); i++) {
                        JsonObject song = songItems.get(i).getAsJsonObject();
                        JsonArray artists = song.get("artists").getAsJsonArray();
                        filteredSuggestions.add(new Song(song.get("id").getAsString(), song.get("name").getAsString(), artists.get(0).getAsJsonObject().get("name").getAsString(), String.valueOf(song.get("duration_ms").getAsInt())));
                        Log.d("RoomFragment", "name of song: "+song.get("name"));
                    }

                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // After obtaining the filtered suggestions, update the adapter
                            searchAdapter.clear();
                            searchAdapter.addAll(filteredSuggestions);
                            searchAdapter.notifyDataSetChanged();

                            // Show/hide the suggestion list based on whether suggestions are available
                            if (filteredSuggestions.isEmpty()) {
                                suggestionListView.setVisibility(View.GONE);
                            } else {
                                suggestionListView.setVisibility(View.VISIBLE);
                                suggestionListView.bringToFront();
                            }
                        }
                    });

                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private String encodeSongTitle(String title) {
        String encoded;
        try {
            encoded = URLEncoder.encode(title, Charsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return encoded;
    }
    // ChatGPT Usage: No
    private void addSongToQueue(Song suggestion) {
        if (model.checkSessionActive().getValue()) {
            JSONObject message = new JSONObject();
            try {
                message.put("method", "SESSION");
                message.put("action", "queueAdd");
                JSONObject body = new JSONObject();
                body.put("uri", suggestion.getSongID());
                body.put("durationMs", suggestion.getDuration());
                body.put("title", suggestion.getSongName());
                body.put("artist", suggestion.getSongArtist());
                message.put("body", body);
                if (webSocketService != null) {
                    webSocketService.sendMessage(message.toString());
                } else {
                    Log.e(TAG, "WebSocketService is not available");
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to create JSON message for adding song to queue", e);
            }
        }

        List<Song> existingQueue = model.getSongQueue().getValue();
        if (existingQueue == null) {
            existingQueue = new ArrayList<>();
        }
        for (Song s : existingQueue) {
            if (s.getSongID().equals(suggestion.getSongID())) {
                return;
            }
        }
        existingQueue.add(suggestion);
        model.getSongQueue().postValue(existingQueue);
        suggestionListView.setVisibility(View.GONE);
        songSearchBar.setQuery("", false);
        songSearchBar.clearFocus();
    }
    private void updateFriendsAboutSongChange() {
        if (webSocketService != null && model.checkSessionActive().getValue()) {
            JSONObject messageToSend = new JSONObject();
            try {
                messageToSend.put("method", "SESSION");
                messageToSend.put("action", "queueSkip");
                webSocketService.sendMessage(messageToSend.toString());
            } catch (JSONException e) {
                Log.e(TAG, "Failed to create JSON message for updating friends about song change", e);
            }
        } else {
            Log.e(TAG, "WebSocketService is not available or session is not active");
        }
    }

    private void sendCurrentSongToFriends(String songName) {
        if (webSocketService != null) {
            JSONObject messageToSend = new JSONObject();
            try {
                messageToSend.put("method", "FRIENDS");
                messageToSend.put("action", "update");
                JSONObject body = new JSONObject();
                body.put("song", songName);
                messageToSend.put("body", body);
                webSocketService.sendMessage(messageToSend.toString());
            } catch (JSONException e) {
                Log.e(TAG, "Failed to create JSON message for updating friends about song change", e);
            }
        } else {
            Log.e(TAG, "WebSocketService is not available or session is not active");
        }
    }

    private void playNextSong() {
        Log.d(TAG, "playNextSong: is it triggered?");
        List<Song> songQueue = model.getSongQueue().getValue();
        if (songQueue == null || songQueue.isEmpty()) {
            Toast.makeText(getContext(), "Queue is empty", Toast.LENGTH_SHORT).show();
            Song currentSong = model.getCurrentSong().getValue();
            mSpotifyAppRemote.getPlayerApi().seekTo(0);
            mSpotifyAppRemote.getPlayerApi().pause();
            currentSong.setIsPLaying(false);
            model.getCurrentSong().postValue(currentSong);
            return;
        }
        Song nextSong = songQueue.remove(0);
        model.getSongQueue().postValue(songQueue);
        nextSong.setCurrentPosition("0");
        nextSong.setIsPLaying(true);
        model.getCurrentSong().postValue(nextSong);
        mSpotifyAppRemote.getPlayerApi().play("spotify:track:" + nextSong.getSongID());
        // Send WebSocket message to update friends
        //  updateFriendsAboutSongChange(nextSong);
    }
//    private void playNextSong() {
//        List<Song> songQueue = model.getSongQueue().getValue();
//        if (songQueue == null || songQueue.isEmpty()) {
//            Toast.makeText(getContext(), "Queue is empty", Toast.LENGTH_SHORT).show();
//            Song currentSong = model.getCurrentSong().getValue();
//            mSpotifyAppRemote.getPlayerApi().seekTo(0);
//            mSpotifyAppRemote.getPlayerApi().pause();
//            if (currentSong != null) {
//                currentSong.setIsPLaying(false);
//                model.getCurrentSong().postValue(currentSong);
//            }
//            return;
//        }
//        Song nextSong = songQueue.remove(0);
//        model.getSongQueue().postValue(songQueue);
//        nextSong.setCurrentTimestamp(String.valueOf(System.currentTimeMillis()));
//        nextSong.setIsPLaying(true);
//        model.getCurrentSong().postValue(nextSong);
//        mSpotifyAppRemote.getPlayerApi().play("spotify:track:" + nextSong.getSongID());
//    }
}
