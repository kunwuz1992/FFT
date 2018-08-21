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
import com.example.kw.pacersound.alg.PlayThread;
import com.example.kw.pacersound.alg.draw.DrawPacer;
import com.example.kw.pacersound.alg.wav.AudioPlayer;
import com.example.kw.pacersound.recvdata.PacerPattern;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private Spinner mTestSpinner;
    private EditText InputReFs;

    //Pacer
    private PacerPattern pacerPattern;
    private Thread drawPacerThread;
    private PlayThread playThread;
    private DrawPacer drawPacerRect;
    private AudioPlayer mAudioPlayer;
    private Handler playHandler;

    private int rs = 6;

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
        findViewById(R.id.btnOpenPacer).setOnClickListener(ctrlListener);
        findViewById(R.id.btnOpenPacerSound).setOnClickListener((ctrlListener));
        findViewById(R.id.btnSetRS).setOnClickListener((ctrlListener));
        findViewById(R.id.textCurrentBpm).setOnClickListener((ctrlListener));
        findViewById(R.id.btnGen).setOnClickListener((ctrlListener));
        findViewById(R.id.btnTest).setOnClickListener((ctrlListener));
        drawPacerRect = (DrawPacer) findViewById(R.id.realpaly_draw_pacer);
        // initialize audio player
        PlayerInit();
        drawPacerRect.setHandler(playHandler);
    }

    private OnClickListener ctrlListener = new View.OnClickListener() {
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
                    playThread.Pause();
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
                    playThread.Pause();
                    Button btnOpenPacer = (Button) findViewById(R.id.btnOpenPacer);
                    btnOpenPacer.setText(getResources().getString(R.string.app_main_pacer_on));
                    Button btnOpenPacerSound = (Button) findViewById(R.id.btnOpenPacerSound);
                    btnOpenPacerSound.setText(getResources().getString(R.string.app_main_pacer_sound_on));
                }
            } else if (v.getId() == R.id.btnGen){
                testPlayer();
                Toast.makeText(v.getContext(), "已生成，可以开始测试", Toast.LENGTH_SHORT).show();
            } else if (v.getId() == R.id.btnTest){
                playTest();
                Toast.makeText(v.getContext(), "播放结束", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /*--------------------------Pacer--------------------------------------------------------------------*/


    /*--------------------------Pacer--------------------------------------------------------------------*/
    private void OpenDrawingPacer(boolean pacerOn)
    {
        if(pacerOn) {
            //Set pacer bpm
            int buffersize = mAudioPlayer.getBufferSize();
            PacerGenerator pg = new PacerGenerator(this,rs,(int)buffersize);
            pg.generation();
            if(pacerPattern==null)
                pacerPattern= new PacerPattern();
            pacerPattern.PACEPATTERNUSED = 0;
            pacerPattern.AddPacers(0, pg.getPacerlist());
            playThread.setInWav(pg.getInHaust());
            playThread.setExWav(pg.getExHaust());
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
                pacerPattern.PACERSOUNDUSED = -1;
            }
            return true;
        }
    }

    private void PlayerInit(){
        if(mAudioPlayer == null)
            mAudioPlayer = new AudioPlayer();
        mAudioPlayer.startPlayer();

        playThread = new PlayThread(mAudioPlayer);
        playThread.start();

        playHandler = new Handler(playThread.getmLooper()){
            @Override
            public void handleMessage(Message msg) {
                int state = msg.getData().getInt("PacerState");
                int value = msg.getData().getInt("PacerValue");
                if(pacerPattern.PACERSOUNDUSED >= 0){
                    playThread.updatePlayer(state,value);
                    playThread.playBuffer();
                } else {
                    if(state!= 0)
                        playThread.setSoundIndex(value, state);
                }
            }
        };
    }

    private List<byte[]> testWav;

    private void testPlayer(){
        int buffersize = mAudioPlayer.getBufferSize();
        PacerGenerator pg = new PacerGenerator(this,rs,(int)buffersize);
        pg.generation();
        testWav = new ArrayList<byte[]>();
        testWav.addAll(pg.getInHaust());
    }

    private void playTest(){
        mAudioPlayer.startPlayer();
        for(byte[] buffer:testWav)
            mAudioPlayer.play(buffer,0,buffer.length);
        mAudioPlayer.stopPlayer();
    }
}
