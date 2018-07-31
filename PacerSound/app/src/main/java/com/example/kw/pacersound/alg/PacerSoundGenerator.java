package com.example.kw.pacersound.alg;

import java.util.ArrayList;
import java.util.List;

public class PacerSoundGenerator {

    private List Pacerlist = new ArrayList<>();
    private int WaveRate;


    public PacerSoundGenerator(int waveRate, List Pacerlist)
    {
        setWaveRate(waveRate);
        setPacerlist(Pacerlist);

    }

    public void SoundPartternGen(int factor){


    }

    private void setWaveRate(int WaveRate) {
        this.WaveRate = WaveRate;;
    }

    private void setPacerlist(List Pacerlist) {
        this.Pacerlist = Pacerlist;
    }
}
