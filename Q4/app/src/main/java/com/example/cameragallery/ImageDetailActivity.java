package com.example.cameragallery;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailActivity extends AppCompatActivity {

    private String imageName = "Unknown";
    private String imagePath = "Unknown";
    private String imageSize = "Unknown";
    private String imageDate = "Unknown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        ImageView imagePreview = findViewById(R.id.imagePreview);
        findViewById(R.id.buttonDetails).setOnClickListener(v -> showDetailsDialog());
        findViewById(R.id.buttonDelete).setOnClickListener(
                v -> Toast.makeText(this, "Delete feature coming soon", Toast.LENGTH_SHORT).show()
        );

        String uriString = getIntent().getStringExtra(MainActivity.EXTRA_IMAGE_URI);
        if (uriString == null || uriString.isEmpty()) {
            Toast.makeText(this, "Invalid image data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Uri imageUri = Uri.parse(uriString);
        imagePreview.setImageURI(imageUri);
        imagePath = imageUri.toString();

        DocumentFile imageFile = DocumentFile.fromSingleUri(this, imageUri);
        if (imageFile == null) {
            return;
        }

        imageName = imageFile.getName() != null ? imageFile.getName() : "Unknown";
        imageSize = formatSize(imageFile.length());
        imageDate = formatDate(imageFile.lastModified());
    }

    private void showDetailsDialog() {
        String message = "Name: " + imageName
                + "\n\nPath: " + imagePath
                + "\n\nSize: " + imageSize
                + "\n\nDate: " + imageDate;

        new AlertDialog.Builder(this)
                .setTitle("Image Details")
                .setMessage(message)
                .setPositiveButton("Close", null)
                .show();
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
