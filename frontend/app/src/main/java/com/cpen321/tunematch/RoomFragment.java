package com.cpen321.tunematch;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.ImageUri;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import jp.wasabeef.blurry.Blurry;


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
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_room, container, false);

        Button chatBtn = view.findViewById(R.id.chatBtn);
        Button queueBtn = view.findViewById(R.id.queueBtn);



        return view;
    }

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

    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

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

    }
    private String formatDuration(long duration) {
        int seconds = (int) (duration / 1000) % 60;
        int minutes = (int) ((duration / (1000 * 60)) % 60);

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }





}
