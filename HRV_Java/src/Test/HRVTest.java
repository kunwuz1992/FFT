package Test;

import alg.FFTCal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import alg.Filtfilt;
import alg.PACER;
import alg.PacerGenerator;
import alg.iirj.Butterworth;

public class HRVTest {

    private static String FFT_TEST_FILE_PATH = "TestData/test1_hrv_data.csv";
    private static String FILTER_TEST_FILE_PATH = "TestData/wave1.dat";

    public static void main(String[] args) throws IOException {
//        FFTTest();
//        filtertest();
        filtfilttest();
    }

    public static void FFTTest() throws IOException {

        int nPR;
        int PR_For_FFT_Index = 0;
        int DEV_HZ = 1; //device frequency
        int RESAMPLE_HZ = 3; //Resample frequency
        int _PR_For_Index_MAX = 30; //The length of the data window

        ArrayList<Double> _PR_For_Index = new ArrayList<>();
        ArrayList<Double> _Index_From_PR = new ArrayList<>();

        ArrayList<Double> bpmAll = new ArrayList<>();

        //read data from file
        Scanner scanner = new Scanner(new File(FFT_TEST_FILE_PATH));
        while (scanner.hasNext()) {
            bpmAll.add(Double.parseDouble(scanner.next()));
        }
        scanner.close();

//        //read data from string
//        int _rawdata_index = 0;
//        String _rawdata = "73:61, 72:61, 72:61, 72:61, 72:61, 71:61, 70:61, 68:61, 65:61, 62:61, 59:61, 56:61, 53:61, 51:61, 48:61, 46:61, 45:61, 43:61, 42:61, 35:61, 33:61, 32:61, 31:61, 30:61, 29:61, 28:60, 26:60, 25:60, 24:60, 23:60, 22:60, 21:60, 20:60, 19:60, 20:60, 26:60, 38:60, 55:60, 74:60, 91:60, 102:60, 107:60, 107:60, 104:60, 99:60, 95:60, 91:60, 87:60, 83:60, 79:60, 76:60, 74:60, 74:60, 74:60, 74:60, 74:60, 71:60, 68:60, 64:60, 60:60, 56:60, 53:60, 49:60, 46:60, 44:60, 41:60, 39:60, 37:60, 36:60, 34:60, 34:60, 33:60, 32:60, 31:60, 30:60, 29:60, 28:60, 27:60, 26:60, 25:60, 23:60, 22:60, 21:60, 21:60, 24:60, 32:60, 47:60, 66:60, 86:60, 103:60, 114:60, 118:60, 117:60, 115:60, 111:60, 108:60, 105:60, 101:60, 97:60, 93:60, 89:60, 86:60, 84:60, 83:60, 82:60, 79:60, 76:60, 72:60, 67:60, 62:60, 58:60, 54:60, 50:60, 47:60, 43:60, 41:60, 39:60, 37:60, 36:60, 35:60, 34:60, 33:60, 31:60, 30:60, 29:60, 28:60, 26:60, 25:60, 24:60, 23:60, 21:60, 21:60, 20:60, 20:60, 19:60, 19:60, 19:60, 22:60, 30:60, 46:60, 63:60, 82:60, 99:60, 109:60";
//        _rawdata = _rawdata.trim();
//        String[] _rawdata_arr = _rawdata.split(",");
//        bpmAll.clear();
//        for (String s:_rawdata_arr)
//        {
//            String[] lsPairs = s.split(":");
//            if(_rawdata_index == 0)
//                bpmAll.add(Double.parseDouble(lsPairs[lsPairs.length-1]));
//            _rawdata_index++;
//            if(_rawdata_index==50) _rawdata_index = 0;
//        }
//        for(double x:bpmAll) System.out.println(x);
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

    public static void filtertest() throws IOException{
        // Read the file data
        List<Double> InputData = new ArrayList<>();
        Scanner scanner = new Scanner(new File(FILTER_TEST_FILE_PATH));
        while (scanner.hasNext()) {
            InputData.add(Double.parseDouble(scanner.next()));
        }
        scanner.close();

        // Construct a filter
        int Order = 6;
        int SampleRate = 250;
        double out = 0;
        int CutoffFrequency = 100;
        Butterworth butterworth = new Butterworth();
        butterworth.highPass(Order, SampleRate, CutoffFrequency/2, 0);
        for(double x:InputData) {
            out = butterworth.filter(x);
            System.out.println("Output: " + out);
        }
    }

    public static void pacertest() {
        int br = 7;

        List<PACER> ps = new ArrayList<PACER>();
        for(int i =6; i < 15; i++){
            br = i;
            PacerGenerator pg = new PacerGenerator(br,ps);
            pg.stepgeneration();
            System.out.println(pg.PacerVal);}
    }

    public static void filtfilttest() throws IOException{
        /**
         *
         * @param _B
         *          filter numerator
         * @param _A
         *          filter denominator
         * @param X
         *          the input: signal to be filtered
         * @output
         * 			the output: filtered sequence
         */

        //initialize the filter
        Double[] _A = {1.0000,-1.1876,1.3052,-0.6743, 0.2635,-0.0518, 0.0050};
        Double[] _B = {0.0701,-0.4207,1.0517,-1.4023, 1.0517,-0.4207, 0.0701};
        ArrayList<Double> A = new ArrayList<>();
        ArrayList<Double> B = new ArrayList<>();
        A.addAll(Arrays.asList(_A));
        B.addAll(Arrays.asList(_B));
        Filtfilt mFiltfilt = new Filtfilt(B,A);

        // Read the file data
        ArrayList<Double> InputData = new ArrayList<>();
        Scanner scanner = new Scanner(new File(FILTER_TEST_FILE_PATH));
        while (scanner.hasNext()) {
            InputData.add(Double.parseDouble(scanner.next()));
        }
        scanner.close();

        //Test
        mFiltfilt.doFiltfilt(InputData);
        ArrayList<Double> Output = mFiltfilt.getFilterdSignal();
        for(double x:Output) System.out.println(x);
    }


}
