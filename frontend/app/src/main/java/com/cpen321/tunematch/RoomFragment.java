package com.cpen321.tunematch;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.text.Charsets;

public class RoomFragment extends Fragment {
    final Handler handler = new Handler(Looper.getMainLooper());
    private final List<JSONObject> fullSongDataList = new ArrayList<>();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    // Views
    private View view;
    private Button playpauseButton;
    private Button nextButton;
    private Button restartButton;
    private Button chatBtn;
    private Button queueBtn;
    private Button exitBtn;
    private SeekBar seekBar;
    private ImageView songBanner;
    private TextView songTitle;
    private TextView songArtist;
    private TextView currentDuration;
    private TextView totalDuration;
    private SearchView songSearchBar;
    private ListView suggestionListView;
    // Services and other fields
    private SpotifyAppRemote mSpotifyAppRemote;
    private ChatFragment chatFrag;
    private QueueFragment queueFrag;
    private ArrayAdapter<String> searchAdapter; // Changed to ArrayAdapter<String>
    private long currentSongTotalDuration = 0;
    private SpotifyClient spotifyClient;
    private ReduxStore model;
    private WebSocketService webSocketService;
//    private long currentPosition = 0;
    private Runnable searchRunnable;
//    private Song lastSongState = null;

    // ChatGPT Usage: Partial
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                    long newPosition = model.getCurrentPosition().getValue()+1000;
                    model.setCurrentPosition(newPosition);
                    if ((currentSongTotalDuration - newPosition < 3000)) {
                        playNextSong();
                    }
                handler.postDelayed(this, 1000);
            }
        }
    };

    // ChatGPT Usage: Partial
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initServices();
    }

    // ChatGPT Usage: No
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_room, container, false);
        initializeViews();
        initializeEventListeners();
        return view;
    }

    // ChatGPT Usage: No
    @Override
    public void onStart() {
        super.onStart();
        initializeObservers();
        setUpPlayerControls();
    }

    // ChatGPT Usage: Partial
    private void initServices() {
        // Initialize ViewModel and ApiClient
        model = ReduxStore.getInstance();

        // Get instances of MainActivity, WebSocketService, and SpotifyService
        MainActivity mainActivity = (MainActivity) getActivity();
        webSocketService = mainActivity.getWebSocketService();
        SpotifyService mSpotifyService = mainActivity.getSpotifyService();
        mSpotifyAppRemote = mSpotifyService.getSpotifyAppRemote();
    }

    // ChatGPT Usage: No
    private void initializeViews() {
        chatBtn = view.findViewById(R.id.chatBtn);
        queueBtn = view.findViewById(R.id.queueBtn);
        exitBtn = view.findViewById(R.id.exitBtn);
        playpauseButton = view.findViewById(R.id.play_button);
        nextButton = view.findViewById(R.id.next_button);
        restartButton = view.findViewById(R.id.restart_button);
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

        // Set the queue fragment to be the default fragment in the subFrame
        switchFragment(R.id.subFrame, queueFrag);

        // Initialize the search adapter and set it to the suggestion list view
        searchAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<String>());
        suggestionListView.setAdapter(searchAdapter);

        // Retrieve the authentication token
        SharedPreferences preferences = getActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        String authToken = preferences.getString("auth_token", null);

        // Initialize the Spotify API client with the base URL and custom headers
        spotifyClient = new SpotifyClient(authToken);
    }

    // ChatGPT Usage: No
    private void initializeEventListeners() {
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(R.id.subFrame, chatFrag);
            }
        });

        queueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(R.id.subFrame, queueFrag);
            }
        });

        songSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                suggestionListView.setVisibility(View.GONE);
                // Consider calling filterSuggestions(query) here if you want to search immediately on submit
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // User input changed, cancel the previous searchRunnable if it exists
                searchHandler.removeCallbacks(searchRunnable);

                if (TextUtils.isEmpty(newText)) {
                    suggestionListView.setVisibility(View.GONE);
                } else {
                    // Define a new searchRunnable with the updated text
                    searchRunnable = () -> filterSuggestions(newText);

                    // Schedule the searchRunnable to run after a delay (e.g., 500ms)
                    searchHandler.postDelayed(searchRunnable, 200);
                }
                return false; // Return true since the listener has handled the query text change
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
                    Log.d("RoomFragment", "Error: " + e.getMessage());
                }
                if (webSocketService != null) {
                    model.checkSessionActive().postValue(false);
                    webSocketService.sendMessage(messageToSend.toString());
                }
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavi);
                bottomNavigationView.setSelectedItemId(R.id.navigation_home);
            }
        });

        suggestionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject selectedSuggestion = fullSongDataList.get(position);
                try {
                    String songId = selectedSuggestion.getString("id");
                    String songName = selectedSuggestion.getString("name");
                    String songArtist = selectedSuggestion.getString("artist");
                    long songDuration = selectedSuggestion.getLong("duration");
                    Song song = new Song(songId, songName, songArtist, songDuration);
                    addSongToQueue(song);
                } catch (JSONException e) {
                    Log.d("RoomFragment", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void setUpPlayerControls() {
        songBanner.setImageResource(R.color.darkGray);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Required function
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(runnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                long newProgress = (long) (currentSongTotalDuration * (progress / 100.0));
                // Update the model with the new position
                model.setCurrentPosition(newProgress);
                mSpotifyAppRemote.getPlayerApi().seekTo(newProgress);
                if (webSocketService != null && model.checkSessionActive().getValue()) {
                    JSONObject messageToSend = new JSONObject();
                    try {
                        messageToSend.put("method", "SESSION");
                        messageToSend.put("action", "queueSeek");
                        messageToSend.put("body", new JSONObject().put("seekPosition", newProgress));
                    } catch (JSONException e) {
                        Log.d("RoomFragment", "Error: " + e.getMessage());
                    }
                    webSocketService.sendMessage(messageToSend.toString());
                }
                if(model.getCurrentSong().getValue().isPlaying()) {
                    handler.post(runnable);
                }
            }
        });

        playpauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Song currSong = model.getCurrentSong().getValue();
                Boolean isPlaying = currSong.isPlaying();
                if (!isPlaying) {
                    if (webSocketService != null && model.checkSessionActive().getValue()) {
                        JSONObject messageToSend = new JSONObject();
                        try {
                            messageToSend.put("method", "SESSION");
                            messageToSend.put("action", "queueResume");
                        } catch (JSONException e) {
                            Log.d("RoomFragment", "Error: " + e.getMessage());
                        }
                        webSocketService.sendMessage(messageToSend.toString());
                    }
                    currSong.setIsPLaying(true);
                    mSpotifyAppRemote.getPlayerApi().resume();
                } else {
                    if (webSocketService != null && model.checkSessionActive().getValue()) {
                        JSONObject messageToSend = new JSONObject();
                        try {
                            messageToSend.put("method", "SESSION");
                            messageToSend.put("action", "queuePause");
                        } catch (JSONException e) {
                            Log.d("RoomFragment", "Error: " + e.getMessage());
                        }
                        webSocketService.sendMessage(messageToSend.toString());
                    }
                    currSong.setIsPLaying(false);
                    mSpotifyAppRemote.getPlayerApi().pause();
                }
                model.getCurrentSong().postValue(currSong);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextSong();
            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                model.setCurrentSongPosition(0);
                model.setCurrentPosition(0);
                mSpotifyAppRemote.getPlayerApi().seekTo(0);
                if (webSocketService != null && model.checkSessionActive().getValue()) {
                    JSONObject messageToSend = new JSONObject();
                    try {
                        messageToSend.put("method", "SESSION");
                        messageToSend.put("action", "queueSeek");
                        messageToSend.put("body", new JSONObject().put("seekPosition", 0));
                    } catch (JSONException e) {
                        Log.d("RoomFragment", "Error: " + e.getMessage());
                    }
                    webSocketService.sendMessage(messageToSend.toString());
                }
            }
        });
    }

    // ChatGPT Usage: Partial
    private void initializeObservers() {
        model.getCurrentPosition().observe(getViewLifecycleOwner(), newPosition -> {
            // Update the seek bar and current position text view
//            Log.e(TAG, "initializeObservers: seekbar :: " + newPosition + " " + model.getCurrentSong().getValue().getDuration());
            float total = 0;

            if(model.getCurrentSong().getValue() != null) {
                total = model.getCurrentSong().getValue().getDuration();
            }
            int progress = (int) ((float) newPosition / (float) total * 100);
            seekBar.setProgress(progress);
            currentDuration.setText(formatDuration(newPosition));
        });




        model.getCurrentSong().observe(getViewLifecycleOwner(), newSong -> {
//            Log.e(TAG, "SONG HAS BEEN CHANGED SIR :: " + newSong.getSongID() + " " + newSong.getSongName() + " " + newSong.getSongArtist() + " " + newSong.getDuration() + " " + newSong.getCurrentPosition() + " " + newSong.isPlaying());
            if (newSong != null) {
                currentSongTotalDuration = newSong.getDuration();
                songTitle.setText(newSong.getSongName());
                songArtist.setText(newSong.getSongArtist());
                totalDuration.setText(formatDuration(newSong.getDuration()));
                if (newSong.isPlaying()) {
                    handler.removeCallbacks(runnable);
//                    Log.e(TAG, "initializeObservers: handler is being instantiated" );
                    handler.post(runnable);
                    playpauseButton.setBackgroundResource(R.drawable.pause_btn);
                } else {
                    handler.removeCallbacks(runnable);
                    playpauseButton.setBackgroundResource(R.drawable.play_btn);
                }
//                int progress = (int) ((float) newSong.getCurrentPosition() / newSong.getDuration() * 100);
//                seekBar.setProgress(progress);
//                currentDuration.setText(formatDuration(newSong.getCurrentPosition()));

            }
        });

        // Observe the song queue LiveData
        model.getSongQueue().observe(getViewLifecycleOwner(), songQueue -> {
            if (songQueue != null && model.getCurrentSong().getValue() == null && songQueue.size() > 0) {
//                Log.e(TAG, "handleSession: song queue changed:: " + songQueue + ", current song:: " + model.getCurrentSong().getValue());
                Song replace = songQueue.get(0);
                replace.setIsPLaying(true);
                model.getCurrentSong().postValue(replace);
                songQueue.remove(0);
                model.getSongQueue().postValue(songQueue);
            }
        });

        // Observe the session active LiveData
        model.checkSessionActive().observe(getViewLifecycleOwner(), this::updateSessionUI);
    }


    @Override
    public void onStop() {
        super.onStop();
        Song currSong = model.getCurrentSong().getValue();
//        currSong.setCurrentPosition(currentPosition);
        model.getCurrentSong().postValue(currSong);
//        handler.removeCallbacks(runnable);
//        Log.e(TAG, "onStop: " + "handler has been stopped.. lol" );
    }

    // Update UI components related to the session active state
    // ChatGPT Usage: No
    private void updateSessionUI(Boolean isActive) {
        chatBtn.setVisibility(isActive ? View.VISIBLE : View.GONE);
        exitBtn.setVisibility(isActive ? View.VISIBLE : View.GONE);
    }

    // Utility Methods
    // ChatGPT Usage: No
    private String formatDuration(long duration) {
        int seconds = (int) (duration / 1000) % 60;
        int minutes = (int) ((duration / (1000 * 60)) % 60);
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    // ChatGPT Usage: No
    private void switchFragment(int frameId, Fragment frag) {
        getFragmentManager()
                .beginTransaction()
                .replace(frameId, frag)
                .addToBackStack(null)
                .commit();
    }

    private void filterSuggestions(String newText) {
        String query = "track:" + encodeSongTitle(newText);

        // Use an executor service to perform network operations
        executorService.execute(() -> {
            try {
                JsonObject rawResponse = spotifyClient.getSong(query);
                JsonArray songItems = rawResponse.get("tracks").getAsJsonObject().get("items").getAsJsonArray();

                // Only clear the list when new data is being added
                ArrayList<String> filteredSuggestions = new ArrayList<>();
                ArrayList<JSONObject> newSongDataList = new ArrayList<>();

                for (JsonElement item : songItems) {
                    JsonObject song = item.getAsJsonObject();
                    JsonObject artists = song.get("artists").getAsJsonArray().get(0).getAsJsonObject();

                    JSONObject newSong = new JSONObject();
                    newSong.put("id", song.get("id").getAsString());
                    newSong.put("name", song.get("name").getAsString());
                    newSong.put("artist", artists.get("name").getAsString());
                    newSong.put("duration", song.get("duration_ms").getAsString());

                    // Add the formatted string to the display list
                    String displayString = newSong.getString("name") + " - " + newSong.getString("artist");
                    filteredSuggestions.add(displayString);

                    // Add the full JSON object to the new song data list
                    newSongDataList.add(newSong);
                }

                mainHandler.post(() -> {
                    fullSongDataList.clear();
                    fullSongDataList.addAll(newSongDataList);

                    // After obtaining the filtered suggestions, update the adapter
                    searchAdapter.clear();
                    searchAdapter.addAll(filteredSuggestions);
                    searchAdapter.notifyDataSetChanged();

                    // Show/hide the suggestion list based on whether suggestions are available
                    suggestionListView.setVisibility(filteredSuggestions.isEmpty() ? View.GONE : View.VISIBLE);
                    if (!filteredSuggestions.isEmpty()) {
                        suggestionListView.bringToFront();
                    }
                });

            } catch (ApiException | JSONException e) {
                // Handle errors appropriately (e.g., display a message to the user)
                mainHandler.post(() -> showErrorToUser(e));
            }
        });
    }

    private void showErrorToUser(Exception e) {
        Log.e(TAG, "showErrorToUser: ", e);
        // Implement user-friendly error handling
    }

    // ChatGPT Usage: No
    private String encodeSongTitle(String title) {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(title, Charsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            Log.d("RoomFragment", "Error: " + e.getMessage());
        }
        return encoded;
    }

    // ChatGPT Usage: No
    private void addSongToQueue(Song suggestion) {
        if (Boolean.TRUE.equals(model.checkSessionActive().getValue())) {
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

    // ChatGPT Usage: No
    private void playNextSong() {
        List<Song> songQueue = model.getSongQueue().getValue();
        if (songQueue == null || songQueue.isEmpty()) {
            try {
                Toast.makeText(requireContext(), "Queue is empty", Toast.LENGTH_SHORT).show();
            } catch (IllegalStateException e) {e.printStackTrace();}
            Song currentSong = model.getCurrentSong().getValue();
            currentSong.setIsPLaying(false);
            currentSong.setCurrentPosition(0);
            model.getCurrentSong().postValue(currentSong);
            model.setCurrentPosition(0);
            mSpotifyAppRemote.getPlayerApi().pause();
            mSpotifyAppRemote.getPlayerApi().seekTo(0);
            return;
        }
        Song nextSong = songQueue.remove(0);
        model.getSongQueue().postValue(songQueue);
        nextSong.setCurrentPosition(0);
        model.setCurrentPosition(0);
        nextSong.setIsPLaying(true);
        model.getCurrentSong().postValue(nextSong);
        mSpotifyAppRemote.getPlayerApi().play("spotify:track:" + nextSong.getSongID());
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

}