package com.sam.q2_audio_video;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.media.MediaPlayer;

public class MainActivity extends AppCompatActivity {

    private Button btnPlay;
    private Button btnPause;
    private Button btnStop;
    private Button btnRestart;
    private TextView textStatus, textFileName;
    private SeekBar seekBar;

    // --- MediaPlayer for audio ---
    private MediaPlayer mediaPlayer;

    // --- Handler to update seekbar periodically ---
    private final Handler handler = new Handler();

    // --- Currently loaded audio URI ---
    private Uri audioUri = null;

    // --- Flag: is audio loaded and ready ---
    private boolean isAudioReady = false;

    // --- File picker launcher ---
    private ActivityResultLauncher<String> filePickerLauncher;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Media Player");
        }

        // Link views
        // --- UI elements ---
        Button btnOpenFile = findViewById(R.id.btnOpenFile);
        Button btnOpenUrl = findViewById(R.id.btnOpenUrl);
        btnPlay      = findViewById(R.id.btnPlay);
        btnPause     = findViewById(R.id.btnPause);
        btnStop      = findViewById(R.id.btnStop);
        btnRestart   = findViewById(R.id.btnRestart);
        textStatus   = findViewById(R.id.textStatus);
        textFileName = findViewById(R.id.textFileName);
        seekBar      = findViewById(R.id.seekBar);

        // Disable controls until a file is loaded
        setControlsEnabled(false);

        // Register file picker — opens audio files from device storage
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        audioUri = uri;
                        loadAudio(uri);
                    }
                }
        );

        // --- Button: Open audio file from disk ---
        btnOpenFile.setOnClickListener(v -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_MEDIA_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 1);
                } else {
                    filePickerLauncher.launch("audio/*");
                }
            } else {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    filePickerLauncher.launch("audio/*");
                }
            }
        });

        // --- Button: Open video URL in VideoActivity ---
        btnOpenUrl.setOnClickListener(v -> {
            // Open a dialog to enter URL, then launch VideoActivity
            showUrlDialog();
        });

        // --- Button: Play ---
        btnPlay.setOnClickListener(v -> {
            if (mediaPlayer != null && isAudioReady && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                updateSeekBar();
                textStatus.setText("Status: Playing");
            }
        });

        // --- Button: Pause ---
        btnPause.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                textStatus.setText("Status: Paused");
            }
        });

        // --- Button: Stop ---
        btnStop.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                isAudioReady = false;
                seekBar.setProgress(0);
                textStatus.setText("Status: Stopped");
                // Reload the file so it can be played again
                if (audioUri != null) {
                    loadAudio(audioUri);
                }
            }
        });

        // --- Button: Restart ---
        btnRestart.setOnClickListener(v -> {
            if (mediaPlayer != null && isAudioReady) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                updateSeekBar();
                textStatus.setText("Status: Restarted");
            }
        });

        // --- SeekBar: allow manual scrubbing ---
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null && isAudioReady) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    /**
     * Loads an audio file from the given URI into MediaPlayer.
     */
    @SuppressLint("SetTextI18n")
    private void loadAudio(Uri uri) {
        try {
            // Release any existing player
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            isAudioReady = false;
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getApplicationContext(), uri);

            // Prepare asynchronously so UI doesn't freeze
            mediaPlayer.prepareAsync();
            textStatus.setText("Status: Loading...");
            textFileName.setText("File: " + uri.getLastPathSegment());

            // Called when audio is ready to play
            mediaPlayer.setOnPreparedListener(mp -> {
                isAudioReady = true;
                seekBar.setMax(mp.getDuration());
                setControlsEnabled(true);
                textStatus.setText("Status: Ready");
                Toast.makeText(this, "Audio loaded!", Toast.LENGTH_SHORT).show();
            });

            // Called when audio finishes playing
            mediaPlayer.setOnCompletionListener(mp -> {
                textStatus.setText("Status: Completed");
                seekBar.setProgress(0);
            });

            // Called on error
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                textStatus.setText("Status: Error loading file");
                Toast.makeText(this, "Error loading audio file", Toast.LENGTH_SHORT).show();
                return true;
            });

        } catch (Exception e) {
            Toast.makeText(this, "Failed to load: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Updates the seekbar every 500ms while audio is playing.
     */
    private void updateSeekBar() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        if (mediaPlayer.isPlaying()) {
            handler.postDelayed(this::updateSeekBar, 500);
        }
    }

    /**
     * Shows a simple dialog to enter a video URL.
     */
    @SuppressLint("SetTextI18n")
    private void showUrlDialog() {
        // Use an EditText inside an AlertDialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Enter Video URL");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("https://...");
        input.setPadding(40, 20, 40, 20);
        // Pre-fill with a sample working video URL for testing
        input.setText("https://www.w3schools.com/html/mov_bbb.mp4");
        builder.setView(input);

        builder.setPositiveButton("Open", (dialog, which) -> {
            String url = input.getText().toString().trim();
            if (!url.isEmpty()) {
                // Launch VideoActivity with the URL
                Intent intent = new Intent(this, VideoActivity.class);
                intent.putExtra("VIDEO_URL", url);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * Enable or disable the playback control buttons.
     */
    private void setControlsEnabled(boolean enabled) {
        btnPlay.setEnabled(enabled);
        btnPause.setEnabled(enabled);
        btnStop.setEnabled(enabled);
        btnRestart.setEnabled(enabled);
        seekBar.setEnabled(enabled);
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            filePickerLauncher.launch("audio/*");
        } else {
            Toast.makeText(this,
                    "Permission denied — cannot access audio files",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Release MediaPlayer when activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
}