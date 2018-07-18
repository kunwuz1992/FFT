import static java.lang.Math.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

public class FFT {  

  int n, m;
  
  // Lookup tables.  Only need to recompute when size of FFT changes.
  double[] cos;
  double[] sin;
  
  //bpm
  // double[] bpm;
  // //frequency domain results;
  // double[] frequency;
  // double[] power;
  
  double[] window;
  //index_fd = [max_min_HR aHR ULF VLF LF_peak LF  HF_peak HF pLF pHF ratio_LFHF CR];
  // double[] index_fd;

  
  public FFT(int n) {
    this.n = n;
    this.m = (int)(Math.log(n) / Math.log(2));

    // Make sure n is a power of 2
    if(n != (1<<m))
      throw new RuntimeException("FFT length must be power of 2");

    // precompute tables
    cos = new double[n/2];
    sin = new double[n/2];

//     for(int i=0; i<n/4; i++) {
//       cos[i] = Math.cos(-2*Math.PI*i/n);
//       sin[n/4-i] = cos[i];
//       cos[n/2-i] = -cos[i];
//       sin[n/4+i] = cos[i];
//       cos[n/2+i] = -cos[i];
//       sin[n*3/4-i] = -cos[i];
//       cos[n-i]   = cos[i];
//       sin[n*3/4+i] = -cos[i];        
//     }
    for(int i=0; i<n/2; i++) {
      cos[i] = Math.cos(-2*Math.PI*i/n);
      sin[i] = Math.sin(-2*Math.PI*i/n);
    }

    makeWindow();
  }

  protected void makeWindow() {
    // Make a blackman window:
    // w(n)=0.42-0.5cos{(2*PI*n)/(N-1)}+0.08cos{(4*PI*n)/(N-1)};
    window = new double[n];
    for(int i = 0; i < window.length; i++)
      window[i] = 0.42 - 0.5 * Math.cos(2*Math.PI*i/(n-1)) 
        + 0.08 * Math.cos(4*Math.PI*i/(n-1));
  }
  
  public double[] getWindow() {
    return window;
  }


  /***************************************************************
  * fft.c
  * Douglas L. Jones 
  * University of Illinois at Urbana-Champaign 
  * January 19, 1992 
  * http://cnx.rice.edu/content/m12016/latest/
  * 
  *   fft: in-place radix-2 DIT DFT of a complex input 
  * 
  *   input: 
  * n: length of FFT: must be a power of two 
  * m: n = 2**m 
  *   input/output 
  * x: double array of length n with real part of data 
  * y: double array of length n with imag part of data 
  * 
  *   Permission to copy and use this program is granted 
  *   as long as this header is included. 
  ****************************************************************/
  public void fft(double[] x, double[] y)
  {
    int i,j,k,n1,n2,a;
    double c,s,e,t1,t2;
  
  
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

  // Test the FFT to make sure it's working
  public static void main(String[] args) {
    
    int N = (int)Math.pow(2, 13);
    double[] bpm = new double[N];
    double[] index = new double[12];

    try{  
      Scanner scanner = new Scanner(new File("test1_hrv_data.csv"));
      for(int i=0; i<N; i++)
      {
        bpm[i] = Double.parseDouble(scanner.next());  
      }
      scanner.close();
    } catch (FileNotFoundException e){
      System.out.print("File not found");
    }
    indexcal(bpm,index);
    for(int i = 0; i<8; i++) System.out.println(index[i]);
  }

  public static void fillspectrum(double[] bpm, double fs, int N_fft, double[] power, double[] frequency){

    FFT fft = new FFT(N_fft);
    double[] window = fft.getWindow();
    double[] re = new double[N_fft];
    double[] im = new double[N_fft];

    for(int i = 0; i < N_fft; i++)
    {
      re[i] = 60/bpm[i];
      im[i] = 0;
    }

    fft.fft(re, im);
    
    for(int i = 0; i < N_fft/2+1; i++){
      // power[i] = re[i];
      power[i] = 2*(Math.pow(re[i]/N_fft, 2)+Math.pow(im[i]/N_fft, 2));
      frequency[i] = (double)i/ (double)(N_fft/2+1);
    } 
  }

  public static int nextPowerOf2(final int a)
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
  
  public static void indexcal(double[] bpm, double[] PI){

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
    double peakpower = 0;
    double mLFpeak = 0;
    int mlFpeak_index = 0;
    double mTpL = 0;
    double mTpH = 0;
    double mTp = 0;


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
    double fs = 3.0;

    fillspectrum(bpm,fs,N_fft,power,frequency);

    for(int i = 0; i<N_fft/2+1; i++){
      if(frequency[i] <= 0.003){
        ULF = ULF + power[i];
        mTpL = mTpL + power[i];
        mTpH = mTpH + power[i];
      } else if ((frequency[i] <= 0.0033) && (frequency[i] > 0.003)) {
        VLF = VLF + power[i];
        mTpL = mTpL + power[i];
        mTpH = mTpH + power[i];
      } else if ((frequency[i] <= 0.015) && (frequency[i] > 0.0033)) {
        VLF = VLF + power[i];
        mTpH = mTpH + power[i];
      } else if ((frequency[i] <= 0.04) && (frequency[i] > 0.015)) {
        VLF = VLF + power[i];
        peakpower = peakpower + power[i];
        mTpH = mTpH + power[i];
      } else if ((frequency[i] <= 0.15) && (frequency[i] > 0.04)) {
        LF = LF + power[i];
        if ( LF_peak < power[i] ){LF_peak = power[i];}
        if ( mLFpeak < power[i] ){
          mLFpeak = power[i];
          mlFpeak_index = i;
          peakpower = peakpower + power[i];
        }
        mTpH = mTpH + power[i];
      } else if ((frequency[i] <= cr_cutoff) && (frequency[i] > 0.15)) {
        HF = HF + power[i];
        if ( HF_peak < frequency[i] ){HF_peak = power[i];}
        if ( mLFpeak < power[i] ){
          mLFpeak = power[i];
          mlFpeak_index = i;
        }
        mTpH = mTpH + power[i];
      } else if ((frequency[i] < 0.4) && (frequency[i] > cr_cutoff)) {
        HF = HF + power[i];
        if (HF_peak < frequency[i]){HF_peak = power[i];}
        mTpH = mTpH + power[i];
      } else if (frequency[i] == 0.4){
        HF = HF + power[i];
        if (HF_peak < frequency[i]){HF_peak = power[i];}
      } 

      pLF = LF/(VLF+HF)*100;
      pHF = HF/(VLF+LF)*100;
      ratio_LFHF = LF/HF*100; 

      mTp = mTpH - mTpL;
      CR = peakpower/Math.pow(mTp-peakpower, 2);
      
      PI[0] = max_min_HR;
      PI[1] = aHR;
      PI[2] = ULF;
      PI[3] = VLF;
      PI[4] = LF;
      PI[5] = LF_peak;
      PI[6] = HF;
      PI[7] = HF_peak;
      PI[8] = pLF;
      PI[9] = pHF;
      PI[10] = ratio_LFHF;
      PI[11] = CR;
    }
  }


  protected static void printReIm(double[] re, double[] im) {
    System.out.print("Re: [");
    for(int i=0; i<re.length; i++)
      System.out.print(((int)(re[i]*1000)/1000.0) + " ");

    System.out.print("]\nIm: [");
    for(int i=0; i<im.length; i++)
      System.out.print(((int)(im[i]*1000)/1000.0) + " ");

    System.out.println("]");
  }

  protected static void printReIm(double[] re, double[] im, int num) {
    System.out.print("Re: [");
    for(int i=0; i<num; i++)
      System.out.print(re[i] + " ");

    System.out.print("]\nIm: [");
    for(int i=0; i<num; i++)
      System.out.print(im[i] + " ");

    System.out.println("]");
  }
}