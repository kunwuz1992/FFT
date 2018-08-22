/*
 *  COPYRIGHT NOTICE  
 *  Copyright (C) 2016, Jhuster <lujun.hust@gmail.com>
 *  https://github.com/Jhuster/AudioDemo
 *   
 *  @license under the Apache License, Version 2.0 
 *
 *  @file    AudioPlayer.java
 *  
 *  @version 1.0     
 *  @author  Jhuster
 *  @date    2016/03/19
 */
package com.example.kw.pacersound.alg.wav;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioPlayer {

    private static final String TAG = "AudioPlayer";

    private static final int DEFAULT_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private static final int DEFAULT_SAMPLE_RATE = 44100;
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_STEREO;
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int DEFAULT_PLAY_MODE = AudioTrack.MODE_STREAM;

    private volatile boolean mIsPlayStarted = false;
    private AudioTrack mAudioTrack;
    private int bufferSize;

    public boolean startPlayer() {
        return startPlayer(DEFAULT_STREAM_TYPE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
    }

    public boolean startPlayer(int streamType, int sampleRateInHz, int channelConfig, int audioFormat) {
        if (mIsPlayStarted) {
            Log.e(TAG, "Player already started !");
            return false;
        }

        int bufferSizeInBytes = 2*AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (bufferSizeInBytes == AudioTrack.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
            return false;
        }
        Log.i(TAG, "getMinBufferSize = " + bufferSizeInBytes + " bytes !");

        mAudioTrack = new AudioTrack(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, DEFAULT_PLAY_MODE);
        bufferSize = bufferSizeInBytes;
        if (mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioTrack initialize fail !");
            return false;
        }


        mIsPlayStarted = true;
        Log.i(TAG, "Start audio player success !");

        return true;
    }

    public void stopPlayer() {
        if (!mIsPlayStarted) {
            return;
        }

        if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            mAudioTrack.stop();
        }

        mAudioTrack.release();
        mIsPlayStarted = false;

        Log.i(TAG, "Stop audio player success !");
    }

    public void pausePlayer(){
        if (!mIsPlayStarted) {
            return;
        }
        if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
            mAudioTrack.pause();
        }
        Log.i(TAG, "Pause audio player success !");
    }


    public int play(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if (!mIsPlayStarted) {
            Log.e(TAG, "Player not started !");
            return 0;
        }
        int WriteBytes;

        mAudioTrack.play();
        WriteBytes = mAudioTrack.write(audioData, offsetInBytes, sizeInBytes);
        if ( WriteBytes!= sizeInBytes) {
            Log.e(TAG, "Could not write all the samples to the audio device !");
        }



        Log.d(TAG, "OK, Played " + sizeInBytes + " bytes !");

        return WriteBytes;
    }

    public int getBufferSize(){
        return bufferSize;
    }
}
