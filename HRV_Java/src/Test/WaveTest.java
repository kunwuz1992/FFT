package Test;

import alg.wav.WavFileHeader;
import alg.wav.WavFileReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class WaveTest {
    /*
    * The input is
    * */
    private WavFileReader mWavFileReader;
    private byte[] WavData; //Bytes of the wav file
    private byte[] SoundData; //Bytes of the sound except the header information
    private String Path;
    
    private final int OriBpm = 15;


    public WaveTest(String path){
        this.Path = path;
        SetupWaveReader();
    }

    public static void main(String[] args) {
        String DEFAULT_TEST_FILE_INPUT = "TestData/testInput.wav";
        String DEFAULT_TEST_FILE_OUTPUT = "TestData/testOutput.wav";

        String pth= "TestData/testInput.wav";
        WaveTest mWaveTest = new WaveTest(pth);
        byte[] sound = mWaveTest.getResampleSoundBuffer(6);
        for(byte x:sound) System.out.println(x);
    }

    private void SetupWaveReader(){
        /*Initialize the wav reader*/
        if(mWavFileReader != null) {
            try{
                mWavFileReader.closeFile();}
            catch (Exception e){e.printStackTrace();}
        }

        mWavFileReader = new WavFileReader();
        try{
            mWavFileReader.openFile(Path);}
        catch (Exception e){e.printStackTrace();}

        /*read the bytes of the wave file*/
        int num = mWavFileReader.getHeader().mChunkSize;
        WavData = new byte[num];
        if(mWavFileReader.readData(WavData,0,num) != -1)
            SoundData = Arrays.copyOfRange(WavData,45, num);
    }

    public byte[] getResampleSoundBuffer (int ResampleFs){
        /*Obtain the new sound data bytes*/

        byte[] NewSoundData = interpolation(SoundData,ResampleFs,OriBpm,mWavFileReader.getHeader().mBlockAlign); //interpolate the original sound data

        //Reformulate a new header and convert to byte array
        WavFileHeader ResampleHeader = mWavFileReader.getHeader();
        ResampleHeader.mSubChunk2Size = NewSoundData.length;
        int SizeChange = NewSoundData.length - mWavFileReader.getHeader().mSubChunk2Size;
        ResampleHeader.mChunkSize = ResampleHeader.mChunkSize + SizeChange;

        //formulate the new sound data
        byte[] SoundByte = new byte[44 + NewSoundData.length];
        System.arraycopy(WriteHeader(ResampleHeader),0,SoundByte, 0,WriteHeader(ResampleHeader).length);
        System.arraycopy(NewSoundData, 0, SoundByte, 44, NewSoundData.length);

        return SoundByte;
    }

    private static byte[] WriteHeader(WavFileHeader header){
        /*Write the header information into bytes*/
        int size = 44; //the size of a wav header is 44
        byte[] results = new byte[size]; // the header byte array

        byte[] ChunkID = header.mChunkID.getBytes();
        System.arraycopy(ChunkID,0,results,0,ChunkID.length);
        size = ChunkID.length;

        byte[] ChunkSize = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(header.mChunkSize).array();
        System.arraycopy(ChunkSize,0,results,size,ChunkSize.length);
        size = size + ChunkSize.length;

        byte[] Format = header.mFormat.getBytes();
        System.arraycopy(Format,0,results,size,Format.length);
        size = size + Format.length;

        byte[] SubChunk1ID = header.mSubChunk1ID.getBytes();
        System.arraycopy(SubChunk1ID,0,results,size,SubChunk1ID.length);
        size = size + SubChunk1ID.length;

        byte[] SubChunk1Size = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(header.mSubChunk1Size).array();
        System.arraycopy(SubChunk1Size,0,results,size,SubChunk1Size.length);
        size = size + SubChunk1ID.length;

        byte[] AudioFormat = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(header.mAudioFormat).array();
        System.arraycopy(AudioFormat,0,results,size,AudioFormat.length);
        size = size + AudioFormat.length;

        byte[] NumChannel = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(header.mNumChannel).array();
        System.arraycopy(NumChannel,0,results,size,NumChannel.length);
        size = size + NumChannel.length;

        byte[] SampleRate =  ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(header.mSampleRate).array();
        System.arraycopy(SampleRate,0,results,size,SampleRate.length);
        size = size + SampleRate.length;

        byte[] ByteRate =  ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(header.mByteRate).array();
        System.arraycopy(ByteRate,0,results,size,ByteRate.length);
        size = size + ByteRate.length;

        byte[] BlockAlign = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(header.mBlockAlign).array();
        System.arraycopy(BlockAlign,0,results,size,BlockAlign.length);
        size = size + BlockAlign.length;

        byte[] Bitspersample = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(header.mBitsPerSample).array();
        System.arraycopy(Bitspersample,0,results,size,Bitspersample.length);
        size = size + Bitspersample.length;

        byte[] SubChunk2ID = header.mSubChunk2ID.getBytes();
        System.arraycopy(SubChunk2ID,0,results,size,SubChunk2ID.length);
        size = size + SubChunk1ID.length;

        byte[] SubChunk2Size = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(header.mSubChunk2Size).array();
        System.arraycopy(SubChunk2Size,0,results,size,SubChunk2Size.length);
        size = size + SubChunk2Size.length;

        return results;
    }



    private static byte[] interpolation(byte[] data, int NewFs, int OldFs, int bytespersample){
        /* interpolation function for byte data*/
        return toByteArray(interpolation(toIntArray(data,bytespersample), NewFs, OldFs),bytespersample);
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

        int[] resample = new int[1+(data.length-1)*NewFs/OldFs];

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

    public static byte[] toByteArray(int[] ints, int x)
    {
        /*Converting every x bytes of an integer array into a byte array:*/
        byte[] buf = new byte[ints.length*x];
        byte[] tmp = new byte[x];
        for(int i =0; i < ints.length; i++) {
            if (x == 2) tmp = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort((short) ints[i]).array();
            if (x == 4) tmp = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(ints[i]).array();
            if (tmp != null) {
                for(int j = 0; j < x; j++) buf[i * x + j] = tmp[j];
            } else {
                return null;
            }
        }
        return buf;  
    }

    public static int[] toIntArray(byte buf[], int x)
    {
        /*Converting every x bytes of a byte array into an integer array:*/
        byte[] data = new byte[x];
        int[] ret = new int[buf.length/x];

        for(int i =0; i < ret.length; i++){
            data = Arrays.copyOfRange(buf, i*x, (i+1)*x);
            ret[i] = (int)((0xff & data[0]) << 8 | (0xff & data[1]) << 0);
        }
        return ret;
    }

}
