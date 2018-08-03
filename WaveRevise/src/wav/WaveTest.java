package wav;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import wav.WavFileReader;

public class WaveTest {
    private WavFileReader mWavFileReader;
    private WavFileWriter mWavFileWriter;

    private String DEFAULT_TEST_FILE_INPUT = "TestData/testInput.wav";
    private String DEFAULT_TEST_FILE_OUTPUT = "TestData/testOutput.wav";

    private byte[] WavData; //Bytes of the wav file
    private byte[] SoundData; //Bytes of the sound except the header information

//    private int WavByteSize; //The size of the wav file
//    private int SoundByteSize; //The size of the sound data
    private final int OriBpm = 15;


    public WaveTest(){
        WaveChange();
    }


    public static void main(String[] args){
//        WaveTest mWaveTest = new WaveTest();
    }

    private void WaveChange(){
        mWavFileReader = new WavFileReader();
        try {
            mWavFileReader.openFile(DEFAULT_TEST_FILE_INPUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataInputStream oldDataStream = new DataInputStream(mWavFileReader.getDataInputStream());
        try{
            int num = oldDataStream.available();
            WavFileHeader oldHeader = mWavFileReader.getHeader();
            WavFileHeader newHeader = oldHeader;
            newHeader.mSampleRate = 2*oldHeader.mSampleRate;
            /*read the bytes of the wave file*/
            WavData = new byte[num];
            if(mWavFileReader.readData(WavData,0,num) != -1)
                SoundData = Arrays.copyOfRange(WavData,45, num);
            if(mWavFileWriter == null) mWavFileWriter = new WavFileWriter();
            mWavFileWriter.openFile(DEFAULT_TEST_FILE_OUTPUT,oldHeader.mSampleRate,oldHeader.mNumChannel,oldHeader.mBitsPerSample);
            mWavFileWriter.writeData(SoundData,0,SoundData.length);
        }catch (IOException e){
            e.printStackTrace();}
    }

    private void readWavFile() {
        /*
         *  Read the orignal wav file information consisting of：
         *      The Overall size, sound data size, header
         */
        if (mWavFileReader == null)
        mWavFileReader = new WavFileReader();
        try {
            mWavFileReader.openFile(DEFAULT_TEST_FILE_INPUT);
//            mWavFileReader.setDataInputStream(mInputStream);

            int WavByteSize = mWavFileReader.getHeader().mChunkSize;
//            int SoundByteSize = mWavFileReader.getHeader().mSubChunk2Size;

            if(mWavFileReader.readData(WavData,0,WavByteSize) != -1)
                SoundData = Arrays.copyOfRange(WavData,45, WavByteSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getResampleSoundBuffer (int ResampleFs){
        byte[] NewSoundData = interpolation(SoundData,ResampleFs,OriBpm);
        WavFileHeader ResampleHeader = mWavFileReader.getHeader();
        ResampleHeader.mSubChunk2Size = NewSoundData.length;
        int SizeChange = NewSoundData.length - mWavFileReader.getHeader().mSubChunk2Size;
        ResampleHeader.mChunkSize = ResampleHeader.mChunkSize + SizeChange;

    }
//
    private static byte[] interpolation(byte[] data, int NewFs, int OldFs){
        /* interpolation function for byte data*/
        return toByteArray(interpolation(toIntArray(data), NewFs, OldFs));
    }
    private static int[] interpolation(int[] data, int NewFs, int OldFs){
        /* interpolation function for int data*/
        int fs = lcm(NewFs, OldFs);
        int mul = fs/OldFs;

        double dif = 0;
        int[] newdata = new int[1+(data.length-1)*mul];
        if(fs == OldFs)
            newdata = data;
        else{
            newdata[0] = data[0];
            for (int i = 0; i <data.length-1; i ++){
                dif = data[i+1] - data[i];
                for (int j =1; j <= mul; j++)
                    newdata[i*mul+j] = (int) Math.round(data[i]+dif*j/mul);
            }
        }

        int[] resample = new int[1+(newdata.length-1)*NewFs/OldFs];

        for(int i = 0; i < resample.length; i++){
            resample[i] = newdata[i*fs/NewFs];
        }
        return resample;
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

    public static byte[] toByteArray(int[] ints)
    {
        final ByteBuffer buf = ByteBuffer.allocate(ints.length * 4)
                .order(ByteOrder.LITTLE_ENDIAN);
        buf.asIntBuffer().put(ints);
        return buf.array();
    }

    public static int[] toIntArray(byte buf[])
    {
        final ByteBuffer buffer = ByteBuffer.wrap(buf)
                .order(ByteOrder.LITTLE_ENDIAN);
        final int[] ret = new int[buf.length / 4];
        buffer.asIntBuffer().put(ret);
        return ret;
    }

}
