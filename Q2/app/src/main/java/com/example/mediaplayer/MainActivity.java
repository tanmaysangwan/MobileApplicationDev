package com.example.mediaplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Uri selectedAudioUri;
    private MediaPlayer mediaPlayer;
    private boolean audioPrepared;

    private VideoView videoView;

    private final ActivityResultLauncher<Intent> pickAudioLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }
                Uri uri = result.getData().getData();
                if (uri != null) {
                    selectedAudioUri = uri;
                    if (mediaPlayer != null) {
                        mediaPlayer.reset();
                    }
                    audioPrepared = false;
                    Toast.makeText(this, "Audio file selected", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.buttonOpenAudio).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            pickAudioLauncher.launch(intent);
        });

        videoView = findViewById(R.id.videoView);
        EditText editTextUrl = findViewById(R.id.editTextUrl);

        MediaController videoMediaController = new MediaController(this);
        videoMediaController.setAnchorView(videoView);
        videoView.setMediaController(videoMediaController);

        videoView.setOnPreparedListener(mp -> {
            Log.i(TAG, "Video prepared");
            Toast.makeText(this, "Video prepared", Toast.LENGTH_SHORT).show();
            pauseAudioIfPlaying();
            videoView.start();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            String message = "Video error what=" + what + " extra=" + extra;
            Log.e(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return true;
        });

        findViewById(R.id.buttonOpenVideo).setOnClickListener(v -> {
            String url = editTextUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "Enter video URL", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!url.matches("(?i)^https?://.*")) {
                url = "https://" + url;
            }
            Uri videoUri = Uri.parse(url);
            Log.i(TAG, "Video URL: " + url);
            Toast.makeText(this, "URL: " + url, Toast.LENGTH_SHORT).show();

            videoView.stopPlayback();
            videoView.setVideoURI(videoUri);
            videoView.requestFocus();
        });

        findViewById(R.id.buttonPlay).setOnClickListener(v -> {
            if (selectedAudioUri == null) {
                Toast.makeText(this, "Please select an audio file first", Toast.LENGTH_SHORT).show();
                return;
            }
            pauseVideoIfPlaying();
            if (!audioPrepared) {
                prepareAudioFromUri();
                if (!audioPrepared) {
                    return;
                }
            }
            mediaPlayer.start();
        });

        findViewById(R.id.buttonPause).setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        });

        findViewById(R.id.buttonStop).setOnClickListener(v -> {
            if (mediaPlayer == null) {
                return;
            }
            if (audioPrepared) {
                try {
                    mediaPlayer.stop();
                } catch (IllegalStateException ignored) {
                }
            }
            mediaPlayer.reset();
            audioPrepared = false;
        });

        findViewById(R.id.buttonRestart).setOnClickListener(v -> {
            if (selectedAudioUri == null) {
                Toast.makeText(this, "Please select an audio file first", Toast.LENGTH_SHORT).show();
                return;
            }
            pauseVideoIfPlaying();
            if (!audioPrepared) {
                prepareAudioFromUri();
                if (!audioPrepared) {
                    return;
                }
            } else {
                mediaPlayer.seekTo(0);
            }
            mediaPlayer.start();
        });
    }

    private void pauseAudioIfPlaying() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void pauseVideoIfPlaying() {
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    private void prepareAudioFromUri() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                mediaPlayer.reset();
            }
            mediaPlayer.setDataSource(this, selectedAudioUri);
            mediaPlayer.prepare();
            audioPrepared = true;
        } catch (IOException e) {
            audioPrepared = false;
            Log.e(TAG, "Audio prepare failed", e);
            Toast.makeText(this, "Could not load audio", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (videoView != null) {
            videoView.stopPlayback();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        audioPrepared = false;
        super.onDestroy();
    }
}
