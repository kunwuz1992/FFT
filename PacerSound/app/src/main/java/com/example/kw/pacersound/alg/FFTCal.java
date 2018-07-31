package com.example.kw.pacersound.alg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FFTCal {

    //[max_min_HR aHR ULF VLF LF_peak LF HF_peak HF pLF pHF ratio_LFHF CR]
    /**
     * Ouput
            - Time domain
                -max_min_HR: average difference between the highest HR and the lowest HR in each respiratory circle during at least 2-min time duration
                -aHR: average heart rate
            - Frequency domain
                -ULF: absolute power of the ultra-low-frequency band (<=0.003 Hz)
                -VLF: absolute power of the very-low-frequency band (0.003~0.04 Hz)
                -LF:  absolute power of the low-frequency band (0.04~0.15 Hz)
                -LF_peak: peak frequency of the low-frequency band (0.04~0.15 Hz)
                -HF: absolute power of the high-frequency band
                -HF_peak: peak frequency of the high-frequency band (0.15~0.4 Hz)
                -pLF: relative power of LF power, suggested to calculate by (LF power) / (VFL power + HF power)
                -pHF: relative power of HF power, suggested to calculate by (HF power) / (VFL power + LF power)
                -ratio_LFHF: ratio of LF power to HF power
                -CR: the power in (0.015 ~ peak frequency Hz) to the power in (0.003 ~ 0.4 Hz)
        - ver001 created on Jul. 12, 2018
     */

    private ArrayList<Double> index;
    private ArrayList<Double> bpm;
    private int Devicefs;
    private int Resamplefs;
    private boolean isresample;


    public FFTCal(ArrayList<Double> bpm, ArrayList index, int dfs, int rfs, boolean isresample){
        this.bpm = bpm;
        this.index = index;
        this.Devicefs = dfs;
        this.Resamplefs = rfs;
        this.isresample = isresample;
    }

    private static void FFT(int n, List<Double> x, List<Double> y) {

        int m = (int)(Math.log(n) / Math.log(2));

        // Make sure n is a power of 2
        if(n != (1<<m))
            throw new RuntimeException("FFT length must be power of 2");
        List<Double> cos = new ArrayList<Double>(n/2);
        List<Double> sin = new ArrayList<Double>(n/2);

        // precompute tables
        for(int i=0; i<n/2; i++) {
            cos.add(Math.cos(-2* Math.PI*i/n));
            sin.add(Math.sin(-2* Math.PI*i/n));
        }

        int i,j,k,n1,n2,a;
        double c,s,t1,t2;


        // Bit-reverse
        j = 0;
        n2 = n/2;
        for (i=1; i < n - 1; i++) {
            n1 = n2;
            while ( j >= n1 ) {
                j = j - n1;
                n1 = n1/2;
            }
            j = j + n1;

            if (i < j) {
                t1 = x.get(i);
                x.set(i,x.get(j));
                x.set(j,t1);
                t1 = y.get(i);
                y.set(i,y.get(j));
                y.set(j,t1);
            }
        }

        // FFT
        n1 = 0;
        n2 = 1;

        for (i=0; i < m; i++) {
            n1 = n2;
            n2 = n2 + n2;
            a = 0;

            for (j=0; j < n1; j++) {
                c = cos.get(a);
                s = sin.get(a);
                a +=  1 << (m-i-1);

                for (k=j; k < n; k=k+n2) {
                    t1 = c*x.get(k+n1) - s*y.get(k+n1);
                    t2 = s*x.get(k+n1) + c*y.get(k+n1);
                    x.set(k + n1, x.get(k) - t1);
                    y.set(k + n1, y.get(k) - t2);
                    x.set(k, x.get(k) + t1);
                    y.set(k, y.get(k) + t2);
                }
            }
        }
    }

    /*transfer the time domain signal to frequency domain*/
    private static void fillspectrum(ArrayList<Double> bpm, int fs, ArrayList<Double> power, ArrayList<Double> frequency){

        int N_fft = (int) Math.pow(2,nextPowerOf2(bpm.size()));
        List<Double> re = new ArrayList<Double>(N_fft);
        List<Double> im = new ArrayList<Double>(N_fft);

        for(int i = 0; i < N_fft; i++)
        {
            if(i < bpm.size()){
                re.add(60/bpm.get(i));
                im.add(0.0);
            } else if (i>=bpm.size()){
                im.add(0.0);
                re.add(0.0);
            }
        }

        FFT(N_fft,re,im);

        for(int i = 0; i < N_fft/2+1; i++){
            power.add(2*(Math.pow(re.get(i)/bpm.size(), 2)+ Math.pow(im.get(i)/bpm.size(), 2)));
            frequency.add(0.5*fs*i/N_fft*2);
        }
    }

    /**/
    private static int nextPowerOf2(final int a)
    {
        int b = 1;
        int i = 0;
        while (b < a)
        {
            b = b << 1;
            i = i + 1;
        }
        return i;
    }

    private static double SumArray(List<Double> InputArray)
    {
        double sum = 0;
        for(double x:InputArray) sum = sum + x;
        return sum;
    }

    public void indexcal(){

        double cr_cutoff = 0.26;

        double max_hr = 0.0;
        double min_hr = 99999.0;
        double sum = 0;
        double max_min_HR = 0;
        double aHR = 0;

        double ULF = 0;
        double VLF = 0;
        double LF = 0;
        double LF_peak = 0;
        double HF = 0;
        double HF_peak = 0;
        double pLF = 0;
        double pHF = 0;
        double ratio_LFHF = 0;
        int fs = 0;
        double CR = 0;


        if(isresample){
            interpolation(bpm,Resamplefs, Devicefs);
            fs = Resamplefs;
        } else{
            fs = Devicefs;
        }
        /* calculate the time domain index */
        for(double x:bpm) {
            if(x > max_hr) max_hr = x;
            if(x < min_hr) min_hr = x;
            sum = sum + x;
        }

        max_min_HR = max_hr-min_hr;
        aHR = sum/bpm.size();

        /* calculate the frequency domain index */
        int N_fft = (int) Math.pow(2,nextPowerOf2(bpm.size()));
        ArrayList<Double> power = new ArrayList<>(N_fft/2+1);
        ArrayList<Double> frequency = new ArrayList<>(N_fft/2+1);

        /* trasfer the time domian data to the frequency domian */
        fillspectrum(bpm,fs,power,frequency);

        /* find the position of the frequency index*/
        double[] FrequencyIdx = {0.003,0.0033,0.015,0.04,0.15,cr_cutoff,0.4};
//        List<Integer> Idxnum = new ArrayList<Integer>();

        int[] IdxNum = new int[FrequencyIdx.length];
        for(int i = 0; i<FrequencyIdx.length; i++) IdxNum[i]=(int)(FrequencyIdx[i]*N_fft/fs);

        /* Calculate ULF,VLF,LF and HF*/
        ULF = SumArray(power.subList(0, IdxNum[0]+1));
        VLF = SumArray(power.subList(IdxNum[0]+1, IdxNum[3]+1));

        int LFPeakNum = IdxNum[3]+1;
        for (int i = IdxNum[3]+1; i <= IdxNum[4]; i++) {
            if(power.get(i) < power.get(LFPeakNum)) LFPeakNum = i;
            LF = LF + power.get(i);
        }
        LF_peak = frequency.get(LFPeakNum);

        int HFPeakNum = IdxNum[4]+1;
        for (int i = IdxNum[4]+1; i <= IdxNum[6]; i++) {
            if(power.get(i) > power.get(HFPeakNum)) HFPeakNum =i;
            HF = HF + power.get(i);
        }
        HF_peak = frequency.get(HFPeakNum);

        pLF = LF/(VLF+HF)*100;
        pHF = HF/(VLF+LF)*100;
        ratio_LFHF = LF/HF*100;

        /* Calculate Coherence related*/
        int fPeakNum = IdxNum[3]+1;
        for (int i = IdxNum[3]+1; i <= IdxNum[5]; i++) {
            if(power.get(i) > power.get(fPeakNum)) fPeakNum =i;
        }
        double peakpower = SumArray(power.subList( IdxNum[2]+1, fPeakNum));
        double mTpL = SumArray(power.subList( 0, IdxNum[1]+1));
        double mTpH = SumArray(power.subList( 0, IdxNum[6]+1));
        double mTp = mTpH - mTpL;
        CR = 100*peakpower/ Math.pow(mTp-peakpower, 1);

//        pLF = Math.round(pLF);
//        pHF = Math.round(pHF);
//        CR = Math.round(CR);
//
//        ratio_LFHF = Math.round(ratio_LFHF*100)/100;
        double pLF1 = Math.round(100*LF/(LF+HF));
        double pLF2 = Math.round(100*LF/(LF+VLF+HF));
//        CR = Math.round(CR);
        Double[] tmp = {max_min_HR, aHR, ULF, VLF, LF_peak, LF, HF_peak, HF, pLF, pHF, ratio_LFHF, CR};
//        if(index.size()>=12) index.clear();
//        Double[] tmp = {max_min_HR, pLF1, pLF2, CR};
        index.addAll(Arrays.asList(tmp));
    }

    private void interpolation(ArrayList<Double> bpmlist, int resamplefs, int devicefs){
        /* resample the bpm signal with the frequency refs*/
        int newfs = lcm(resamplefs, devicefs);
        int mul = newfs/devicefs;

        double dif = 0;

        ArrayList<Double> newbpm = new ArrayList<Double>();

        for (int i = 0; i <bpmlist.size()-1; i ++){
            dif = bpmlist.get(i+1) - bpmlist.get(i);
            for (int j =0; j < mul; j++) newbpm.add(bpmlist.get(i)+dif*j/mul);
        }
        newbpm.add(bpmlist.get(bpmlist.size()-1));

        bpm.clear();
        bpm.addAll(newbpm);
    }

    private static int gcf(int a, int b)
    {
        while (a != b) // while the two numbers are not equal...
        {
            // ...subtract the smaller one from the larger one
            if (a > b) a -= b; // if a is larger than b, subtract b from a
            else b -= a; // if b is larger than a, subtract a from b
        }
        return a; // or return b, a will be equal to b either way
    }

    private static int lcm(int a, int b)
    {
        // the lcm is simply (a * b) divided by the gcf of the two
        return (a * b) / gcf(a, b);
    }

}