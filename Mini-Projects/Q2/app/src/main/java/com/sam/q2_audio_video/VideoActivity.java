package com.sam.q2_audio_video;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatActivity;

public class VideoActivity extends AppCompatActivity {

    // --- UI elements ---
    private VideoView videoView;
    private TextView textStatus;

    // --- Video URL passed from MainActivity ---
    private String videoUrl;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        // Set toolbar with back arrow
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarVideo);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Video Player");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Link views
        videoView   = findViewById(R.id.videoView);
        Button btnPlay = findViewById(R.id.btnVideoPlay);
        Button btnPause = findViewById(R.id.btnVideoPause);
        Button btnStop = findViewById(R.id.btnVideoStop);
        Button btnRestart = findViewById(R.id.btnVideoRestart);
        textStatus  = findViewById(R.id.textVideoStatus);

        // Get the URL sent from MainActivity
        videoUrl = getIntent().getStringExtra("VIDEO_URL");

        if (videoUrl == null || videoUrl.isEmpty()) {
            Toast.makeText(this, "No URL provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load the video URL into VideoView
        loadVideo(videoUrl);

        // --- Button: Play ---
        btnPlay.setOnClickListener(v -> {
            if (!videoView.isPlaying()) {
                videoView.start();
                textStatus.setText("Status: Playing");
            }
        });

        // --- Button: Pause ---
        btnPause.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.pause();
                textStatus.setText("Status: Paused");
            }
        });

        // --- Button: Stop ---
        btnStop.setOnClickListener(v -> {
            videoView.stopPlayback();
            textStatus.setText("Status: Stopped");
            // Reload so it can play again
            loadVideo(videoUrl);
        });

        // --- Button: Restart ---
        btnRestart.setOnClickListener(v -> {
            videoView.seekTo(0);
            videoView.start();
            textStatus.setText("Status: Restarted");
        });
    }

    /**
     * Loads a video URL into the VideoView.
     */
    @SuppressLint("SetTextI18n")
    private void loadVideo(String url) {
        textStatus.setText("Status: Loading...");
        Uri uri = Uri.parse(url);
        videoView.setVideoURI(uri);

        // Called when video is ready to play
        videoView.setOnPreparedListener(mp -> {
            textStatus.setText("Status: Ready — press Play");
            // Make video scale properly
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            Toast.makeText(this, "Video loaded!", Toast.LENGTH_SHORT).show();
        });

        // Called when video finishes
        videoView.setOnCompletionListener(mp -> {
            textStatus.setText("Status: Completed");
        });

        // Called on error (e.g. no internet, bad URL)
        videoView.setOnErrorListener((mp, what, extra) -> {
            textStatus.setText("Status: Error — check URL or internet");
            Toast.makeText(this, "Cannot play this video", Toast.LENGTH_LONG).show();
            return true;
        });
    }

    // Handle back arrow in toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // Pause video when app goes to background
    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }
}