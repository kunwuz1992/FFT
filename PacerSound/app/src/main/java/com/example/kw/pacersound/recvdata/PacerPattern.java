package com.example.kw.pacersound.recvdata;

import com.example.kw.pacersound.alg.wav.AudioPlayer;

import java.util.ArrayList;
import java.util.List;

public class PacerPattern {
    // current used pacepatterused>=0, <0 turn off pacer
    public static int PACEPATTERNUSED = -1;
    // current used pacesoundused >=0, <0 turn off pacer
    public static int PACERSOUNDUSED = -1;

    /**
     * Save wavedata for drawing the rectangle, list for drawing spo2 rect
     */
    public static List<PACERS> PACEPATTERN = new ArrayList<PACERS>();
    // wav bytes data
    private static List<byte[]> INHAUSTWAV = new ArrayList<>();
    private static List<byte[]> EXHAUSTWAV = new ArrayList<>();

    private boolean SoundOn = false;

    public static class PACERS
    {
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        private int id=0;

        public List<PACER> getPacers() {
            return pacers;
        }

        public void setPacers(List<PACER> pacers) {
            this.pacers = pacers;
        }

        private List<PACER> pacers;
        public PACERS(int pacerId,List<PACER> pacers)
        {
            setId(pacerId);
            setPacers(pacers);
        }
    }
    public void AddPacers(int pacerid, List<PACER> pacers) {
        PACERS ps= new PACERS(pacerid,pacers);
        this.PACEPATTERN.add(ps);
    }

    /*-----------------------------------------------------*/
    public void setINHAUSTWAV (List<byte[]> _in){
        if(INHAUSTWAV !=null) INHAUSTWAV.clear();
        INHAUSTWAV.addAll(_in);
    }
    public void setExHAUSTWAV (List<byte[]> _en){
        if(EXHAUSTWAV != null) EXHAUSTWAV.clear();
        EXHAUSTWAV.addAll(_en);
    }

    public List<byte[]> getInWav() {
        return INHAUSTWAV;
    }

    public List<byte[]> getExWav() {
        return EXHAUSTWAV;
    }

    public void Patternclear(){
        PACEPATTERNUSED = -1;
        PACERSOUNDUSED = -1;
        PACEPATTERN.clear();
        INHAUSTWAV.clear();
        EXHAUSTWAV.clear();
    }
}
