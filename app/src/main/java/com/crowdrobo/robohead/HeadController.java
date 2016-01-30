package com.crowdrobo.robohead;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.crowdrobo.robohead.coms.WebSocketClient;
import com.crowdrobo.robohead.face.Eye;
import com.crowdrobo.robohead.face.FaceScreen;
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
 * Created by rory on 30/01/16.
 */
public class HeadController implements WebSocketClient.Listener {
    private static final String TAG = "HeadController";
    public static final int RECONNECT_DELAY = 10 * 1000;
    private static final long BLINK_RUNNABLE_DELAY = 3000;
    private final WebSocketClient client;

    private static final String HAPPY = "happy";
    private static final String NEUTRAL = "neutral";
    private static final String UNHAPPY = "unhappy";
    private static final String ANGRY = "angry";
    private static final String SURPRISED = "surprised";
    private static final String MIDDLE_FINGER = "finger";

    private final FaceScreen mFaceScreen;
    private final PixelMatrix mPixelMatrix;

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
    Runnable blinkRunnable = new Runnable() {
        public boolean blinking;
        int frameNumber = 0;
        @Override
        public void run() {

            if(mConnected) {
                if(blinking) {
                    blinking = false;
                    mFaceScreen.getLeftEye().setPixelInfo(Eye.LEFT_EYE_OPEN);
                    mFaceScreen.getRightEye().setPixelInfo(Eye.RIGHT_EYE_OPEN);
                } else {
                    blinking = true;
                    mFaceScreen.getLeftEye().setPixelInfo(Eye.LEFT_EYE_CLOSED);
                    mFaceScreen.getRightEye().setPixelInfo(Eye.RIGHT_EYE_CLOSED);
                }


                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mPixelMatrix.invalidate();
                    }
                });

                handler.postDelayed(blinkRunnable, (long) (50 + Math.random()*700 + (blinking ? 0 : (BLINK_RUNNABLE_DELAY + Math.random() * BLINK_RUNNABLE_DELAY))));
            }
        }
    };

    private Handler handler;

    public HeadController(final PixelMatrix pixelMatrix) {

        mPixelMatrix = pixelMatrix;

        handler = new Handler();

        mFaceScreen = new FaceScreen();

        List<BasicNameValuePair> extraHeaders = Arrays.asList(
                new BasicNameValuePair("Cookie", "session=abcd")
        );



        handler.post(connectRunnable);
    }

    @Override
    public void onConnect() {
        mConnected = true;
        Log.d(TAG, "Connected!");
        handler.postDelayed(blinkRunnable, BLINK_RUNNABLE_DELAY);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                pixelMatrix.setFace(mFaceScreen);
            }
        });
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, String.format("Got string message! %s", message));

        final PixelInterface pixelInterface;
        switch (message) {
            case UNHAPPY:
                pixelInterface = mFaceScreen;
                new UnhappyEmotion().applyEmotion(mFaceScreen);
                break;
            case ANGRY:
                pixelInterface = mFaceScreen;
                new AngryEmotion().applyEmotion(mFaceScreen);
                break;
            case SURPRISED:
                pixelInterface = mFaceScreen;
                new SurprisedEmotion().applyEmotion(mFaceScreen);
                break;
            case MIDDLE_FINGER:
                pixelInterface = new MiddleFingerScreen();
                break;
            case HAPPY:
                pixelInterface = mFaceScreen;
                new HappyEmotion().applyEmotion(mFaceScreen);
                break;
            case NEUTRAL:
            default:
                pixelInterface = mFaceScreen;
                new NeutralEmotion().applyEmotion(mFaceScreen);
                break;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mPixelMatrix.setFace(pixelInterface);
            }
        });
    }

    @Override
    public void onMessage(byte[] data) {
//                Log.d(TAG, "We got a binary message, assuming an image");
//                final int[][] array = new int[16][16];
//                int index = 0;
//                for (int i = 0; i < data.length; i++) {
//                    for (int j = 7; j >= 0; j--) {
//                        int x = index / 16;
//                        int y = index % 16;
//                        index++;
//                        array[x][y] = (data[i] >> j) & 1;
//                    }
//                }
//
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        pixelMatrix.setFace(array);
//                    }
//                });
    }

    @Override
    public void onDisconnect(int code, String reason) {
        mConnected = false;
        handler.postDelayed(connectRunnable, RECONNECT_DELAY);
        Log.d(TAG, String.format("Disconnected! Code: %d Reason: %s", code, reason));
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mPixelMatrix.setFace(new NoConnectionScreen());
            }
        });
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "Error!", error);
    }
}
