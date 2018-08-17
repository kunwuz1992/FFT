package com.example.kw.pacersound;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kw.pacersound.alg.PacerGenerator;
import com.example.kw.pacersound.alg.draw.DrawPacer;
import com.example.kw.pacersound.alg.wav.AudioPlayer;
import com.example.kw.pacersound.recvdata.PacerPattern;

import java.util.Arrays;


public class MainActivity0816 extends AppCompatActivity {
    private Spinner mTestSpinner;
    private EditText InputReFs;
    //Sound data
    //Thread for draw pacer
    private int soundIndex;
    private boolean isUpdate;
    //Pacer
    private PacerPattern pacerPattern;
    private Thread drawPacerThread;

    private DrawPacer drawPacerRect;
    private AudioPlayer mAudioPlayer;
    private final int PLAYSTATE_PAUSED = 2;
    private final int PLAYSTATE_PLAYING = 3;
    private final int PLAYSTATE_STOPPED = 1;
    private int PLATER_STATE;


    private int rs = 6;

    public static final String[] TEST_PROGRAM_ARRAY = {
            "请选择播放速度",
            "播放速度 x0.5",
            "播放速度 x1",
            "播放速度 x2",
            "播放速度 x3",
    };

    private Handler playHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if (pacerPattern.PACEPATTERNUSED >= 0){
                if(pacerPattern.PACERSOUNDUSED >= 0){
                    isUpdate = msg.getData().getBoolean("PacerUpdate");
                    soundIndex = msg.getData().getInt("SoundIndex");
                    if(isUpdate){
                        try {
                            byte[] buffer = pacerPattern.getWav().get(soundIndex);
                            PlayerPlay(buffer);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                } else {
                    PlayerPause();
                }
            }
        }
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
        findViewById(R.id.btnOpenPacer).setOnClickListener(ctrlListener);
        findViewById(R.id.btnOpenPacerSound).setOnClickListener((ctrlListener));
        findViewById(R.id.btnSetRS).setOnClickListener((ctrlListener));
        findViewById(R.id.textCurrentBpm).setOnClickListener((ctrlListener));
        drawPacerRect = (DrawPacer) findViewById(R.id.realpaly_draw_pacer);
        drawPacerRect.setHandler(playHandler);
        // initialize audio player
        PlayerInit();
    }

    private OnClickListener ctrlListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btnOpenPacer) {
                Button btnOpenPacer = (Button) findViewById(R.id.btnOpenPacer);
                Button btnOpenPacerSound = (Button) findViewById(R.id.btnOpenPacerSound);
                boolean toOpen = (btnOpenPacer.getText().equals(getResources().getString(R.string.app_main_pacer_on)));
                OpenDrawingPacer(toOpen);
                if (toOpen)
                {
                    btnOpenPacer.setText(getResources().getString(R.string.app_main_pacer_off));
                } else{
                    btnOpenPacer.setText(getResources().getString(R.string.app_main_pacer_on));
                    btnOpenPacerSound.setText(getResources().getString(R.string.app_main_pacer_sound_on));
                    PlayerPause();
                }
            } else if (v.getId() == R.id.btnOpenPacerSound) {
                Button btnOpenPacerSound = (Button) findViewById(R.id.btnOpenPacerSound);
                boolean toOpen = (btnOpenPacerSound.getText().equals(getResources().getString(R.string.app_main_pacer_sound_on)));
                boolean PacerExist = OpenPacerSound(v.getContext(),toOpen);
                if(PacerExist) {
                    if (toOpen){
                        btnOpenPacerSound.setText(getResources().getString(R.string.app_main_pacer_sound_off));
                    }
                    else{
                        btnOpenPacerSound.setText(getResources().getString(R.string.app_main_pacer_sound_on));
                    }
                }
            } else if (v.getId() == R.id.btnSetRS){
                String InputStr=  InputReFs.getText().toString();
                if(InputStr.equals(""))
                    Toast.makeText(v.getContext(), "请输入", Toast.LENGTH_SHORT).show();
                else{
                    rs = Integer.parseInt(InputReFs.getText().toString());
                    TextView currentRS = findViewById(R.id.textCurrentBpm);
                    currentRS.setText(String.valueOf(rs));
                }
                if(pacerPattern !=null) {
                    pacerPattern.Patternclear();
                    drawPacerRect.drawClear();

                    PlayerPause();
                    Button btnOpenPacer = (Button) findViewById(R.id.btnOpenPacer);
                    btnOpenPacer.setText(getResources().getString(R.string.app_main_pacer_on));
                    Button btnOpenPacerSound = (Button) findViewById(R.id.btnOpenPacerSound);
                    btnOpenPacerSound.setText(getResources().getString(R.string.app_main_pacer_sound_on));
                }
            }
        }
    };

    /*--------------------------Pacer--------------------------------------------------------------------*/
    private void OpenDrawingPacer(boolean pacerOn)
    {
        if(pacerOn) {
            //Set pacer bpm
            PacerGenerator pg = new PacerGenerator(this,rs,0);
            pg.generation();
            if(pacerPattern==null)
                pacerPattern= new PacerPattern();
            pacerPattern.AddPacers(0, pg.getPacerlist());
            pacerPattern.PACEPATTERNUSED = 0;
            pacerPattern.AddWav(0,pg.getWavlist());
        }else {
            if(pacerPattern!=null) pacerPattern.PACEPATTERNUSED = -1;
        }
        drawPacerThread = new Thread(drawPacerRect, "DrawPacerThread");
        drawPacerThread.start();
    }

    /*--------------------------Pacer--------------------------------------------------------------------*/
    private boolean OpenPacerSound(Context context, boolean soundOn){
        if(pacerPattern==null)
        {
            Toast.makeText(context, "先请打开节拍器", Toast.LENGTH_SHORT).show();
            return false;
        } else{
            if(soundOn) {
                pacerPattern.PACERSOUNDUSED = 0;
            }else {
                if(pacerPattern!=null) pacerPattern.PACERSOUNDUSED = -1;
            }
            return true;
        }
    }

    private void PlayerInit(){
        if(mAudioPlayer == null)
            mAudioPlayer = new AudioPlayer();
        mAudioPlayer.startPlayer();
    }

    private void PlayerPause(){
        if(PLATER_STATE == PLAYSTATE_PLAYING)
            mAudioPlayer.pausePlayer();
         PLATER_STATE = PLAYSTATE_PAUSED;
    }

    private void PlayerStop(){
        if(mAudioPlayer != null)
            mAudioPlayer.stopPlayer();
        PLATER_STATE = PLAYSTATE_STOPPED;
    }

    private void PlayerPlay(byte[] Wavbytes){
        int buffersize = mAudioPlayer.getBufferSize();
        byte[] buffer;
        int PlayedBytes = 0;
        int endBytes = 0;
        if(Wavbytes.length <= buffersize)
        {
            mAudioPlayer.play(Wavbytes, 0, Wavbytes.length);
        } else{
            while (PlayedBytes < Wavbytes.length){
                endBytes = PlayedBytes + buffersize;
                if(endBytes > Wavbytes.length) endBytes = Wavbytes.length;
                buffer = Arrays.copyOfRange(Wavbytes,PlayedBytes,endBytes);
                mAudioPlayer.play(buffer, 0, buffer.length);
                PlayedBytes = endBytes;
            }
        }
        PLATER_STATE = PLAYSTATE_PLAYING;
    }
}
