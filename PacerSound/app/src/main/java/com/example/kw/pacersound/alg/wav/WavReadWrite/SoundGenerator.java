package com.example.kw.pacersound.alg.wav.WavReadWrite;
/**
 * Package replacement in Android
 * avax.sound.sampled.AudioFormat - android.media.AudioFormat;
 */

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class SoundGenerator {
    /**
     *  Generator new sound stream based on the resampling rate
     * @param Path the file path of the source file
     * @param mWaveFileReader the source file format information
     * @param HEADER_LENGTH the byte length of wav file header
     * @param BlockAlign the number of bytes per sample
     * @param OriBpm the default rate of the source file
     */
    private WavFileReader mWavFileReader;
    private byte[] SoundData; //Bytes of the sound except the header information
    private String Path;
    private InputStream mInputStream;
    private int BlockAlign;

    private final int HEADER_LENGTH = 44;
    private final int OriBpm = 15;

    //establish the class through the file path
    public SoundGenerator(String path){
        this.Path = path;
        try {
            mWavFileReader = new WavFileReader();
            mWavFileReader.openFile(Path);
        } catch (Exception e){
            e.printStackTrace();
        }
        init();
    }
    //establish the class through the file path
    public SoundGenerator(InputStream inputStream){
        this.mInputStream = inputStream;
        try {
            mWavFileReader = new WavFileReader();
            mWavFileReader.setDataInputStream(mInputStream);
        } catch (Exception e){
            e.printStackTrace();
        }
        init();
    }

    //establish the class through the file path
    public SoundGenerator(WavFileReader wavefilereader){
        try
        {
            this.mWavFileReader = wavefilereader;
            int WavLength = mWavFileReader.getHeader().mChunkSize+8;
            byte[] WavByte = new byte[WavLength];
            if(mWavFileReader.readData(WavByte,0,WavLength)>0)
            {
                SoundData = Arrays.copyOfRange(WavByte,HEADER_LENGTH,WavByte.length);
                BlockAlign = mWavFileReader.getHeader().mBlockAlign;
            } else{
                throw new NullPointerException("Wav file is empty");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void init(){
        /**
         * Initialization:
         * Obtain the header information and get the sound data
         */
        try {
            int WavLength = mWavFileReader.getHeader().mChunkSize+8;
            byte[] WavByte = new byte[WavLength];
            if(mWavFileReader.readData(WavByte,0,WavLength)>0)
            {
                SoundData = Arrays.copyOfRange(WavByte,HEADER_LENGTH,WavByte.length);
                BlockAlign = mWavFileReader.getHeader().mBlockAlign;
            } else{
                throw new NullPointerException("Wav file is empty");
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public byte[] SoundGeneration(int resample){

        byte[] NewSoundData = interpolation(SoundData,resample,OriBpm,BlockAlign);
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

    /*---------------------------------------------------------------------------------------*/
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
            if (x == 2) tmp = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) ints[i]).array();
            if (x == 4) tmp = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ints[i]).array();
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
        short b2i = 0;

        for(int i =0; i < ret.length; i++){
            data = Arrays.copyOfRange(buf, i*x, (i+1)*x);
            b2i = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getShort();
            ret[i] = b2i;
        }
        return ret;
    }
}
