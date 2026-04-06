package com.sam.q4_camera_gallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI buttons
    private Button btnTakePhoto;
    private Button btnChooseFolder;

    // The folder where photos will be saved
    private File saveFolder;

    // Full path of the photo currently being taken
    private String currentPhotoPath;

    // Uri of the photo file passed to camera
    private Uri photoUri;

    // Launcher for camera intent
    private ActivityResultLauncher<Uri> cameraLauncher;

    // Launcher for folder picker (document tree)
    private ActivityResultLauncher<Uri> folderPickerLauncher;

    // Permission request codes
    private static final int PERM_CAMERA    = 101;
    private static final int PERM_STORAGE   = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Camera & Gallery");
        }

        btnTakePhoto    = findViewById(R.id.btnTakePhoto);
        btnChooseFolder = findViewById(R.id.btnChooseFolder);

        // Default save folder = Pictures/CameraApp on device
        saveFolder = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "CameraApp");

        // Create folder if it does not exist
        if (!saveFolder.exists()) {
            saveFolder.mkdirs();
        }

        // --- Camera launcher ---
        // Launched with a Uri; returns true if photo was taken
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success) {
                        // Photo saved — notify gallery so it appears in phone gallery too
                        Intent mediaScan = new Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, photoUri);
                        sendBroadcast(mediaScan);
                        Toast.makeText(this,
                                "Photo saved to: " + currentPhotoPath,
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Photo was not taken", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // --- Folder picker launcher ---
        folderPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                uri -> {
                    if (uri != null) {
                        // Convert the chosen folder Uri to a real file path
                        String path = getRealPathFromUri(uri);
                        if (path != null) {
                            // Open GalleryActivity with this folder path
                            Intent intent = new Intent(this, GalleryActivity.class);
                            intent.putExtra("FOLDER_PATH", path);
                            startActivity(intent);
                        } else {
                            // Fallback: pass the uri as string
                            Intent intent = new Intent(this, GalleryActivity.class);
                            intent.putExtra("FOLDER_URI", uri.toString());
                            startActivity(intent);
                        }
                    }
                }
        );

        // --- Take Photo button ---
        btnTakePhoto.setOnClickListener(v -> {
            if (hasCameraPermission()) {
                launchCamera();
            } else {
                requestCameraPermission();
            }
        });

        // --- Choose Folder button ---
        btnChooseFolder.setOnClickListener(v -> {
            if (hasStoragePermission()) {
                folderPickerLauncher.launch(null);
            } else {
                requestStoragePermission();
            }
        });
    }

    /**
     * Creates a new image file in the save folder and launches camera.
     */
    private void launchCamera() {
        try {
            // Create a unique filename using timestamp
            String timeStamp = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName  = "IMG_" + timeStamp + ".jpg";
            File photoFile   = new File(saveFolder, fileName);
            currentPhotoPath = photoFile.getAbsolutePath();

            // Get a content Uri via FileProvider (required for Android 7+)
            photoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );

            // Launch camera with this Uri as destination
            cameraLauncher.launch(photoUri);

        } catch (Exception e) {
            Toast.makeText(this,
                    "Error creating file: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Converts a document tree Uri to a real file system path.
     */
    private String getRealPathFromUri(Uri uri) {
        try {
            String uriString = uri.toString();
            // Document tree URIs look like:
            // content://com.android.externalstorage.documents/tree/primary:DCIM
            if (uriString.contains("/tree/primary:")) {
                String folderName = uriString.split("/tree/primary:")[1];
                folderName = Uri.decode(folderName);
                File f = new File(
                        Environment.getExternalStorageDirectory(), folderName);
                if (f.exists()) return f.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ---- Permission helpers ----

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                PERM_CAMERA
        );
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    PERM_STORAGE
            );
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    PERM_STORAGE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PERM_CAMERA) {
                launchCamera();
            } else if (requestCode == PERM_STORAGE) {
                folderPickerLauncher.launch(null);
            }
        } else {
            Toast.makeText(this,
                    "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}