package com.app.kol.compressiontest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.abedelazizshe.lightcompressorlibrary.CompressionListener;
//import com.abedelazizshe.lightcompressorlibrary.VideoCompressor;
//import com.abedelazizshe.lightcompressorlibrary.VideoQuality;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_TAKE_CAMERA_PHOTO = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 1;
    private static final int REQUEST_TAKE_VIDEO = 200;
    private static final int TYPE_VIDEO = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;

    String mCurrentPhotoPath;
    Uri capturedUri = null;
    Uri compressUri = null;
    ImageView imageView;
    TextView picDescription;
    MaterialButton selectFile, startCompress;
    LinearLayout compressionMsg;
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
        startCompress.setOnClickListener(view -> compress(capturedUri.toString(), compressUri.toString()));
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
            dispatchTakeVideoIntent();
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
                    return;
                }
                else{
                    Toast.makeText(this, "You need enable the permission for External Storage read" +
                            " to test out this library.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            default:
        }
    }

    private File createMediaFile(int type) throws IOException {

        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String timeStamp = SimpleDateFormat.getDateInstance().format(new Date());
        String fileName = type == 1 ? "JPEG_" + timeStamp + "_" : "VID_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                type == 1 ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_MOVIES);
        File file = File.createTempFile(
                fileName,  /* prefix */
                type == 1 ? ".jpg" : ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + file.getAbsolutePath();
        Log.d(TAG, "mCurrentPhotoPath: " + mCurrentPhotoPath);
        return file;
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            try {

                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                capturedUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", createMediaFile(TYPE_VIDEO));

                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedUri);
                Log.d(TAG, "VideoUri: "  + capturedUri.toString());
                startActivityForResult(takeVideoIntent, REQUEST_TAKE_VIDEO);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    // Method which will process the captured image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_VIDEO && resultCode == RESULT_OK){
            if (data.getData() != null) {
                Log.i(TAG, "onActivityResult: " + data.getData().toString());
                fileName.setText(data.getData().toString());
                capturedUri = data.getData();
                //create destination directory
                File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() );//+ "/" + getPackageName() + "/media/videos"
                if (f.mkdirs() || f.isDirectory()){
                    compressUri = Uri.fromFile(f);
                    //compress and output new video specs
//                    new VideoCompressAsyncTask(this).execute(data.getData().toString(), f.getPath());

//                    VideoCompressorWithSili compressor = new VideoCompressorWithSili(this.getApplicationContext());
//                    compressor.compressVideo(data.getData().toString(),f.getAbsolutePath()).addCallback(new FutureCallback<String>() {
//                        @Override
//                        public void onSuccess(String compressedFile) {
//                            // display the image
//                            Log.i(TAG, "onSuccess: " + compressedFile);
//
//                        }
//
//                        @Override
//                        public void onFailure(Throwable t) {
//                            Log.e(TAG, "Unable to download image", t);
//                        }
//                    });
                }
            }
        }
    }
    void compress(String videoFile, String destFile){
//        VideoCompressor.start(videoFile, destFile, new CompressionListener() {
//            @Override
//            public void onStart() {
//                // Compression start
//            }
//
//            @Override
//            public void onSuccess() {
//                // On Compression success
//                progress.setText("Done.");
//            }
//
//            @Override
//            public void onFailure(String failureMessage) {
//                // On Failure
//                Log.e(TAG, "onFailure: " + failureMessage);
//            }
//
//            @Override
//            public void onProgress(float v) {
//                // Update UI with progress value
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        progress.setText(String.format("%s%%", v));
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled() {
//                // On Cancelled
//            }
//        }, VideoQuality.MEDIUM, true, true);
        VideoCompressorWithSili.compressWithVideoResize(videoFile);
    }
}