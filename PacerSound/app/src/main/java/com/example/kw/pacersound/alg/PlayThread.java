package com.example.kw.pacersound.alg;

import android.os.Looper;

import com.example.kw.pacersound.alg.wav.AudioPlayer;

import java.util.ArrayList;
import java.util.List;

public class PlayThread extends Thread {

    private AudioPlayer mAudioPlayer;
    protected boolean stop = false;
    protected boolean pause = true;

    private final int PLAYSTATE_PAUSED = 2;
    private final int PLAYSTATE_PLAYING = 3;
    private final int PLAYSTATE_STOPPED = 1;
    private int PLATER_STATE;
    private List<byte[]> InWav = new ArrayList<>();
    private List<byte[]> ExWav = new ArrayList<>();

    private int pacerState;
    private int pacerValue;



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

    @Override
    public void run() {
        synchronized (this) {
            mAudioPlayer.startPlayer();
            int itr = 0;
            while (!stop){
                if(!pause){
                    switch (pacerState){
                        case 1:
                            if(itr<InWav.size()){
                                byte[] buffer = InWav.get(itr);
                                PlayerPlay(buffer);
                                itr ++;
                            }
                            break;
                        case 2:
                            itr = 0;
                            break;
                        case 3:
                            if(itr<ExWav.size()){
                                byte[] buffer = ExWav.get(itr);
                                PlayerPlay(buffer);
                                itr ++;
                            }
                            break;
                        default:
                            itr = 0;
                    }
                }
            }
        }
    }

    public void setWav(List<byte[]> _in, List<byte[]> _ex){
        if(InWav !=null){
            this.ExWav.clear();
            this.InWav.clear();
        }
        this.ExWav.addAll(_ex);
        this.InWav.addAll(_in);
    }

    public void Stop() {
        this.stop = true;
    }

    public void Pause() {
        this.pause = true;
        PlayerPause();
    }

    public void setPause(boolean _pause) {
        this.pause = _pause;
        if(_pause)
            this.PLATER_STATE = PLAYSTATE_PAUSED;
        else
            this.PLATER_STATE = PLAYSTATE_PLAYING;
    }

    public boolean isPause() {
        return this.pause;
    }

    public boolean isStop() {
        return this.stop;
    }

    public void Continue() {
        this.pause = false;
    }

    public void updatePlayer(int _state, int _value){
        pacerState = _state;
        pacerValue = _value;
    }

}
