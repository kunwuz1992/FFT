package com.example.kw.pacersound.alg;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.kw.pacersound.alg.wav.WavFileReader;



public class PacerSoundGenerator extends Activity{

    private List Pacerlist = new ArrayList<>();
    private int WaveRate;
    private Context context;

    private WavFileReader mWavFileReader;

    private String DEFAULT_TEST_FILE = "test.wav";
    private static final int SAMPLES_PER_FRAME = 1024;



    public PacerSoundGenerator(Context context, int waveRate)
    {
        setWaveRate(waveRate);
        setPacerlist(Pacerlist);
        setContext(context);
//        setPacerlist(Pacerlist);
    }

    public void StartTest(){
        mWavFileReader = new WavFileReader();
        try {
            mWavFileReader.openFile(context.getAssets().open(DEFAULT_TEST_FILE));
            Log.d("Read test:","Start reading data");
            byte[] buffer = new byte[2*SAMPLES_PER_FRAME];
            mWavFileReader.readData(buffer, 0, buffer.length);
            System.out.print(buffer.length);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SoundPartternGen(int factor){
    }

    private void setWaveRate(int WaveRate) {
        this.WaveRate = WaveRate;;
    }

    private void setPacerlist(List Pacerlist) {
        this.Pacerlist = Pacerlist;
    }

    private void setContext(Context context) {
        this.context = context;
    }

}
