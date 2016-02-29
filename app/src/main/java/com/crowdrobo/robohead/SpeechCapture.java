package com.crowdrobo.robohead;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.crowdrobo.robohead.coms.WebSocketClient;

/**
 * Created by rory on 30/01/16.
 */
public class SpeechCapture implements WebSocketClient.Listener {

    private static final String TAG = "SpeechCapture";
    private final WebSocketClient mClient;

    @Override
    public void onConnect() {
        if(!mConnected) {
            mConnected = true;
            new Thread(audioRunnable).start();
        }
    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onMessage(byte[] data) {

    }

    @Override
    public void onDisconnect(int code, String reason) {
        mConnected = false;
    }

    @Override
    public void onError(Exception error) {

    }

    AudioRecord recorder;

    private int sampleRate = 16000; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private boolean mConnected = false;

    public SpeechCapture(WebSocketClient client) {
        mClient = client;
    }

    Runnable audioRunnable = new Runnable() {

        @Override
        public void run() {


            byte[] buffer = new byte[minBufSize];

            Log.d("VS", "Buffer created of size " + minBufSize);

            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufSize * 10);
            Log.d("VS", "Recorder initialized");

            recorder.startRecording();


            while (mConnected) {

                //reading data from MIC into buffer
                minBufSize = recorder.read(buffer, 0, buffer.length);

                mClient.send(buffer);

            }

            recorder.stop();


        }

    };

}
