package com.cpen321.tunematch;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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
    private ArrayAdapter<String> searchAdapter;
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

        // Observer to check if session is active and modify button visibility accordingly
        model.checkSessionActive().observe(getViewLifecycleOwner(), isActive -> {
            Log.d("RoomFragment", "Session is active: " + isActive);
            if (isActive) {
                chatBtn.setVisibility(View.VISIBLE);
                exitBtn.setVisibility(View.VISIBLE);
            } else {
                chatBtn.setVisibility(View.GONE);
                exitBtn.setVisibility(View.GONE);
            }
        });

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
                model.getCurrentSession().postValue(new CurrentSession(null,null));
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavi);
                bottomNavigationView.setSelectedItemId(R.id.navigation_home);
            }
        });
    }
    private void setUpPlayerControls() {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
                    currentDuration.setText(formatDuration(playerState.playbackPosition));
                    int progress = (int) ((float) playerState.playbackPosition / playerState.track.duration * 100);
                    seekBar.setProgress(progress);
                    handler.postDelayed(this, 1000);
                });
            }
        };

        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
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
                        handler.post(runnable);
                    }
                });

        final long[] trackDuration = {0};
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        trackDuration[0] = track.duration;
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
                handler.post(runnable);
            }
        });

        playpauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
                    if (playerState.isPaused) {
                        mSpotifyAppRemote.getPlayerApi().resume();
                        playpauseButton.setText("Pause");
                    } else {
                        mSpotifyAppRemote.getPlayerApi().pause();
                        playpauseButton.setText("Play");
                    }
                });
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpotifyAppRemote.getPlayerApi().skipNext();
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpotifyAppRemote.getPlayerApi().skipPrevious();
            }
        });

        mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
            if (playerState.isPaused) {
                playpauseButton.setText("Play");
            } else {
                playpauseButton.setText("Pause");
            }
        });
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
  
    // ChatGPT Usage: Partial
    private void filterSuggestions(String newText) {
        // Filter the suggestions based on the newText and update the adapter
        ArrayList<String> filteredSuggestions = new ArrayList<>();

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
                        filteredSuggestions.add(song.get("name").toString() + " - " + artists.get(0).getAsJsonObject().get("name").toString());
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

    // ChatGPT Usage: Partial
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

    public WebSocketService getWebSocketService() {
        return webSocketService;
    }

    public SpotifyService getSpotifyService() {
        return mSpotifyService;
    }
}
