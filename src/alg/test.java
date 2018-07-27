package alg;

import static java.lang.Math.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

import alg.FFTCal;

public class test{

    public static void main(String[] args) {
        // int N = (int)Math.pow(2, 8);
        int dfs = 1;
        int rfs = 3;

        int N = dfs*60*2;
        int N_fft = (int)Math.pow(2,nextPowerOf2(N));


//        // input
//        double[] bpm = new double[N];
//        //output
//        double[] index = new double[12];
//
//        double[] re = new double[N_fft];
//        double[] im = new double[N_fft];
//
//        double[] power = new double[N_fft/2+1];
//        double[] frequency = new double[N_fft/2+1];
        ArrayList<Double> bpmAll = new ArrayList<Double>();
        ArrayList<Double> index = new ArrayList<Double>();
        ArrayList<Double> bpm = new ArrayList<Double>();
        try{
            Scanner scanner = new Scanner(new File("TestData/test1_hrv_data.csv"));
            while (scanner.hasNext())
            {
                bpmAll.add(Double.parseDouble(scanner.next()));
//                re[i] = 60/bpm[i];
//                im[i] = 0;
            }
            scanner.close();
            for(int itx = 0; itx < bpmAll.size()/N; itx ++){
                bpm.clear();
                index.clear();
                bpm.addAll(bpmAll.subList(itx*N,(itx+1)*N));
                FFTCal fftcal = new FFTCal(bpm,index,dfs,rfs,false);
                fftcal.indexcal();
                if(index.get(3) <= 0) System.out.println(index.get(3));
//                System.out.println("index:");
//                for(double x:index) System.out.println(x);
//                System.out.println("end");
            }
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