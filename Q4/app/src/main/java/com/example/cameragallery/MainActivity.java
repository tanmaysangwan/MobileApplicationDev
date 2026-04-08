package com.example.cameragallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1001;

    private Uri currentPhotoUri;
    private String currentPhotoPath;
    private Uri selectedFolderUri;
    private final List<Uri> imageUris = new ArrayList<>();
    private ImageGridAdapter imageGridAdapter;
    private RecyclerView recyclerViewImages;
    private TextView textEmpty;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && currentPhotoUri != null) {
                    // Photo is saved to currentPhotoPath/currentPhotoUri for later usage.
                    Toast.makeText(this, "Photo saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Photo capture canceled", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> folderPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri treeUri = result.getData().getData();
                    if (treeUri != null) {
                        int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                        selectedFolderUri = treeUri;
                        Toast.makeText(this, "Folder selected", Toast.LENGTH_SHORT).show();
                        loadImagesFromSelectedFolder();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonCamera = findViewById(R.id.buttonCamera);
        Button buttonFolder = findViewById(R.id.buttonFolder);
        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        textEmpty = findViewById(R.id.textEmpty);

        recyclerViewImages.setLayoutManager(new GridLayoutManager(this, 3));
        imageGridAdapter = new ImageGridAdapter(imageUris);
        recyclerViewImages.setAdapter(imageGridAdapter);
        updateEmptyState();

        buttonCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        });

        buttonFolder.setOnClickListener(v -> launchFolderPicker());
    }

    private void loadImagesFromSelectedFolder() {
        imageUris.clear();
        if (selectedFolderUri == null) {
            updateEmptyState();
            imageGridAdapter.notifyDataSetChanged();
            return;
        }

        DocumentFile selectedFolder = DocumentFile.fromTreeUri(this, selectedFolderUri);
        if (selectedFolder == null || !selectedFolder.isDirectory()) {
            updateEmptyState();
            imageGridAdapter.notifyDataSetChanged();
            return;
        }

        for (DocumentFile file : selectedFolder.listFiles()) {
            if (file.isFile() && isImageFile(file)) {
                Uri uri = file.getUri();
                if (uri != null) {
                    imageUris.add(uri);
                }
            }
        }

        imageGridAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private boolean isImageFile(DocumentFile file) {
        String type = file.getType();
        if (type != null && type.startsWith("image/")) {
            return true;
        }

        String name = file.getName();
        if (name == null) {
            return false;
        }

        String lowerName = name.toLowerCase(Locale.US);
        return lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".png")
                || lowerName.endsWith(".webp");
    }

    private void updateEmptyState() {
        if (imageUris.isEmpty()) {
            textEmpty.setVisibility(View.VISIBLE);
        } else {
            textEmpty.setVisibility(View.GONE);
        }
    }

    private void launchFolderPicker() {
        Intent folderIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        folderIntent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        );
        folderPickerLauncher.launch(folderIntent);
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
            return;
        }

        File imageFile;
        try {
            imageFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
            return;
        }

        currentPhotoUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                imageFile
        );

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
        cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cameraLauncher.launch(cameraIntent);
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "IMG_" + timestamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            throw new IOException("External files directory unavailable");
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
        }
    }
}