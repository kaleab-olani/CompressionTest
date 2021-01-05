package com.app.kol.compressiontest;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.abedelazizshe.lightcompressorlibrary.CompressionListener;
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor;
import com.abedelazizshe.lightcompressorlibrary.VideoQuality;

import net.ypresto.androidtranscoder.MediaTranscoder;
import net.ypresto.androidtranscoder.format.MediaFormatStrategyPresets;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import androidx.appcompat.app.AppCompatActivity;
import pyxis.uzuki.live.mediaresizer.MediaResizer;
import pyxis.uzuki.live.mediaresizer.data.ResizeOption;
import pyxis.uzuki.live.mediaresizer.data.VideoResizeOption;
import pyxis.uzuki.live.mediaresizer.model.MediaType;
import pyxis.uzuki.live.mediaresizer.model.ScanRequest;
import pyxis.uzuki.live.mediaresizer.model.VideoResolutionType;

//import com.iceteck.silicompressorr.SiliCompressor;

class VideoCompressorWithSili {

    private static final String TAG = VideoCompressorWithSili.class.getName();
    private static final String FILE_PROVIDER_AUTHORITY = "";
    private final Context mContext;

    VideoCompressorWithSili(Context context){
        this.mContext = context;
    }
    /*public Future<String> compressVideo(String videoFile) {
        return Async.submit(() -> {
            String filePath = null;
            try {
                    Log.i("TAG", "call: original ==== "+ new File(videoFile).length());

                if (new File(videoFile).exists()){
                    Log.i(TAG, "call: video exists " + videoFile);
                }else {
                    Log.i(TAG, "call: video does not exist " + videoFile);
                }
                File dir = mContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
//        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) +"/video");
                boolean mkdirs = dir.mkdirs();
//        Log.e(String.format("video path - %d", tempResponse.getId()), tempResponse.getLocalFileResponse());
                @SuppressLint("DefaultLocale")


                File file = File.createTempFile("converted", ".mp4",dir);
                String compressedVideoPath = file.getAbsolutePath();
                Log.i(TAG, "compressVideo: =======" + compressedVideoPath);
                filePath = SiliCompressor.with(mContext).compressVideo(videoFile, compressedVideoPath);
                Log.i("TAG", "call: after compress ==== "+ new File(filePath).length());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return  filePath;
        });
    }*/

    public static void transcodeVideo(AppCompatActivity activity , Uri videoUri, MediaTranscoder.Listener listener){
        final File file;
        try {
            File outputDir = new File(activity.getExternalFilesDir(null), "videos");
            //noinspection ResultOfMethodCallIgnored
            outputDir.mkdir();
            file = File.createTempFile("compressed", ".mp4", outputDir);
        } catch (IOException e) {
            Log.e(TAG, "Failed to create temporary file.", e);
            Toast.makeText(activity, "Failed to create temporary file.", Toast.LENGTH_LONG).show();
            return;
        }
        ContentResolver resolver = activity.getContentResolver();
        final ParcelFileDescriptor parcelFileDescriptor;
        try {
            parcelFileDescriptor = resolver.openFileDescriptor(videoUri, "r");
        } catch (FileNotFoundException e) {
            Toast.makeText(activity, "File not found.", Toast.LENGTH_LONG).show();
            return;
        }
        final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Log.d(TAG, "transcoding into " + file);
        MediaTranscoder.getInstance().transcodeVideo(fileDescriptor, file.getAbsolutePath(),
                MediaFormatStrategyPresets.createAndroid720pStrategy(8000*1000,128 * 1000,1), listener);
    }

    public static void compressWithLight(AppCompatActivity context, String videoFile){
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        boolean mkdirs = dir.mkdirs();
        @SuppressLint("DefaultLocale")
        String compressedVideoPath = new File(dir.getAbsolutePath(), String.format("converted - %d.mp4", System.currentTimeMillis())).getAbsolutePath();
        VideoCompressor.start(videoFile, compressedVideoPath, new CompressionListener() {
            @Override
            public void onStart() {
                // Compression start
            }

            @Override
            public void onSuccess() {
                // On Compression success
//                progress.setText("Done.");
            }

            @Override
            public void onFailure(String failureMessage) {
                // On Failure
                Log.e(TAG, "onFailure: " + failureMessage);
            }

            @Override
            public void onProgress(float v) {
                // Update UI with progress value
                context.runOnUiThread(new Runnable() {
                    public void run() {
//                        progress.setText(String.format("%s%%", v));
                    }
                });
            }

            @Override
            public void onCancelled() {
                // On Cancelled
            }
        }, VideoQuality.MEDIUM, true, true);
    }
    public static void compressWithVideoResize(String videoFile, Context context){
        VideoResizeOption resizeOption = new VideoResizeOption.Builder()
                .setVideoResolutionType(VideoResolutionType.AS480)
                .setVideoBitrate(4_000_000)
                .setAudioBitrate(128 * 1000)
                .setAudioChannel(1)
                .setScanRequest(ScanRequest.TRUE)
                .setVideoResolutionType(VideoResolutionType.AS720)
                .build();
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        boolean mkdirs = dir.mkdirs();
        @SuppressLint("DefaultLocale")
        String compressedVideoPath = new File(dir.getAbsolutePath(), String.format("converted%d.mp4", System.currentTimeMillis())).getAbsolutePath();
        ResizeOption option = new ResizeOption.Builder()
                .setMediaType(MediaType.VIDEO)
                .setVideoResizeOption(resizeOption)
                .setTargetPath(videoFile)
                .setOutputPath(compressedVideoPath)
                .setCallback((code, output) -> {
                    Log.i(TAG, "compressWithVideoResize: code = " + code);
                    Log.i(TAG, "compressWithVideoResize: output = " + output);
                    long compressedLength = new File(compressedVideoPath).length();
                    long inputLength = new File(videoFile).length();

                    Log.i("TAG", "compressVideo: Video Compressed!" + compressedVideoPath + " Size from "+ inputLength + "to" + compressedLength);
//                    tempResponse.setLocalFileResponse(output);
//                    tempResponse.save();
//                    Log.e(String.format("video path after - %d", tempResponse.getId()), tempResponse.getLocalFileResponse());
                }).build();
        MediaResizer.process(option);
    }


    @SuppressLint("NewApi")
    public static String getPath(Context context, Uri uri) throws URISyntaxException {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O){
            File file = new File(uri.getPath());
            final String[] split = file.getPath().split(":");
            return split[1];
        }

        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;

        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{ split[1] };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


}