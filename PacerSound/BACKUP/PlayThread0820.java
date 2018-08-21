package com.example.kw.pacersound.alg;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.example.kw.pacersound.alg.wav.AudioPlayer;

import java.util.List;

public class PlayThread extends Thread {

    private AudioPlayer mAudioPlayer;
    protected boolean stop = false;
    protected boolean pause = false;

    private final int PLAYSTATE_PAUSED = 2;
    private final int PLAYSTATE_PLAYING = 3;
    private final int PLAYSTATE_STOPPED = 1;
    private int PLATER_STATE;
    private int SoundIndex;
    private List<byte[]> InWav;
    private List<byte[]> ExWav;

    private int pacerState;
    private int pacerValue;

    private Looper mLooper;
    private Handler mHandler;

    public PlayThread(AudioPlayer _player){
        this.mAudioPlayer = _player;
        PlayerInit();
    }


    private void PlayerInit(){
        if(mAudioPlayer == null)
            mAudioPlayer = new AudioPlayer();
        mAudioPlayer.startPlayer();
    }

    private void PlayerPause(){
        if(PLATER_STATE == PLAYSTATE_PLAYING)
            mAudioPlayer.pausePlayer();
        PLATER_STATE = PLAYSTATE_PAUSED;
    }

    private void PlayerStop(){
        if(mAudioPlayer != null)
            mAudioPlayer.stopPlayer();
        PLATER_STATE = PLAYSTATE_STOPPED;
    }

    private void PlayerPlay(byte[] Wavbytes){
        mAudioPlayer.play(Wavbytes, 0, Wavbytes.length);
        PLATER_STATE = PLAYSTATE_PLAYING;
    }

    private int getEndPoint(int value, int state){
        int end = 0;
        if(state == 1){
            if(InWav.size()*value%100 == 0)
                end = InWav.size()*value/100;
            else
                end = Math.min(1+InWav.size()*value/100,InWav.size());
        } else if(state == 3){
            if(ExWav.size()*value%100 == 0)
                end = ExWav.size()*value/100;
            else
                end = Math.max(-1+ExWav.size()*value/100,0);
        }
        return end;
    }

    public void playBuffer(){
        int end;
        if(pacerState == 1){
            end =  getEndPoint(pacerValue,pacerState);
            if(SoundIndex != end){
                for(int i = SoundIndex; i < end; i++){
                    byte[] buffer = InWav.get(i);
                    PlayerPlay(buffer);
                }
            }
            SoundIndex = end;
        } else if(pacerState == 3) {
            end =  getEndPoint(pacerValue,pacerState);
            if(SoundIndex != end){
                for(int i = SoundIndex; i > end; i--){
                    byte[] buffer = ExWav.get(ExWav.size()-i);
                    PlayerPlay(buffer);
                }
            }
            SoundIndex = end;
        } else{
//                PlayerPause();
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        synchronized (this) {
            mLooper = Looper.myLooper();
        }
        Looper.loop();
    }

    public void setInWav(List<byte[]> _in){
        InWav = _in;
    }

    public void setExWav(List<byte[]> _ex){
        ExWav = _ex;
    }

    public void Stop() {
        this.stop = true;
    }

    public void Pause() {
        this.pause = true;
    }

    public boolean isPause() {
        return this.pause;
    }

    public boolean isStop() {
        return this.stop;
    }

    public synchronized void Continue() {
        this.pause = false;
        this.notify();
    }

    public void updatePlayer(int _state, int _value){
        pacerState = _state;
        pacerValue = _value;
    }
    public void setPlayerState(int _state){
        PLATER_STATE = _state;
    }

    public Looper getmLooper(){
        return mLooper;
    }

    public void setSoundIndex(int value, int state){
        int end =  getEndPoint(value,state);
        SoundIndex = end;
    }

}
