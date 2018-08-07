package Test;

import alg.PACER;
import alg.PacerGenerator;

import java.util.ArrayList;
import java.util.List;

public class PacerTest {
    private int BreathingRate =0;
    private List<PACER> Pacerlist = new ArrayList<PACER>();
    private int diff;
    private int PacerVal;
    /*Input : BreathingRate - the breathing list
    Output: Pacerlist - the pacer partern
    PacerValue = Sum(Time* Per/100)
        **/

    public static void main(String[] args) {
        int br = 7;

        List<PACER> ps = new ArrayList<PACER>();
        for(int i =6; i < 15; i++){
            br = i;
            PacerGenerator pg = new PacerGenerator(br,ps);
            pg.stepgeneration();
            System.out.println(pg.PacerVal);}
    }

}
