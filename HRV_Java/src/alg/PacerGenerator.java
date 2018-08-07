package alg;

import java.util.ArrayList;
import java.util.List;

import alg.PACER;

public class PacerGenerator {
    private int BreathingRate =0;
    private List<PACER> Pacerlist = new ArrayList<PACER>();
    private int diff;
    public int PacerVal;
    /*Input : BreathingRate - the breathing list
    Output: Pacerlist - the pacer partern
    PacerValue = Sum(Time* Per/100)
        **/
    public PacerGenerator(int BreathingRate, List<PACER> Pacerlist){
        setBreathingRate(BreathingRate);
        setPacerlist(Pacerlist);
        this.PacerVal = 0;
    }

    public static void main(String[] args) {
        int br = 7;

        List<PACER> ps = new ArrayList<PACER>();
        for(int i =6; i < 15; i++){
            br = i;
            PacerGenerator pg = new PacerGenerator(br,ps);
            pg.stepgeneration();
            System.out.println(pg.PacerVal);}
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


        int SlowTimeNum = (int)Math.round(0.1*Steps);
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
            PacerVal = PacerVal + MMsec*SlowTimePer/100;
        }

        for(int i = 0; i < NormalTimeNum; i++){
            if (i < Math.abs(diff)){
                PacerlistUP.add(new PACER(MMsec,NormalTimePer+adj,true));
                PacerlistDown.add(new PACER(MMsec,NormalTimePer+adj,false));
                PacerVal = PacerVal + MMsec*(NormalTimePer+adj)/100;
            }
            else{
                PacerlistUP.add(new PACER(MMsec,NormalTimePer,true));
                PacerlistDown.add(new PACER(MMsec,NormalTimePer,false));
                PacerVal = PacerVal + MMsec*(NormalTimePer)/100;
            }

        }
        for(int i = 0; i < SlowTimeNum; i++) {
            PacerlistUP.add(new PACER(MMsec,SlowTimePer,true));
            PacerlistDown.add(new PACER(MMsec,SlowTimePer,false));
            PacerVal = PacerVal + MMsec*SlowTimePer/100;
        }

        PacerlistUP.add(new PACER(HoldTime,0,true));
        PacerlistDown.add(new PACER(HoldTime,0,false));

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
