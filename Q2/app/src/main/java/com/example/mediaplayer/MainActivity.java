package com.example.mediaplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Uri selectedAudioUri;
    private MediaPlayer mediaPlayer;
    private boolean audioPrepared;

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

        findViewById(R.id.buttonPlay).setOnClickListener(v -> {
            if (selectedAudioUri == null) {
                Toast.makeText(this, "Please select an audio file first", Toast.LENGTH_SHORT).show();
                return;
            }
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
            Toast.makeText(this, "Could not load audio", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        audioPrepared = false;
        super.onDestroy();
    }
}
