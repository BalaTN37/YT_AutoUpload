package com.example.java_ffmpeg_08_jan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;

public class GridViewFilesSelected extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view_files_selected);
        GridView gridView;
        gridView = findViewById(R.id.grid_view);

        Intent intent = getIntent();
        ArrayList<Uri> selectedVideoUriList = intent.getParcelableArrayListExtra("selected_video_uri_list");

        gridView.setAdapter(new ImageAdapter(this, selectedVideoUriList));
        //gridView.setAdapter(new GridViewFilesSelected().ImageAdapter(this, selectedVideoUriList));

    }


    private class ImageAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<Uri> imageUris;

        public ImageAdapter(Context context, ArrayList<Uri> imageUris) {
            this.context = context;
            this.imageUris = imageUris;
        }

        @Override
        public int getCount() {
            return imageUris.size();
        }

        @Override
        public Object getItem(int position) {
            return imageUris.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageURI(imageUris.get(position));
            return imageView;
        }
    }
}
