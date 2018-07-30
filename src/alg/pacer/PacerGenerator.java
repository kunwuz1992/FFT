package alg.Pacer;

import java.util.ArrayList;
import java.util.List;

import alg.Pacer.PACER;

public class PacerGenerator {
    private int BreathingRate =0;
    private List<PACER> Pacerlist = new ArrayList<PACER>();

    /*Input : BreathingRate - the breathing list
    Output: Pacerlist - the pacer partern
    PacerValue = Sum(Time* Per/100)
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

        int MMsec = 100; // pacer interval: 100ms

        List<PACER> PacerlistUP = new ArrayList<PACER>(); // patterns for inhaling
        List<PACER> PacerlistDown = new ArrayList<PACER>(); // patterns for exhaling


        int SlowTimeNum = 5;
        int SlowTimePer = 3;

        int NormalTimeNum = (BrethTime-2*SlowTimeNum*MMsec)/MMsec;
        int NormalTimePer = 2;

        for(int i = 0; i < SlowTimeNum; i++) {
            PacerlistUP.add(new PACER(MMsec,SlowTimePer,true));
            PacerlistDown.add(new PACER(MMsec,NormalTimePer,false));
        }

        for(int i = 0; i < NormalTimeNum; i++){
            PacerlistUP.add(new PACER(MMsec,NormalTimePer,true));
            PacerlistDown.add(new PACER(MMsec,SlowTimePer,false));
        }

        for(int i = 0; i < SlowTimeNum; i++) {
            PacerlistUP.add(new PACER(MMsec,SlowTimePer,true));
            PacerlistDown.add(new PACER(MMsec,NormalTimePer,false));
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
