package com.example.cameragallery;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        ImageView imagePreview = findViewById(R.id.imagePreview);
        TextView textName = findViewById(R.id.textName);
        TextView textPath = findViewById(R.id.textPath);
        TextView textSize = findViewById(R.id.textSize);
        TextView textDate = findViewById(R.id.textDate);

        String uriString = getIntent().getStringExtra(MainActivity.EXTRA_IMAGE_URI);
        if (uriString == null || uriString.isEmpty()) {
            Toast.makeText(this, "Invalid image data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Uri imageUri = Uri.parse(uriString);
        imagePreview.setImageURI(imageUri);

        DocumentFile imageFile = DocumentFile.fromSingleUri(this, imageUri);
        if (imageFile == null) {
            textName.setText("Name: Unknown");
            textPath.setText("Path: " + imageUri);
            textSize.setText("Size: Unknown");
            textDate.setText("Date: Unknown");
            return;
        }

        String name = imageFile.getName() != null ? imageFile.getName() : "Unknown";
        textName.setText("Name: " + name);
        textPath.setText("Path: " + imageUri);
        textSize.setText("Size: " + formatSize(imageFile.length()));
        textDate.setText("Date: " + formatDate(imageFile.lastModified()));
    }

    private String formatSize(long bytes) {
        if (bytes <= 0) {
            return "Unknown";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format(Locale.US, "%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        return String.format(Locale.US, "%.2f MB", mb);
    }

    private String formatDate(long lastModified) {
        if (lastModified <= 0) {
            return "Unknown";
        }
        return DateFormat.getDateTimeInstance().format(new Date(lastModified));
    }
}
