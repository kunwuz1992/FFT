package com.creative.alg;

import static java.lang.Math.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

import com.creative.alg.FFTCal;

public class test{

    public static void main(String[] args) {
        // int N = (int)Math.pow(2, 8);
        int N = 50*60*2;
        int N_fft = (int)Math.pow(2,nextPowerOf2(N));
        int fs = 50;
        // input
        double[] bpm = new double[N];
        //output
        double[] index = new double[12];
        
        double[] re = new double[N_fft];
        double[] im = new double[N_fft];

        double[] power = new double[N_fft/2+1];
        double[] frequency = new double[N_fft/2+1];

        try{
            Scanner scanner = new Scanner(new File("TestData/test1_hrv_data.csv"));
            for(int i=0; i<N; i++)
            {
                bpm[i] = Double.parseDouble(scanner.next());
                re[i] = 60/bpm[i];
                im[i] = 0;
            }
            scanner.close();

            FFTCal fftcal = new FFTCal(bpm,index,fs);

            fftcal.indexcal(index);
            System.out.println("index:");
            for(int i = 0; i<12; i++) System.out.println(index[i]);
        } catch (FileNotFoundException e){
            System.out.print("Data file not found");
        }
    }

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

}