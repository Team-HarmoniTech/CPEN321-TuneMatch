package com.cpen321.tunematch;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
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
import okhttp3.Headers;


public class RoomFragment extends Fragment {
    private View view;
    private static final String CLIENT_ID = "0dcb406f508a4845b32a1342a91a71af";
    private static final String REDIRECT_URI = "cpen321tunematch://callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    private Button playpauseButton;
    private Button nextButton;
    private Button prevButton;
    private SeekBar seekBar;
    private ImageView songBanner;
    private TextView songTitle;
    private TextView songArtist;
    private TextView currentDuration;
    private TextView totalDuration;
    private ChatFragment chatFrag;
    private QueueFragment queueFrag;
    private ArrayAdapter<String> searchAdapter;
    private ListView suggestionListView;
    private String authToken;
    private ApiClient spotifyApiClient;

    ReduxStore model;
    ApiClient apiClient;
    CurrentSession currentSession;
    private Button chatBtn;
    private Button queueBtn;
    private Button exitBtn;
    private WebSocketService webSocketService;
    private boolean isServiceBound = false;

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

    // ChatGPT Usage: Partial
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize ViewModel and ApiClient here.
        model = ((MainActivity) getActivity()).getModel();
        apiClient = ((MainActivity) getActivity()).getApiClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response;
                try {
                    response = apiClient.doGetRequest("/me/matches", true);
                    // Parse the response.
//                    List<SearchUser> newSearchList = parseResponse(response);
                    // Update LiveData.
//                    model.getSearchList().postValue(newSearchList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    // ChatGPT Usage: partial
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_room, container, false);
        chatBtn = view.findViewById(R.id.chatBtn);
        queueBtn = view.findViewById(R.id.queueBtn);
        exitBtn = view.findViewById(R.id.exitBtn);
        Log.d("RoomFragment", "onCreateView: session :: "+model.checkCurrentSessionActive());
        if(!model.checkCurrentSessionActive()) {
            chatBtn.setVisibility(View.GONE);
            exitBtn.setVisibility(View.GONE);
        }
  
        chatFrag = new ChatFragment();
        queueFrag = new QueueFragment();

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

        // Retrieve the authentication token
        SharedPreferences preferences = getActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        authToken = preferences.getString("auth_token", null);

        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer "+authToken)
                .build();

        // Initialize your ApiClient with the base URL and custom headers
        String spotifyBaseUrl = "https://api.spotify.com/v1/";
        spotifyApiClient = new ApiClient(spotifyBaseUrl, headers);

        // initialize to chat
        switchFragment(R.id.subFrame, chatFrag);

        SearchView songSearchBar = view.findViewById(R.id.songSearchBar);

        // Create an adapter for suggestions (for example, ArrayAdapter)
        searchAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<>());
        suggestionListView = view.findViewById(R.id.suggestionListView);
        suggestionListView.setAdapter(searchAdapter);
        suggestionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedText = (String) parent.getItemAtPosition(position);
                String selectedSong = selectedText.split(" - ")[0];
                String selectedArtist = selectedText.split(" - ")[1];

                String endpoint = "search/?q=track:"+encodeSongTitle(selectedSong)+"&type=track";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String songListJson = spotifyApiClient.doGetRequest(endpoint, true);
                            JSONObject rawResponse = new JSONObject(songListJson);
                            JSONArray songItems = rawResponse.getJSONObject("tracks").getJSONArray("items");

                            for (int i = 0; i < songItems.length(); i++) {
                                JSONObject song = songItems.getJSONObject(i);
                                String artist = song.getJSONArray("artists").getJSONObject(0).getString("name");
                                if (song.getString("name").equals(selectedSong) && artist.equals(selectedArtist)) {
                                    JSONObject body = new JSONObject();
                                    body.put("uri", song.getString("uri"));
                                    body.put("durationMs", song.getString("duration_ms"));

                                    JSONObject messageToSend = new JSONObject();
                                    try {
                                        messageToSend.put("method", "SESSION");
                                        messageToSend.put("action", "queueAdd");
                                        messageToSend.put("body", body);
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }

                                    Log.d("RoomFragment", "isServiceBound:"+isServiceBound);
                                    if (isServiceBound && webSocketService != null) {
                                        Log.d("RoomFragment", "Add to queue: " + messageToSend);
                                        webSocketService.sendMessage(messageToSend.toString());
                                    }
                                    break;
                                }
                            }

                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                }
                            });

                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        // ChatGPT Usage: Partial
        songSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")) {
                    suggestionListView.setVisibility(View.GONE);
                    Log.d("RoomFragment", "onQueryTextChange: change suggestionListView visibility to GONE");
                } else {
                    filterSuggestions(newText);
                }
                return false;
            }
        });
  
        return view;
    }

    // ChatGPT Usage: Partial
    @Override
    public void onStart() {
        super.onStart();
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(getContext(), connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();

                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    // ChatGPT Usage: No
    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    // ChatGPT Usage: Partial
    public void connected() {
        // Play a playlist
        playpauseButton = view.findViewById(R.id.play_button);
        nextButton = view.findViewById(R.id.next_button);
        prevButton = view.findViewById(R.id.previous_button);
        seekBar = view.findViewById(R.id.seekBar);
        songBanner = view.findViewById(R.id.song_banner_imageview);
        songTitle = view.findViewById(R.id.song_name_text);
        songArtist = view.findViewById(R.id.singer_name_text);
        currentDuration = view.findViewById(R.id.start_time_text);
        totalDuration = view.findViewById(R.id.end_time_text);

//        use spotify api to get the album art, the user is logged in, so use its token
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
                        Log.d(TAG, track.name + " by " + track.artist.name);
                        // Load image into ImageView
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

                        // Update song title and artist
                        songTitle.setText(track.name);
                        songArtist.setText(track.artist.name);

                        // Update seekbar and duration
                        seekBar.setMax(100); // Set max to 100 for percentage
                        int progress = (int) ((float) playerState.playbackPosition / track.duration * 100);
                        seekBar.setProgress(progress);
                        totalDuration.setText(formatDuration(track.duration));

                        // Start updating current duration every second
                        handler.post(runnable);
                    }
                });

        final long[] trackDuration = {0}; // Add this line at the beginning of your class


        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        trackDuration[0] = track.duration; // Store track duration
                        // ... rest of your code ...
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
                // TODO Auto-generated method stub
                handler.removeCallbacks(runnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
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

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpotifyAppRemote.getPlayerApi().pause();
                model.getCurrentSession().postValue(null);
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavi);
                bottomNavigationView.setSelectedItemId(R.id.navigation_home);
            }
        });
    }

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

        String endpoint = "search/?q=track:"+encodeSongTitle(newText)+"&type=track";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String songListJson = spotifyApiClient.doGetRequest(endpoint, true);
                    JSONObject rawResponse = new JSONObject(songListJson);
                    JSONArray songItems = rawResponse.getJSONObject("tracks").getJSONArray("items");

                    for (int i = 0; i < songItems.length(); i++) {
                        JSONObject song = songItems.getJSONObject(i);
                        JSONArray artists = song.getJSONArray("artists");
                        filteredSuggestions.add(song.getString("name")+" - "+artists.getJSONObject(0).getString("name"));
                        Log.d("RoomFragment", "name of song: "+song.getString("name"));
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
                                Log.d("RoomFragment", "change suggestion visibility to GONE");
                            } else {
                                suggestionListView.setVisibility(View.VISIBLE);
                                suggestionListView.bringToFront();
                                Log.d("RoomFragment", "change suggestion visibility to VISIBLE");
                            }
                        }
                    });

                } catch (IOException | JSONException e) {
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
            Log.d("RoomFragment", "encodedTitle: " + encoded);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return encoded;
    }
}
