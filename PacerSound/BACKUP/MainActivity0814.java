package com.example.kw.pacersound;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.kw.pacersound.alg.PacerGenerator;
import com.example.kw.pacersound.alg.draw.DrawPacer;
import com.example.kw.pacersound.alg.wav.AudioPlayer;
import com.example.kw.pacersound.alg.wav.WavGenerator;
import com.example.kw.pacersound.alg.wav.WavReadWrite.SoundGenerator;
import com.example.kw.pacersound.alg.wav.WavReadWrite.WavFileReader;
import com.example.kw.pacersound.recvdata.PACER;
import com.example.kw.pacersound.recvdata.PacerPattern;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity0814 extends AppCompatActivity {
    private SoundGenerator mGenerator;
    private WavGenerator mWaveGenerator;
    private WavFileReader mWavFileReader;
    private Spinner mTestSpinner;
    private EditText InputReFs;
    // Spinner position
    private int SpinnerPosition;
    // resampled frequency
    private int rs;
    //Sound data
    private byte[] WavArray;
    private List<byte[]> Wavlist;
    //Thread for draw pacer
    private Thread drawThread;
    private Thread PlayThread;
    //Pacer
    private PacerPattern pacerPattern;
    private Thread drawPacerThread;
    private DrawPacer drawPacerRect;

    private AudioPlayer mAudioPlayer;
    private volatile boolean mIsTestingExit = false;

    public static final String[] TEST_PROGRAM_ARRAY = {
            "请选择播放速度",
            "播放速度 x0.5",
            "播放速度 x1",
            "播放速度 x2",
            "播放速度 x3",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTestSpinner = (Spinner) findViewById(R.id.TestSpinner);
        InputReFs = (EditText)findViewById(R.id.InputFS);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TEST_PROGRAM_ARRAY);
        mTestSpinner.setAdapter(adapter);
//        String path = "/assets/test.wav";
        findViewById(R.id.btnStartTest).setOnClickListener(ctrlListener);
        findViewById(R.id.btnOpenPacer).setOnClickListener(ctrlListener);
        findViewById(R.id.btnStopTest).setOnClickListener((ctrlListener));
        findViewById(R.id.btnOpenPacerSound).setOnClickListener((ctrlListener));
        drawPacerRect = (DrawPacer) findViewById(R.id.realpaly_draw_pacer);
    }

    private OnClickListener ctrlListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btnStartTest) {
                StartTest();
                Toast.makeText(v.getContext(), "Start Testing !", Toast.LENGTH_SHORT).show();
            } else if (v.getId() == R.id.btnStopTest) {
                stopTesting();
                Toast.makeText(v.getContext(), "Stop Testing !", Toast.LENGTH_SHORT).show();
            } else if (v.getId() == R.id.btnOpenPacer) {
                Button btnOpenPacer = (Button) findViewById(R.id.btnOpenPacer);
                boolean toOpen = (btnOpenPacer.getText().equals(getResources().getString(R.string.app_main_pacer_on)));
                OpenDrawingPacer(toOpen);
                if (toOpen)
                {
                    btnOpenPacer.setText(getResources().getString(R.string.app_main_pacer_off));
                } else{
                    btnOpenPacer.setText(getResources().getString(R.string.app_main_pacer_on));
                }
            } else if (v.getId() == R.id.btnOpenPacerSound) {
                Button btnOpenPacerSound = (Button) findViewById(R.id.btnOpenPacerSound);
                boolean toOpen = (btnOpenPacerSound.getText().equals(getResources().getString(R.string.app_main_pacer_sound_on)));
                if (toOpen){
                    if(pacerPattern==null) {
                        Toast.makeText(v.getContext(), "先请打开节拍器", Toast.LENGTH_SHORT).show();
                    } else
                    {
                        btnOpenPacerSound.setText(getResources().getString(R.string.app_main_pacer_sound_off));
                    }
                }
                else
                    btnOpenPacerSound.setText(getResources().getString(R.string.app_main_pacer_sound_on));
            }
        }
    };



    private void StartTest() {
        SpinnerPosition = mTestSpinner.getSelectedItemPosition();
        boolean SpinnerFlag = false;
        switch (SpinnerPosition){
            case 1:
                rs = 8;
                SpinnerFlag = true;
                break;
            case 2:
                rs = 30;
                SpinnerFlag = true;
                break;
            case 3:
                rs = 45;
                SpinnerFlag = true;
                break;
            case 4:
                rs = 60;
                SpinnerFlag = true;
                break;
            default:
                break;
        }

        if(!SpinnerFlag){
            rs = Integer.parseInt(InputReFs.getText().toString());
        }
        PlayerInitialization();
        mAudioPlayer = new AudioPlayer();
        mAudioPlayer.startPlayer();
        PlayThread = new Thread(AudioPlayRunnable,"SoundPlayThread");
        PlayThread.start();
    }

    /*------------------------------------Sound-------------------------------------*/
    private void PlayerInitialization(){
        try{
            InputStream mStream = this.getResources().openRawResource(R.raw.test);
            mWaveGenerator = new WavGenerator(mStream);
            mWaveGenerator.setSonic(rs/15.0f, 1.0f,0);
            if(mWaveGenerator.generate()){
                WavArray = mWaveGenerator.getOutByteArray();
                Wavlist = mWaveGenerator.getOutByteList();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean stopTesting() {
        mIsTestingExit = true;
        return true;
    }

    private Runnable AudioPlayRunnable = new Runnable() {
        @Override
        public void run() {
            int itr = 0;
            int OutputSamples = 0;
            int SAMPLES_PER_FRAME = 512;
            byte[] buffer = new byte[SAMPLES_PER_FRAME];
            if(!mIsTestingExit && (WavArray.length>0)){
                while (OutputSamples<= WavArray.length){
                    buffer = Arrays.copyOfRange(WavArray,itr*SAMPLES_PER_FRAME,(itr+1)*(SAMPLES_PER_FRAME));
                    OutputSamples = OutputSamples+ mAudioPlayer.play(buffer, 0, buffer.length);
                    itr ++;
                }
            }
            mAudioPlayer.stopPlayer();
        }
    };

    /*--------------------------Pacer--------------------------------------------------------------------*/
    private void OpenDrawingPacer(boolean pacerOn)
    {
        if(pacerOn) {
            PacerGenerator pg = new PacerGenerator(this,6,0);
            pg.generation();
            if(pacerPattern==null)
                pacerPattern= new PacerPattern();
            pacerPattern.AddPacers(0, pg.getPacerlist());
            pacerPattern.PACEPATTERNUSED = 0;
            pacerPattern.AddWav(0,pg.getWavlist());
        }else if(pacerPattern!=null)
            pacerPattern.PACEPATTERNUSED = -1;
        drawPacerThread = new Thread(drawPacerRect, "DrawPacerThread");
        drawPacerThread.start();

    }

}
