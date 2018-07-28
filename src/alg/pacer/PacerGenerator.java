package com.creative.recvdata;

import com.creative.recvdata.PACER;

import java.util.ArrayList;
import java.util.List;

public class PacerGenerator {
    private int BreathingRate =0;
    private List<PACER> Pacerlist = new ArrayList<PACER>();

    /*Input : BreathingRate - the breathing list
    Output: Pacerlist - the pacer partern
        **/
    public PacerGenerator(int BreathingRate, List<PACER> Pacerlist){
        setBreathingRate(BreathingRate);
        setPacerlist(Pacerlist);
    }

    public static void main(String[] args) {
        int br = 6;

        List<PACER> ps = new ArrayList<PACER>();

        PacerGenerator pg = new PacerGenerator(br,ps);
        pg.stepgeneration();

        for(int i =0; i < ps.size(); i++)
            System.out.println(ps.get(i));
    }

    public void stepgeneration(){

        int SumTime = 60*1000/BreathingRate; //Time duration per breath
        int HoldTime = 500; //Holding time
        int BrethTime = SumTime/2-HoldTime; //Breathing time


        int SlowTimeNum = 4;
        int SlowTimeMMsec = 50;
        int SlowTimePer = 2;

        int NormalTimeMMsec = 100;
        int NormalTimeNum = (BrethTime-SlowTimeMMsec*SlowTimeNum*2)/NormalTimeMMsec;
        int NormalTimePer = (100 - SlowTimePer*SlowTimeNum*2)/NormalTimeNum;

        List<PACER> PacerlistUP = new ArrayList<PACER>();
        List<PACER> PacerlistDown = new ArrayList<PACER>();

        for(int i = 0; i < SlowTimeNum; i++) {
            PacerlistUP.add(new PACER(SlowTimeMMsec,SlowTimePer,true));
            PacerlistDown.add(new PACER(NormalTimeMMsec,NormalTimePer,false));
        }
        for(int i = 0; i < NormalTimeNum; i++){
            PacerlistUP.add(new PACER(NormalTimeMMsec,NormalTimePer,true));
            PacerlistDown.add(new PACER(SlowTimeMMsec,SlowTimePer,false));
        }
        for(int i = 0; i < SlowTimeNum; i++) {
            PacerlistUP.add(new PACER(SlowTimeMMsec,SlowTimePer,true));
            PacerlistDown.add(new PACER(SlowTimeMMsec,SlowTimePer,false));
        }

        Pacerlist.addAll(PacerlistUP);
        Pacerlist.addAll(PacerlistDown);
    }

    public void setBreathingRate(int BreathingRate) {
        this.BreathingRate = BreathingRate;;
    }

    public void setPacerlist(List<PACER> Pacerlist) {
        this.Pacerlist = Pacerlist;
    }

}
