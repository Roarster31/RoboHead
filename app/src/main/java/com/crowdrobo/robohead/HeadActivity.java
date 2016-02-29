package com.crowdrobo.robohead;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.crowdrobo.robohead.coms.WebSocketClient;
import com.crowdrobo.robohead.face.MiddleFingerScreen;
import com.crowdrobo.robohead.face.NoConnectionScreen;
import com.crowdrobo.robohead.face.emotion.AngryEmotion;
import com.crowdrobo.robohead.face.emotion.HappyEmotion;
import com.crowdrobo.robohead.face.emotion.NeutralEmotion;
import com.crowdrobo.robohead.face.emotion.SurprisedEmotion;
import com.crowdrobo.robohead.face.emotion.UnhappyEmotion;

import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class HeadActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    public static final int RECONNECT_DELAY = 10 * 1000;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    public static final String DEV_SERVER = "http://10.1.53.195:3110";
    public static final String PROD_SERVER = "http://7639d0fc.ngrok.io";
    private final Handler mHideHandler = new Handler();
    private View mFace;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mFace.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private TextView mTextView;

    private boolean mConnected;

    Runnable connectRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mConnected) {
                client.connect();

                handler.postDelayed(connectRunnable, RECONNECT_DELAY);
            }
        }
    };
    private WebSocketClient client;
    private Handler handler;
    private HeadController headController;
    private SpeechCapture speechCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_head);

        mVisible = true;
        mFace = findViewById(R.id.face);

        mTextView = (TextView) findViewById(R.id.textView);
        // Set up the user interaction to manually show or hide the system UI.
        mFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        List<BasicNameValuePair> extraHeaders = Arrays.asList(
                new BasicNameValuePair("Cookie", "session=abcd")
        );


        handler = new Handler();
        client = new WebSocketClient(URI.create(DEV_SERVER), new WebSocketClient.Listener() {

            @Override
            public void onConnect() {
                mConnected = true;
                headController.onConnect();
                speechCapture.onConnect();
            }

            @Override
            public void onMessage(String message) {
                headController.onMessage(message);
                speechCapture.onMessage(message);
            }

            @Override
            public void onMessage(byte[] data) {
                headController.onMessage(data);
                speechCapture.onMessage(data);
            }

            @Override
            public void onDisconnect(int code, String reason) {
                mConnected = false;
                handler.postDelayed(connectRunnable, RECONNECT_DELAY);
                headController.onDisconnect(code, reason);
                speechCapture.onDisconnect(code,reason);
            }

            @Override
            public void onError(Exception error) {
                headController.onError(error);
                speechCapture.onError(error);
            }
        }, extraHeaders);

        handler.post(connectRunnable);

        headController = new HeadController(this, (PixelMatrix) mFace);
        speechCapture = new SpeechCapture(client);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mFace.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
