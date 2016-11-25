package com.novonity.plugin.vr;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.novonity.vr.R;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;
import com.google.vr.sdk.widgets.video.VrVideoView.Options;

public class VideoActivity extends Activity {

    private static final String TAG = VideoActivity.class.getSimpleName();

    private ImageView back_img;
    protected VrVideoView videoWidgetView;
    private boolean isMuted;
    private boolean isPaused = false;

    /**
     * Preserve the video's state when rotating the phone.
     */
    private static final String STATE_IS_PAUSED = "isPaused";
    private static final String STATE_PROGRESS_TIME = "progressTime";
    /**
     * The video duration doesn't need to be preserved, but it is saved in this example. This allows
     * the seekBar to be configured during {@link #onRestoreInstanceState(Bundle)} rather than waiting
     * for the video to be reloaded and analyzed. This avoid UI jank.
     */
    private static final String STATE_VIDEO_DURATION = "videoDuration";

    public static final int LOAD_VIDEO_STATUS_UNKNOWN = 0;
    public static final int LOAD_VIDEO_STATUS_SUCCESS = 1;
    public static final int LOAD_VIDEO_STATUS_ERROR = 2;

    private int loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

    /**
     * Tracks the file to be loaded across the lifetime of this app.
     **/
    private Uri fileUri;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vr_video_main);

        back_img = (ImageView) findViewById(R.id.back_img);
        back_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        videoWidgetView = (VrVideoView) findViewById(R.id.video_view);
        videoWidgetView.setEventListener(new ActivityEventListener());
        loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

        handleIntent(getIntent());
    }

    private void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
        videoWidgetView.setVolume(isMuted ? 0.0f : 1.0f);
    }

    private void handleIntent(Intent intent) {
        String url = intent.getStringExtra("url");
        if (url != null) {
            fileUri = Uri.parse(url);
            Log.d(TAG, "Using file " + fileUri.toString());
        } else {
            fileUri = null;
            Toast.makeText(VideoActivity.this, "视频文件不存在", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String subString = fileUri.toString().substring(0, 4);
                    Log.d(TAG, subString);

                    Options options = new Options();
                    if (subString.equalsIgnoreCase("http")) {
                        options.inputFormat = Options.FORMAT_HLS;
                        options.inputType = Options.TYPE_MONO;
                        videoWidgetView.loadVideo(fileUri, options);
                    } else {
                        options.inputType = Options.TYPE_STEREO_OVER_UNDER;
                        videoWidgetView.loadVideoFromAsset(fileUri.toString(), options);
                    }
                } catch (IOException e) {
                    // An error here is normally due to being unable to locate the file.
                    loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
                    // Since this is a background thread, we need to switch to the main thread to show a toast.
                    videoWidgetView.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VideoActivity.this, "无法打开视频文件", Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                    Log.e(TAG, "Could not open video: " + e);
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(STATE_PROGRESS_TIME, videoWidgetView.getCurrentPosition());
        savedInstanceState.putLong(STATE_VIDEO_DURATION, videoWidgetView.getDuration());
        savedInstanceState.putBoolean(STATE_IS_PAUSED, isPaused);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long progressTime = savedInstanceState.getLong(STATE_PROGRESS_TIME);
        videoWidgetView.seekTo(progressTime);

        isPaused = savedInstanceState.getBoolean(STATE_IS_PAUSED);
        if (isPaused) {
            videoWidgetView.pauseVideo();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the 3D rendering.
        videoWidgetView.resumeRendering();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Prevent the view from rendering continuously when in the background.
        videoWidgetView.pauseRendering();
        // If the video is playing when onPause() is called, the default behavior will be to pause
        // the video and keep it paused when onResume() is called.
        isPaused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void togglePause() {
        if (isPaused) {
            videoWidgetView.playVideo();
        } else {
            videoWidgetView.pauseVideo();
        }
        isPaused = !isPaused;
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrVideoEventListener {
        /**
         * Called by video widget on the UI thread when it's done loading the video.
         */
        @Override
        public void onLoadSuccess() {
            Log.i(TAG, "Sucessfully loaded video " + videoWidgetView.getDuration());
            loadVideoStatus = LOAD_VIDEO_STATUS_SUCCESS;
        }

        /**
         * Called by video widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            // An error here is normally due to being unable to decode the video format.
            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
            Toast.makeText(
                    VideoActivity.this, "加载视频文件错误", Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Error loading video: " + errorMessage);
        }

        @Override
        public void onClick() {
            togglePause();
        }

        /**
         * Make the video play in a loop. This method could also be used to move to the next video in
         * a playlist.
         */
        @Override
        public void onCompletion() {
            videoWidgetView.seekTo(0);
        }
    }
}

