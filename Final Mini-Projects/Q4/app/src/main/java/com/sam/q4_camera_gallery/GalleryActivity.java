package com.sam.q4_camera_gallery;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private TextView textEmpty;

    private final List<File> imageFiles = new ArrayList<>();
    private String folderPath;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarGallery);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Gallery");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView    = findViewById(R.id.recyclerViewImages);
        textEmpty       = findViewById(R.id.textEmpty);
        TextView textFolderPath = findViewById(R.id.textFolderPath);

        folderPath = getIntent().getStringExtra("FOLDER_PATH");
        String folderUri = getIntent().getStringExtra("FOLDER_URI");

        if (folderPath != null) {
            textFolderPath.setText("Folder: " + folderPath);
            loadImagesFromFolder(folderPath);
        } else if (folderUri != null) {
            textFolderPath.setText("Folder: " + Uri.decode(folderUri));
            showEmpty();
        } else {
            showEmpty();
        }

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // Initializing adapter
        imageAdapter = new ImageAdapter(
                this,
                imageFiles,
                file -> {
                    Intent intent = new Intent(this, ImageDetailActivity.class);
                    intent.putExtra("IMAGE_PATH", file.getAbsolutePath());
                    startActivity(intent);
                },
                this::showDeleteConfirmationDialog
        );

        recyclerView.setAdapter(imageAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (folderPath != null) {
            loadImagesFromFolder(folderPath);
            if (imageAdapter != null) {
                imageAdapter.updateList(new ArrayList<>(imageFiles));
            }
        }
    }

    private void loadImagesFromFolder(String path) {
        imageFiles.clear();
        File folder = new File(path);

        if (!folder.exists() || !folder.isDirectory()) {
            showEmpty();
            return;
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (isImageFile(file)) {
                    imageFiles.add(file);
                }
            }
        }

        if (imageFiles.isEmpty()) {
            showEmpty();
        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showDeleteConfirmationDialog(File file) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete \"" + file.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (file.delete()) {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                        loadImagesFromFolder(folderPath);
                        imageAdapter.updateList(new ArrayList<>(imageFiles));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp");
    }

    private void showEmpty() {
        textEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
