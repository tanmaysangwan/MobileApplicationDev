package com.example.mediaplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

    private static final int SEEK_SKIP_MS = 10_000;

    private static final int VIDEO_PENDING_NONE = 0;
    private static final int VIDEO_PENDING_PLAY = 1;
    private static final int VIDEO_PENDING_RESTART = 2;

    private enum MediaKind {
        NONE,
        AUDIO,
        VIDEO
    }

    private MediaKind activeMedia = MediaKind.NONE;

    private Uri selectedAudioUri;
    private MediaPlayer mediaPlayer;
    private boolean audioPrepared;

    private VideoView videoView;
    private Uri currentVideoUri;
    private boolean videoPrepared;
    private boolean videoNeedsReload;
    private int videoPendingAction = VIDEO_PENDING_NONE;

    private final ActivityResultLauncher<Intent> pickAudioLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }
                Uri uri = result.getData().getData();
                if (uri != null) {
                    activeMedia = MediaKind.AUDIO;
                    selectedAudioUri = uri;
                    if (mediaPlayer != null) {
                        mediaPlayer.reset();
                    }
                    audioPrepared = false;
                    clearVideoState();
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

        videoView.setOnPreparedListener(mp -> {
            Log.i(TAG, "Video prepared");
            videoPrepared = true;
            if (videoPendingAction == VIDEO_PENDING_PLAY) {
                videoPendingAction = VIDEO_PENDING_NONE;
                pauseAudioIfPlaying();
                videoView.start();
            } else if (videoPendingAction == VIDEO_PENDING_RESTART) {
                videoPendingAction = VIDEO_PENDING_NONE;
                pauseAudioIfPlaying();
                videoView.seekTo(0);
                videoView.start();
            }
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            String message = "Video error what=" + what + " extra=" + extra;
            Log.e(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            videoPrepared = false;
            videoNeedsReload = true;
            videoPendingAction = VIDEO_PENDING_NONE;
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

            activeMedia = MediaKind.VIDEO;
            pauseAudioIfPlaying();
            currentVideoUri = videoUri;
            videoNeedsReload = false;
            videoPendingAction = VIDEO_PENDING_NONE;
            videoPrepared = false;

            videoView.setVisibility(View.VISIBLE);
            videoView.stopPlayback();
            videoView.setVideoURI(videoUri);
            videoView.requestFocus();
        });

        findViewById(R.id.buttonPlay).setOnClickListener(v -> handlePlay());
        findViewById(R.id.buttonPause).setOnClickListener(v -> handlePause());
        findViewById(R.id.buttonStop).setOnClickListener(v -> handleStop());
        findViewById(R.id.buttonRestart).setOnClickListener(v -> handleRestart());
        findViewById(R.id.buttonSeekBack).setOnClickListener(v -> handleSeekRelative(-SEEK_SKIP_MS));
        findViewById(R.id.buttonSeekForward).setOnClickListener(v -> handleSeekRelative(SEEK_SKIP_MS));
        findViewById(R.id.buttonClear).setOnClickListener(v -> handleClear());
    }

    private void clearVideoState() {
        if (videoView != null) {
            videoView.stopPlayback();
        }
        currentVideoUri = null;
        videoPrepared = false;
        videoNeedsReload = false;
        videoPendingAction = VIDEO_PENDING_NONE;
    }

    private void handleClear() {
        activeMedia = MediaKind.NONE;
        selectedAudioUri = null;
        currentVideoUri = null;
        audioPrepared = false;
        videoPrepared = false;
        videoNeedsReload = false;
        videoPendingAction = VIDEO_PENDING_NONE;

        if (mediaPlayer != null) {
            try {
                mediaPlayer.reset();
            } catch (IllegalStateException ignored) {
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (videoView != null) {
            videoView.stopPlayback();
            videoView.setVisibility(View.INVISIBLE);
        }
    }

    private void handleSeekRelative(int deltaMs) {
        if (activeMedia == MediaKind.NONE) {
            toastSelectMedia();
            return;
        }
        if (activeMedia == MediaKind.AUDIO) {
            seekAudio(deltaMs);
            return;
        }
        seekVideo(deltaMs);
    }

    private void seekAudio(int deltaMs) {
        if (!audioPrepared || mediaPlayer == null) {
            Toast.makeText(this, "Media not ready to seek", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int current = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();
            int target = current + deltaMs;
            target = Math.max(0, target);
            if (duration > 0) {
                target = Math.min(target, duration);
            }
            mediaPlayer.seekTo(target);
        } catch (IllegalStateException e) {
            Log.w(TAG, "Audio seek failed", e);
            Toast.makeText(this, "Cannot seek audio now", Toast.LENGTH_SHORT).show();
        }
    }

    private void seekVideo(int deltaMs) {
        if (currentVideoUri == null || !videoPrepared || videoView == null) {
            Toast.makeText(this, "Media not ready to seek", Toast.LENGTH_SHORT).show();
            return;
        }
        int current = videoView.getCurrentPosition();
        int duration = videoView.getDuration();
        int target = current + deltaMs;
        target = Math.max(0, target);
        if (duration > 0) {
            target = Math.min(target, duration);
        }
        videoView.seekTo(target);
    }

    private void handlePlay() {
        if (activeMedia == MediaKind.NONE) {
            toastSelectMedia();
            return;
        }
        if (activeMedia == MediaKind.AUDIO) {
            if (selectedAudioUri == null) {
                toastSelectMedia();
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
            return;
        }
        if (currentVideoUri == null) {
            toastSelectMedia();
            return;
        }
        pauseAudioIfPlaying();
        if (videoNeedsReload) {
            videoView.setVideoURI(currentVideoUri);
            videoView.requestFocus();
            videoNeedsReload = false;
            videoPrepared = false;
            videoPendingAction = VIDEO_PENDING_PLAY;
            return;
        }
        if (!videoPrepared) {
            videoPendingAction = VIDEO_PENDING_PLAY;
            return;
        }
        pauseAudioIfPlaying();
        videoView.start();
    }

    private void handlePause() {
        if (activeMedia == MediaKind.NONE) {
            toastSelectMedia();
            return;
        }
        if (activeMedia == MediaKind.AUDIO) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            return;
        }
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    private void handleStop() {
        if (activeMedia == MediaKind.NONE) {
            toastSelectMedia();
            return;
        }
        if (activeMedia == MediaKind.AUDIO) {
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
            return;
        }
        videoPendingAction = VIDEO_PENDING_NONE;
        videoView.stopPlayback();
        videoPrepared = false;
        videoNeedsReload = true;
    }

    private void handleRestart() {
        if (activeMedia == MediaKind.NONE) {
            toastSelectMedia();
            return;
        }
        if (activeMedia == MediaKind.AUDIO) {
            if (selectedAudioUri == null) {
                toastSelectMedia();
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
            return;
        }
        if (currentVideoUri == null) {
            toastSelectMedia();
            return;
        }
        pauseAudioIfPlaying();
        if (videoNeedsReload || !videoPrepared) {
            videoView.setVideoURI(currentVideoUri);
            videoView.requestFocus();
            videoNeedsReload = false;
            videoPrepared = false;
            videoPendingAction = VIDEO_PENDING_RESTART;
            return;
        }
        videoView.seekTo(0);
        videoView.start();
    }

    private void toastSelectMedia() {
        Toast.makeText(this, "Select audio or video first", Toast.LENGTH_SHORT).show();
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
