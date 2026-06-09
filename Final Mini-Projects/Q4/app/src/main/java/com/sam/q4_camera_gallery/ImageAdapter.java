package com.sam.q4_camera_gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context context;
    private List<File> imageFiles;
    private OnImageClickListener listener;
    private OnImageLongClickListener longListener;

    public interface OnImageClickListener {
        void onImageClick(File file);
    }

    public interface OnImageLongClickListener {
        void onImageLongClick(File file);
    }

    public ImageAdapter(Context context,
                        List<File> imageFiles,
                        OnImageClickListener listener,
                        OnImageLongClickListener longListener) {
        this.context    = context;
        this.imageFiles = imageFiles;
        this.listener   = listener;
        this.longListener = longListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        File file = imageFiles.get(position);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        if (bitmap != null) {
            holder.imageView.setImageBitmap(bitmap);
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(file);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longListener != null) {
                longListener.onImageLongClick(file);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return imageFiles != null ? imageFiles.size() : 0;
    }

    public void updateList(List<File> newList) {
        this.imageFiles = newList;
        notifyDataSetChanged();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewThumb);
        }
    }
}
