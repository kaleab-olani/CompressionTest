package com.app.kol.compressiontest;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.github.clemp6r.futuroid.Async;
import com.github.clemp6r.futuroid.Future;
//import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.concurrent.Callable;



class VideoCompressorWithSili {

    private final Context mContext;

    VideoCompressorWithSili(Context context){
        this.mContext =context;
    }
    public Future<String> compressVideo(String videoFile, String outputPath) {
        return Async.submit(new Callable<String>() {
            @Override
            public String call() {
                String filePath = null;
//                try {
//                    filePath = SiliCompressor.with(mContext).compressVideo(videoFile, outputPath);
//                } catch (URISyntaxException e) {
//                    e.printStackTrace();
//                }
                return  filePath;
            }
        });
    }
}
public class VideoCompressAsyncTask extends AsyncTask<String, String, String> {

    public VideoCompressAsyncTask(String name) {
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//            imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_photo_camera_white_48px));

//        compressionMsg.setVisibility(View.VISIBLE);
//        picDescription.setVisibility(View.GONE);
    }

    @Override
    protected String doInBackground(String... paths) {
        String filePath = null;
//        try {
//
//            filePath = SiliCompressor.with(mContext).compressVideo(paths[0], paths[1]);
//
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
        return  filePath;

    }


    @Override
    protected void onPostExecute(String compressedFilePath) {
        super.onPostExecute(compressedFilePath);
        File imageFile = new File(compressedFilePath);
        float length = imageFile.length() / 1024f; // Size in KB
        String value;
        if(length >= 1024)
            value = length/1024f+" MB";
        else
            value = length+" KB";
//        String text = String.format(Locale.US, "%s\nName: %s\nSize: %s", getString(R.string.video_compression_complete), imageFile.getName(), value);
//        compressionMsg.setVisibility(View.GONE);
//        picDescription.setVisibility(View.VISIBLE);
//        picDescription.setText(text);
        Log.i("Silicompressor", "Path: "+compressedFilePath);
    }
}