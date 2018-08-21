package com.example.kw.pacersound.alg;

import android.content.Context;

import com.example.kw.pacersound.R;
import com.example.kw.pacersound.alg.wav.WavGenerator;
import com.example.kw.pacersound.recvdata.PACER;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacerGenerator0816 {
    // Pacer
    private int BreathingRate =0;
    private List<PACER> Pacerlist = new ArrayList<PACER>();
    private int diff;
    private int PacerValue = 0;
    private int BreathTime;
    private int HoldTime;
    // Sound
    private WavGenerator InhaustGenerator;
    private WavGenerator ExhaustGenerator;
    private byte[] InhaustArray;
    private byte[] ExhaustArray;
    private List<byte[]> Wavlist = new ArrayList<>();
    private final int ORIGIN_SOUND_LENGTH = 4;// The length of the source file
    private float rate = 1.0f;
    private int quality = 0;


    /*Input : BreathingRate - the breathing list
    Output: Pacerlist - the pacer partern
    PacerValue = Sum(Time* Per/100)
        **/
    public PacerGenerator0816(Context context , int BreathingRate){
        setBreathingRate(BreathingRate);
        try{
            InputStream InhausStream = context.getResources().openRawResource(R.raw.test);
            InputStream ExhausStream = context.getResources().openRawResource(R.raw.test);
            InhaustGenerator = new WavGenerator(InhausStream);
            ExhaustGenerator = new WavGenerator(ExhausStream);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public void generation(){
        StepTimeInit();
        if (SoundGeneration())
            stepgeneration();
        else
            throw new NullPointerException("Sound generation failed");
    }
    /*--------------Pace pattern--------------------------------------------*/
    private void StepTimeInit(){
        int SumTime = 60*1000/BreathingRate; //Time duration per breath
        if (BreathingRate <= 8)
            HoldTime = 500;
        else
            HoldTime = 200;
        BreathTime = SumTime/2-HoldTime; //Breathing time
    }

    private void stepgeneration(){

        int MMsec = 100; // pacer interval: 100ms
        int Steps = BreathTime/MMsec;

        //Sound buffer length per percentage
        int BufferSize = InhaustArray.length/100;

        List<PACER> PacerlistUP = new ArrayList<PACER>(); // patterns for inhaling
        List<PACER> PacerlistDown = new ArrayList<PACER>(); // patterns for exhaling
        List<byte[]> WavlistUP = new ArrayList<byte[]>(); // patterns for inhaling
        List<byte[]> WavlistDown = new ArrayList<byte[]>(); // patterns for exhaling

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

            byte[] inbuffer = Arrays.copyOfRange(InhaustArray, BufferSize*PacerValue,BufferSize*(PacerValue + SlowTimePer));
            byte[] exbuffer = Arrays.copyOfRange(ExhaustArray, BufferSize*PacerValue,BufferSize*(PacerValue + SlowTimePer));
            WavlistUP.add(inbuffer);
            WavlistDown.add(exbuffer);

            PacerValue = PacerValue + SlowTimePer;
        }

        for(int i = 0; i < NormalTimeNum; i++){
            int per;
            if (i < Math.abs(diff)){
                per = NormalTimePer+adj;
            }
            else{
                per = NormalTimePer;
            }
            PacerlistUP.add(new PACER(MMsec,per,true));
            PacerlistDown.add(new PACER(MMsec,per,false));

            byte[] inbuffer = Arrays.copyOfRange(InhaustArray, BufferSize*PacerValue,BufferSize*(PacerValue + per));
            byte[] exbuffer = Arrays.copyOfRange(ExhaustArray, BufferSize*PacerValue,BufferSize*(PacerValue + per));
            WavlistUP.add(inbuffer);
            WavlistDown.add(exbuffer);

            PacerValue = PacerValue + per;
        }

        for(int i = 0; i < SlowTimeNum; i++) {
            PacerlistUP.add(new PACER(MMsec,SlowTimePer,true));
            PacerlistDown.add(new PACER(MMsec,SlowTimePer,false));

            byte[] inbuffer = Arrays.copyOfRange(InhaustArray, BufferSize*PacerValue,BufferSize*(PacerValue + SlowTimePer));
            byte[] exbuffer = Arrays.copyOfRange(ExhaustArray, BufferSize*PacerValue,BufferSize*(PacerValue + SlowTimePer));
            WavlistUP.add(inbuffer);
            WavlistDown.add(exbuffer);

            PacerValue = PacerValue + SlowTimePer;
        }

        PacerlistUP.add(new PACER(HoldTime,0,true));
        PacerlistDown.add(new PACER(HoldTime,0,false));

        Pacerlist.addAll(PacerlistUP);
        Pacerlist.addAll(PacerlistDown);

        Wavlist.addAll(WavlistUP);
        Wavlist.addAll(WavlistDown);

        if(PacerValue >= 100) PacerValue = 0;
    }

    private void setBreathingRate(int BreathingRate) {
        this.BreathingRate = BreathingRate;
    }

    public List<PACER> getPacerlist() {
        return Pacerlist;
    }

    /*--------------Pace sound--------------------------------------------*/
    private boolean SoundGeneration(){
        float InSpeed = (float)ORIGIN_SOUND_LENGTH*1000/BreathTime;
        float OutSpeed = (float)ORIGIN_SOUND_LENGTH*1000/BreathTime;

        InhaustGenerator.setSonic(InSpeed, rate,quality);
        ExhaustGenerator.setSonic(OutSpeed, rate,quality);

        if(InhaustGenerator.generate() && ExhaustGenerator.generate()){
            InhaustArray = InhaustGenerator.getOutByteArray();
            ExhaustArray = ExhaustGenerator.getOutByteArray();
            return true;
        } else
            return false;
    }

    public void setSoundRate(float _rate){
        this.rate = _rate;
    }
    public void setSoundQuality(int _quality){
        this.quality = _quality;
    }

    public List<byte[]> getWavlist(){
        return Wavlist;
    }

}