package com.example.kw.pacersound.alg.wav;

import android.media.AudioFormat;
import android.media.AudioManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WavGenerator {
    /**
     *  Generator new sound stream based on the resampling rate using sonic lib
     * @param speed scale for speed
     * @param pitch scale for speed
     * @param rate scale for rate
     * @param volume scale for volume
     * @param emulateChordPitch the vocal chord mode for pitch computation
     * @param quality scale for quality, 0 or 1
     * @param SourceStream Input stream for source file
     * @param sampleRate the sample rate for source file
     * @param numChannels the number of channels for source file
     */

    private float speed;
    private float pitch;
    private float rate;
    private float volume;
    private boolean emulateChordPitch;
    private int quality;

//    private int sampleRate;
//    private int numChannels;
    private final int sampleRate = 44100;
    private final int numChannels = 2;
    private Sonic mSonic;
    private InputStream SourceStream;
    private List<byte[]> OutByteList;
//    private WavFileReader mWavFileReader;

    public WavGenerator(String path){
        File filein = new File(path);
        try {
//            this.mWavFileReader = new WavFileReader();
            this.SourceStream = new FileInputStream(filein);
//            mWavFileReader.setDataInputStream(SourceStream);
//            this.sampleRate = mWavFileReader.getHeader().mSampleRate;
//            this.numChannels = mWavFileReader.getHeader().mNumChannel;
            this.mSonic = new Sonic(sampleRate, numChannels);
//            mWavFileReader.closeFile();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public WavGenerator(InputStream mStream){
        this.SourceStream = mStream;
        this.mSonic = new Sonic(sampleRate, numChannels);
    }

    public boolean setSonic(float speed,float rate,int quality){
        // check whether Sonic exists
        if(mSonic == null)
            return false;
        //Set sonic params
        mSonic.setQuality(quality);
        mSonic.setSpeed(speed);
        mSonic.setRate(rate);
        return true;
    }

    public boolean generate(){
        //generate new wav
        int bufferSize = sampleRate;
        byte inBuffer[] = new byte[bufferSize];
        byte outBuffer[] = new byte[bufferSize];
        int numRead, numWritten;
        if (OutByteList == null)
            OutByteList = new ArrayList<>();
        else
            OutByteList.clear();

        try{
            do {
                numRead = SourceStream.read(inBuffer, 0, bufferSize);
                if(numRead <= 0) {
                    mSonic.flushStream();
                } else {
                    mSonic.writeBytesToStream(inBuffer, numRead);
                }
                do {
                    numWritten = mSonic.readBytesFromStream(outBuffer, bufferSize);
                    if(numWritten > 0) {
                        byte[] tmp = Arrays.copyOfRange(outBuffer,0,numWritten);
                        OutByteList.add(tmp);
                    }
                } while(numWritten > 0);
            }while(numRead > 0);
            SourceStream.close();
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public byte[] getOutByteArray(){
        byte[] OutByte = null;
        for(byte[] buffer:OutByteList){
            if(OutByte == null)
                OutByte = buffer;
            else{
                byte[] tmpByte = new byte[OutByte.length + buffer.length];
                System.arraycopy(OutByte, 0,tmpByte,0, OutByte.length);
                System.arraycopy(buffer, 0, tmpByte, OutByte.length, buffer.length);
                OutByte = tmpByte;
            }
        }
        return OutByte;
    }

    public List<byte[]> getOutByteList(){
        return OutByteList;
    }

}
