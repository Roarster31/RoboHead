package com.crowdrobo.robohead;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by rory on 30/01/16.
 */
public class SpeechCapture {

    private static final String TAG = "SpeechCapture";

    public interface SpeechCallback {
        void onTextRecognised(String text);
    }

    public byte[] buffer;
    public static DatagramSocket socket;
    private int port=50005;

    AudioRecord recorder;

    private int sampleRate = 16000 ; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private boolean status = true;

    public SpeechCapture (final Context context, final SpeechCallback callback) {
        Thread streamThread = new Thread(new Runnable() {

        @Override
        public void run() {
            try {


                byte[] buffer = new byte[minBufSize];

                Log.d("VS","Buffer created of size " + minBufSize);
                DatagramPacket packet;


                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);
                Log.d("VS", "Recorder initialized");

                recorder.startRecording();


                while(status == true) {


                    //reading data from MIC into buffer
                    minBufSize = recorder.read(buffer, 0, buffer.length);

                    //putting buffer in the packet
                    packet = new DatagramPacket (buffer,buffer.length,destination,port);

                    socket.send(packet);
                    System.out.println("MinBufferSize: " +minBufSize);


                }



            } catch(UnknownHostException e) {
                Log.e("VS", "UnknownHostException");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("VS", "IOException");
            }
        }

    });
    streamThread.start();

    }

    private void processMatches(ArrayList<String> voiceResults, SpeechCallback callback) {
        String longest = null;
        for (String match : voiceResults) {
            if(longest == null || longest.length() < match.length()) {
                longest = match;
            }
        }
        callback.onTextRecognised(longest);
    }
}
