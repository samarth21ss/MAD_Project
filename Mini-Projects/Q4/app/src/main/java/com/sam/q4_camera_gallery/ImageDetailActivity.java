package com.sam.q4_camera_gallery;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailActivity extends AppCompatActivity {

    private ImageView imageViewFull;
    private TextView textName, textPath, textSize, textDate;
    private ImageButton btnDelete, btnInfo, btnShare;
    private CardView detailsCard;

    // The image file being viewed
    private File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        // Toolbar with back arrow
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Image Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Link views
        imageViewFull = findViewById(R.id.imageViewFull);
        textName = findViewById(R.id.textName);
        textPath = findViewById(R.id.textPath);
        textSize = findViewById(R.id.textSize);
        textDate = findViewById(R.id.textDate);
        btnDelete = findViewById(R.id.btnDelete);
        btnInfo = findViewById(R.id.btnInfo);
        btnShare = findViewById(R.id.btnShare);
        detailsCard = findViewById(R.id.detailsCard);

        // Get image path from intent
        String imagePath = getIntent().getStringExtra("IMAGE_PATH");
        if (imagePath == null) {
            Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        imageFile = new File(imagePath);

        if (!imageFile.exists()) {
            Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display the full image
        imageViewFull.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));

        // Fill in the detail fields
        displayImageDetails();

        // Button Listeners
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());

        btnInfo.setOnClickListener(v -> {
            if (detailsCard.getVisibility() == View.VISIBLE) {
                detailsCard.setVisibility(View.GONE);
            } else {
                detailsCard.setVisibility(View.VISIBLE);
            }
        });

        btnShare.setOnClickListener(v -> shareImage());
    }

    /**
     * Fills in name, path, size, and date taken fields.
     */
    private void displayImageDetails() {
        textName.setText("Name:  " + imageFile.getName());
        textPath.setText("Path:  " + imageFile.getAbsolutePath());

        long sizeBytes = imageFile.length();
        String sizeStr;
        if (sizeBytes >= 1024 * 1024) {
            sizeStr = String.format(Locale.getDefault(), "%.2f MB", sizeBytes / (1024.0 * 1024.0));
        } else {
            sizeStr = String.format(Locale.getDefault(), "%.2f KB", sizeBytes / 1024.0);
        }
        textSize.setText("Size:  " + sizeStr);

        long lastModified = imageFile.lastModified();
        String dateStr = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date(lastModified));
        textDate.setText("Date taken:  " + dateStr);
    }

    private void shareImage() {
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share Image via"));
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete \"" + imageFile.getName() + "\"?\nThis cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteImage() {
        if (imageFile.delete()) {
            Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
