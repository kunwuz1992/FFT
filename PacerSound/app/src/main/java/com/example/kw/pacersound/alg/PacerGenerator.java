package com.example.kw.pacersound.alg;

import com.example.kw.pacersound.recvdata.PACER;

import java.util.ArrayList;
import java.util.List;

public class PacerGenerator {
    private int BreathingRate =0;
    private List<PACER> Pacerlist = new ArrayList<PACER>();
    private int diff;

    /*Input : BreathingRate - the breathing list
    Output: Pacerlist - the pacer partern
    PacerValue = Sum(Time* Per/100)
        **/
    public PacerGenerator(int BreathingRate, List<PACER> Pacerlist){
        setBreathingRate(BreathingRate);
        setPacerlist(Pacerlist);
    }

    public void stepgeneration(){

        int SumTime = 60*1000/BreathingRate; //Time duration per breath
        int HoldTime; //Holding time
        if (BreathingRate <= 8) HoldTime = 500;
        else HoldTime = 200;

        int BrethTime = SumTime/2-HoldTime; //Breathing time

        int MMsec = 100; // pacer interval: 100ms
        int Steps = BrethTime/MMsec;

        List<PACER> PacerlistUP = new ArrayList<PACER>(); // patterns for inhaling
        List<PACER> PacerlistDown = new ArrayList<PACER>(); // patterns for exhaling


        int SlowTimeNum = (int) Math.round(0.1*Steps);
        int SlowTimePer = Math.round(15/SlowTimeNum);

        int NormalTimeNum = Steps - 2*SlowTimeNum;
        int NormalTimePer = (100-2*SlowTimePer*SlowTimeNum)/NormalTimeNum;

        int adj = 0;

        if ((NormalTimePer*NormalTimeNum + 2*SlowTimeNum*SlowTimePer) != 100)
            diff = (NormalTimePer*NormalTimeNum + 2*SlowTimeNum*SlowTimePer)-100;
        if (diff < 0) adj = 1;
        if (diff > 0) adj = -1;

        /*Generate the pacer pattern*/
        for(int i = 0; i < SlowTimeNum; i++) {
            PacerlistUP.add(new PACER(MMsec,SlowTimePer,true));
            PacerlistDown.add(new PACER(MMsec,SlowTimePer,false));
        }

        for(int i = 0; i < NormalTimeNum; i++){
            if (i < Math.abs(diff)){
                PacerlistUP.add(new PACER(MMsec,NormalTimePer+adj,true));
                PacerlistDown.add(new PACER(MMsec,NormalTimePer+adj,false));
            }
            else{
                PacerlistUP.add(new PACER(MMsec,NormalTimePer,true));
                PacerlistDown.add(new PACER(MMsec,NormalTimePer,false));
            }

        }
        for(int i = 0; i < SlowTimeNum; i++) {
            PacerlistUP.add(new PACER(MMsec,SlowTimePer,true));
            PacerlistDown.add(new PACER(MMsec,SlowTimePer,false));
        }

        PacerlistUP.add(new PACER(HoldTime,0,true));
        PacerlistDown.add(new PACER(HoldTime,0,false));

        Pacerlist.addAll(PacerlistUP);
        Pacerlist.addAll(PacerlistDown);
    }

    private void setBreathingRate(int BreathingRate) {
        this.BreathingRate = BreathingRate;;
    }

    private void setPacerlist(List<PACER> Pacerlist) {
        this.Pacerlist = Pacerlist;
    }

}
