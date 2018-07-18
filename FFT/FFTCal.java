import static java.lang.Math.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

public class FFTCal {
    
    private double[] index;
    private double[] bpm;

    public FFTCal(double[] bpm, double[] index){
        this.bpm = bpm;
        this.index = index;
    }

    //  // Test the FFT to make sure it's working
    public static void main(String[] args) {
    // int N = (int)Math.pow(2, 8);
        int N = 50*60*2;
        int N_fft = (int)Math.pow(2,nextPowerOf2(N));
        double fs = 50; 

        double[] bpm = new double[N];
        double[] index = new double[12];
        
        double[] re = new double[N_fft];
        double[] im = new double[N_fft];

        double[] power = new double[N_fft/2+1];
        double[] frequency = new double[N_fft/2+1];

        try{
            Scanner scanner = new Scanner(new File("test1_hrv_data.csv"));
            for(int i=0; i<N; i++)
            {
            bpm[i] = Double.parseDouble(scanner.next());
            re[i] = 60/bpm[i];
            im[i] = 0;
            }
            scanner.close();
        } catch (FileNotFoundException e){
            System.out.print("File not found");
        }   

        fillspectrum(bpm, fs, power, frequency);
        
        FFTCal fftcal = new FFTCal(bpm,index);
        
        fftcal.indexcal(index);
        System.out.println("index:");
        for(int i = 0; i<12; i++) System.out.println(index[i]);
    }

    private static void FFT(int n, double[] x, double[] y) {

        int m = (int)(Math.log(n) / Math.log(2));

        double[] cos;
        double[] sin;

        // Make sure n is a power of 2
        if(n != (1<<m))
            throw new RuntimeException("FFT length must be power of 2");

        // precompute tables
        cos = new double[n/2];
        sin = new double[n/2];

        for(int i=0; i<n/2; i++) {
            cos[i] = Math.cos(-2*Math.PI*i/n);
            sin[i] = Math.sin(-2*Math.PI*i/n);
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
                t1 = x[i];
                x[i] = x[j];
                x[j] = t1;
                t1 = y[i];
                y[i] = y[j];
                y[j] = t1;
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
                c = cos[a];
                s = sin[a];
                a +=  1 << (m-i-1);

                for (k=j; k < n; k=k+n2) {
                    t1 = c*x[k+n1] - s*y[k+n1];
                    t2 = s*x[k+n1] + c*y[k+n1];
                    x[k+n1] = x[k] - t1;
                    y[k+n1] = y[k] - t2;
                    x[k] = x[k] + t1;
                    y[k] = y[k] + t2;
                }
            }
        }
    }

    /*transfer the time domain signal to frequency domain*/
    private static void fillspectrum(double[] bpm, double fs, double[] power, double[] frequency){
        
        int N_fft = (int)Math.pow(2,nextPowerOf2(bpm.length));
        double[] re = new double[N_fft];
        double[] im = new double[N_fft];
        for(int i = 0; i < N_fft; i++)
        {
            if(i<bpm.length){
                re[i] = 60/bpm[i];
                im[i] = 0;     
            } else if (i>=bpm.length){
                re[i] = 0;
                im[i] = 0;  
            }
        }

        FFT(N_fft,re,im);

        for(int i = 0; i < N_fft/2+1; i++){
            power[i] = 2*(Math.pow(re[i]/bpm.length, 2)+Math.pow(im[i]/bpm.length, 2));
            frequency[i] = 0.5*fs*i/N_fft*2;
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

    public void indexcal(double[] PI){

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

        double CR = 0;
        double mLFpeak = 0;

        /* calculate the time domain index */
        for(int i = 0; i<bpm.length;i++){
            if(bpm[i]>max_hr) max_hr=bpm[i];
            if(bpm[i]<min_hr) min_hr=bpm[i];
            sum = sum + bpm[i];
        }
        max_min_HR = max_hr-min_hr;
        aHR = (double)sum/(double)bpm.length;

        /* calculate the frequency domain index */
        int N_fft = (int)Math.pow(2,nextPowerOf2(bpm.length));
        double[] power = new double[N_fft/2+1];
        double[] frequency = new double[N_fft/2+1];
        double fs = 50;

        /* trasfer the time domian data to the frequency domian */
        fillspectrum(bpm,fs,power,frequency);

        /* find the position of the frequency index*/
        double[] FrequencyIdx = {0.003,0.0033,0.015,0.04,0.15,cr_cutoff,0.4};
        int[] IdxNum = new int[FrequencyIdx.length];  
        for(int i = 0; i<FrequencyIdx.length; i++) IdxNum[i]=(int)(FrequencyIdx[i]*N_fft/fs);

        /* Calculate ULF,VLF,LF and HF*/
        ULF = Arrays.stream(Arrays.copyOfRange(power, 0, IdxNum[0]+1)).sum();
        VLF = Arrays.stream(Arrays.copyOfRange(power, IdxNum[0]+1, IdxNum[3]+1)).sum();
        
        int LFPeakNum = IdxNum[3]+1;
        for (int i = IdxNum[3]+1; i <= IdxNum[4]; i++) {
            LFPeakNum = power[i] > power[LFPeakNum] ? i : LFPeakNum;
            LF = LF + power[i];
        }
        LF_peak = frequency[LFPeakNum];

        int HFPeakNum = IdxNum[4]+1;
        for (int i = IdxNum[4]+1; i <= IdxNum[6]; i++) {
            HFPeakNum = power[i] > power[HFPeakNum] ? i : HFPeakNum;
            HF = HF + power[i];
        }
        HF_peak = frequency[HFPeakNum];

        pLF = LF/(VLF+HF)*100;
        pHF = HF/(VLF+LF)*100;
        ratio_LFHF = LF/HF*100;

        /* Calculate Coherence related*/
        int fPeakNum = IdxNum[3]+1;
        for (int i = IdxNum[3]+1; i <= IdxNum[5]; i++) {
            fPeakNum = power[i] > power[fPeakNum] ? i : fPeakNum;
        }
        double peakpower = Arrays.stream(Arrays.copyOfRange(power, IdxNum[2]+1, fPeakNum)).sum();
        double mTpL = Arrays.stream(Arrays.copyOfRange(power, 0, IdxNum[1]+1)).sum();
        double mTpH = Arrays.stream(Arrays.copyOfRange(power, 0, IdxNum[6]+1)).sum();
        double mTp = mTpH - mTpL;
        CR = peakpower/Math.pow(mTp-peakpower, 2);
                    
        PI[0] = max_min_HR;
        PI[1] = aHR;
        PI[2] = ULF;
        PI[3] = VLF;
        PI[4] = LF_peak;
        PI[5] = LF;
        PI[6] = HF_peak;
        PI[7] = HF;
        PI[8] = pLF;
        PI[9] = pHF;
        PI[10] = ratio_LFHF;
        PI[11] = CR;
        
    }
    
}