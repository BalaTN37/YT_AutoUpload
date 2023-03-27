package com.example.java_ffmpeg_08_jan;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.java_ffmpeg_08_jan.PathFinder;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private static final int FILE_SELECT_CODE = 0;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    Date dateThreshold = new Date(2023, 1, 20);
    private Uri selectedMediaUri[];
    private Context context;
    ArrayList<Uri> selectedPaths = new ArrayList<>();
    ArrayList<String> realPaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    ImageView thumbnailImageView;
    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = findViewById(R.id.OnClickListener);
        thumbnailImageView = findViewById(R.id.thumbnailImageView);
        gridView = findViewById(R.id.grid_view);
        context = getApplicationContext();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23)
                    getPermission();
                else
                    uploadVideo();
            }
        });
    }

    private void getPermission () {
        String[] params = null;
        String writeExternalStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;

        int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission(this, writeExternalStorage);
        int hasReadExternalStoragePermission = ActivityCompat.checkSelfPermission(this, readExternalStorage);
        List<String> permissions = new ArrayList<String>();

        if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(writeExternalStorage);
        if (hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(readExternalStorage);

        if (!permissions.isEmpty()) {
            params = permissions.toArray(new String[permissions.size()]);
        }
        if (params != null && params.length > 0) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    params,
                    100);
        } else
            uploadVideo();
    }


    /**
     * Handling response for permission request
     */
    @Override
    public void onRequestPermissionsResult( int requestCode,
                                            @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode) {
            case 100: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    uploadVideo();
                }
            }
            break;
            case 200: { //Not Applicable

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // extractAudioVideo();
                }
            }
        }
    }

    private void uploadVideo() {
        try {
            Intent intent = new Intent();
            intent.setType("image/*, video/*");
            //intent.setType("video/*");
            //intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Select Files to Upload"), REQUEST_TAKE_GALLERY_VIDEO);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Log.d("YT_AutoUpload", " - RESULT_OK");
            // Get the Uri of the selected file
            if (data.getClipData() != null) {

                int count = data.getClipData().getItemCount();
                if (count == 1) {
                    selectedPaths.clear();
                    Uri selectedUri = data.getClipData().getItemAt(0).getUri();
                    String selectedPath = selectedUri.toString();
                    Log.d("YT_AutoUpload", "onActivityResult - Single File Selected: " + selectedPath);
                    selectedPaths.add(data.getClipData().getItemAt(0).getUri());
                    selectedMediaUri = new Uri[]{selectedUri};
                } else {
                    selectedPaths.clear();
                    ArrayList<String> paths = new ArrayList<>();
                    Log.d("YT_AutoUpload", "onActivityResult - Path Count" + count);
                    for (int i = 0; i < count; i++) {
                        paths.add(data.getClipData().getItemAt(i).getUri().toString());
                        Uri selectedUri = data.getClipData().getItemAt(i).getUri();
                        selectedPaths.add(data.getClipData().getItemAt(i).getUri());
                        realPaths.add(PathFinder.getPath(context, selectedPaths.get(i)));
                        //selectedMediaUri.add(selectedUri);
                    }
                    Log.d("YT_AutoUpload", "Selected URI : " + selectedPaths);
                    //ArrayList<String> realPaths = PathFinder.getPath(context, selectedPaths.get(0));
                    //String realPaths = PathFinder.getPath(context, selectedPaths.get(0));
                    Log.d("YT_AutoUpload", "Selected Media Paths : " + realPaths);
                    displayThumbnails(selectedPaths);
                    //gridView.setAdapter(new ImageAdapter(this, selectedPaths));
                    // Do something with the paths
                }
                //FFMPEG_MergeExecute();
            } else {
                Log.d("YT_AutoUpload", "onActivityResult - data NULL");
            }
        }
    }

    public void displayThumbnails(ArrayList<Uri> selectedVideoUriList) {

        Intent intent = new Intent(MainActivity.this, GridViewFilesSelected.class);
        intent.putExtra("selected_video_uri_list", selectedVideoUriList);
        startActivity(intent);

        /*
        for (Uri uri : selectedVideoUriList) {
            Log.d("YT_AutoUpload", "displayThumbnails Entered");
            // Get the content resolver
            ContentResolver contentResolver = getContentResolver();

            // Get the MIME type of the media (image/jpeg or video/mp4, etc.)
            String mimeType = contentResolver.getType(uri);

            if (mimeType.startsWith("image")) {
                // If the MIME type starts with "image", it's an image
                Log.d("YT_AutoUpload", "displayThumbnails mimeType : Image");
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
                    thumbnailImageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("YT_AutoUpload", "displayThumbnails mimeType : Image Error Exception");
                }
            } else if (mimeType.startsWith("video")) {
                Log.d("YT_AutoUpload", "displayThumbnails mimeType : Video " + uri.getPath());
                // If the MIME type starts with "video", it's a video
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(uri.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
                Log.d("YT_AutoUpload", "displayThumbnails mimeType : Video debug after bitmap " );
                thumbnailImageView.setImageBitmap(bitmap);
            }
        }*/
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