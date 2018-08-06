package alg;

import java.io.File;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;


public class test{

    private static String TEST_FILE_PATH = "TestData/test1_hrv_data.csv";

    private static int DEV_HZ = 1; //device frequency
    private static int RESAMPLE_HZ = 3; //Resample frequency

    private static int _PR_For_Index_MAX = 30; //The length of the data window

    public static void main(String[] args) throws IOException {

        int nPR;
        int PR_For_FFT_Index = 0;

        ArrayList<Double> _PR_For_Index = new ArrayList<>();
        ArrayList<Double> _Index_From_PR = new ArrayList<>();

        ArrayList<Double> bpmAll = new ArrayList<>();

        //Save nPR data into a list
        Scanner scanner = new Scanner(new File(TEST_FILE_PATH));
        while (scanner.hasNext()) {
            bpmAll.add(Double.parseDouble(scanner.next()));
        }
        scanner.close();

        // read nPR data from list
        double endPR;// the lastest nPR data in the list for PR calculation
        double diff;
        int count = 0;
        while(bpmAll.size()>0)
        {
            nPR = bpmAll.remove(0).intValue();
            if (PR_For_FFT_Index < _PR_For_Index_MAX) {
                if (nPR != 0) {
                    //Resample the nPR with the new sampling rate RESAMPLE_HZ
                    if (PR_For_FFT_Index == 0) _PR_For_Index.add((double) nPR);
                    endPR = _PR_For_Index.get(_PR_For_Index.size() - 1);
                    diff = ((double) nPR - endPR) / RESAMPLE_HZ;
                    for (int i = 1; i <= RESAMPLE_HZ; i++) {
                        _PR_For_Index.add(endPR + diff * i);
                    }
                    PR_For_FFT_Index++;
                }
            } else {
                _Index_From_PR.clear();
                endPR = _PR_For_Index.get(_PR_For_Index.size() - 1);
                diff = ((double) nPR - endPR) / RESAMPLE_HZ;
                for (int i = 1; i <= RESAMPLE_HZ; i++) {
                    _PR_For_Index.remove(0);
                    _PR_For_Index.add(endPR + diff * i);
                }
                FFTCal fftCal = new FFTCal(_PR_For_Index, _Index_From_PR, RESAMPLE_HZ);
                fftCal.indexcal();
                count = count + 1;

                //output
                System.out.println("Start:" + count);
                for (double x: _Index_From_PR) System.out.println(x);
                System.out.println("End");
            }
        }
    }
}