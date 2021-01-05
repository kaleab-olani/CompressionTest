package com.app.kol.compressiontest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import net.ypresto.androidtranscoder.MediaTranscoder;
import net.ypresto.androidtranscoder.engine.InvalidOutputFormatException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import com.abedelazizshe.lightcompressorlibrary.CompressionListener;
//import com.abedelazizshe.lightcompressorlibrary.VideoCompressor;
//import com.abedelazizshe.lightcompressorlibrary.VideoQuality;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_TAKE_CAMERA_PHOTO = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 1;
    private static final int REQUEST_TAKE_VIDEO = 200;
    private static final int TYPE_VIDEO = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 4;

    String videoPath = "";
    String compressedVideoPath = "";
    Uri capturedUri = null;
    Uri compressUri = null;

    MaterialButton selectFile, startCompress;

    private TextView progress;
    private TextView fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectFile = findViewById(R.id.selectFile);
        selectFile.setOnClickListener(view -> requestPermissions(TYPE_VIDEO));
        progress = findViewById(R.id.progress);
        fileName = findViewById(R.id.fileName);
        startCompress = findViewById(R.id.compress);
        startCompress.setOnClickListener(view -> { compress(videoPath); });
    }

    /**
     * Request Permission for writing to External Storage in 6.0 and up
     */
    private void requestPermissions(int mediaType){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else{
//            dispatchTakeVideoIntent();
            pickFromGallery();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakeVideoIntent();
                }
                else{
                    Toast.makeText(this, "You need enable the permission for External Storage Write" +
                            " to test out this library.", Toast.LENGTH_LONG).show();
                    return;
                }
                break;
            }case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "read permission granted.", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(this, "You need enable the permission for External Storage read" +
                            " to test out this library.", Toast.LENGTH_LONG).show();
                }
            }
            default:
        }
    }

    private File createMediaFile() throws IOException {

        // Create an image file name
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String fileName = "VID_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        return File.createTempFile(fileName, ".mp4", getCacheDir());
    }
    public void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent,"Select Video"),REQUEST_TAKE_GALLERY_VIDEO);
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            try {

                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 25);
                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                File mediaFile = createMediaFile();
                capturedUri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                        BuildConfig.APPLICATION_ID + ".provider", mediaFile);

//                capturedUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() BuildConfig.APPLICATION_ID + ".provider",, mediaFile);
//                Log.i(TAG, "dispatchTakeVideoIntent: file = " + mediaFile.getAbsolutePath());
                Log.i(TAG, "dispatchTakeVideoIntent: provider = " + capturedUri.getPath());

                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedUri.getPath());
                startActivityForResult(takeVideoIntent, REQUEST_TAKE_VIDEO);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method which will process the captured image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_TAKE_VIDEO && resultCode == RESULT_OK){
            Uri data = intent.getData();
            if (data != null) {
//                File cvideo = new File(data.getPath());
                Log.i(TAG, "onActivityResult: " + data.getPath());
                fileName.setText(data.getPath());
                videoPath = data.getPath();
            }
        }else if (requestCode == REQUEST_TAKE_GALLERY_VIDEO && resultCode == Activity.RESULT_OK) {
            capturedUri = intent.getData();
//            videoPath = capturedUri.getPath();
            try {
                videoPath = VideoCompressorWithSili.getPath(this,capturedUri);
                fileName.setText(videoPath);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
    void compress(String videoFile){
        try{
            VideoCompressorWithSili.transcodeVideo(this,capturedUri, getListener());
        }catch (InvalidOutputFormatException e){
            Log.e(TAG, "compress: ", e );
        }

//        VideoCompressorWithSili.compressWithVideoResize(videoFile,this.getApplicationContext());

//        progress.setText("Compressing...");
//        VideoCompressorWithSili compressorWithSili = new VideoCompressorWithSili(this);
//        compressorWithSili.compressVideo(videoFile).addCallback(new FutureCallback<String>() {
//            @Override
//            public void onSuccess(String result) {
//                progress.setText("compressed!");
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                progress.setText("failed");
//                Log.e(TAG, "onFailure: ",t );
//            }
//        });

    }

    private MediaTranscoder.Listener getListener() {
        return new MediaTranscoder.Listener() {
            @Override
            public void onTranscodeProgress(double progressV) {
                progress.setText(String.valueOf(progressV));
            }
            @Override
            public void onTranscodeCompleted() {
                progress.setText("Completed!");
            }
            @Override
            public void onTranscodeCanceled() {

                progress.setText("Canceled!");
            }

            @Override
            public void onTranscodeFailed(Exception exception) {
                progress.setText("Failed!");
                Log.e(TAG, "onTranscodeFailed: ",exception );
            }
        };
    }
}