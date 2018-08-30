package com.example.kw.pacersound;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
    private List<byte[]> inWav = new ArrayList<>();
    private List<byte[]> exWav = new ArrayList<>();

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TEST_PROGRAM_ARRAY);
        mTestSpinner.setAdapter(adapter);
//        String path = "/assets/test.wav";
        findViewById(R.id.btnOpenPacer).setOnClickListener(ctrlListener);
        findViewById(R.id.btnOpenPacerSound).setOnClickListener((ctrlListener));
        findViewById(R.id.btnSetRS).setOnClickListener((ctrlListener));
        findViewById(R.id.textCurrentBpm).setOnClickListener((ctrlListener));
        findViewById(R.id.btnGen).setOnClickListener((ctrlListener));
        findViewById(R.id.btnTest).setOnClickListener((ctrlListener));
        findViewById(R.id.btnStop).setOnClickListener((ctrlListener));
        drawPacerRect = (DrawPacer) findViewById(R.id.realpaly_draw_pacer);
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
                    playThread.Pause();
                }
            } else if (v.getId() == R.id.btnOpenPacerSound) {
                Button btnOpenPacerSound = (Button) findViewById(R.id.btnOpenPacerSound);
                boolean toOpen = (btnOpenPacerSound.getText().equals(getResources().getString(R.string.app_main_pacer_sound_on)));
                boolean PacerExist = OpenPacerSound(v.getContext(),toOpen);
                if(PacerExist) {
                    if (toOpen){
                        btnOpenPacerSound.setText(getResources().getString(R.string.app_main_pacer_sound_off));
                        playThread.Continue();
                    }
                    else{
                        btnOpenPacerSound.setText(getResources().getString(R.string.app_main_pacer_sound_on));
                        playThread.Pause();
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
//                    playThread.Pause();
                    Button btnOpenPacer = (Button) findViewById(R.id.btnOpenPacer);
                    btnOpenPacer.setText(getResources().getString(R.string.app_main_pacer_on));
                    Button btnOpenPacerSound = (Button) findViewById(R.id.btnOpenPacerSound);
                    btnOpenPacerSound.setText(getResources().getString(R.string.app_main_pacer_sound_on));
                }
            } else if (v.getId() == R.id.btnGen){
                testPlayer();
                Toast.makeText(v.getContext(), "已生成，可以开始测试", Toast.LENGTH_SHORT).show();
            } else if (v.getId() == R.id.btnTest){
//                playTest();
                isStop = false;
                Toast.makeText(v.getContext(), "播放开始", Toast.LENGTH_SHORT).show();
                playTest();
            } else if (v.getId() == R.id.btnStop){
                isStop = true;
                playThread.Stop();
                Toast.makeText(v.getContext(), "播放结束", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /*--------------------------Menu--------------------------------------------------------------------*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_PacerSettings) {
            showCustomizeDialog(MainActivity.this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*--------------------------Dialog--------------------------------------------------------------------*/
    private void showCustomizeDialog(final Context context) {
        /* @setView 装入自定义View ==> R.layout.dialog_customize
         * 由于dialog_customize.xml只放置了一个EditView，因此和图8一样
         * dialog_customize.xml可自定义更复杂的View
         */
        AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(context);
        final View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.icon_dialog_pacersetting,null);
        customizeDialog.setTitle("设置呼吸节拍器");
        customizeDialog.setView(dialogView);
        customizeDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CheckBox soundOn = dialogView.findViewById(R.id.checkBox_soundOn);
                        EditText edit_text = (EditText) dialogView.findViewById(R.id.edit_text);
                        rs = Integer.parseInt(edit_text.getText().toString());
                        if(soundOn.isChecked())
                            Toast.makeText(context, "打开声音", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(context, "关闭声音", Toast.LENGTH_SHORT).show();

                        //clear the exist pacer pattern
                        if(pacerPattern !=null) {
                            pacerPattern.Patternclear();
                            drawPacerRect.drawClear();
                            Button btnOpenPacer = (Button) findViewById(R.id.btnOpenPacer);
                            btnOpenPacer.setText(getResources().getString(R.string.app_main_pacer_on));
                            Button btnOpenPacerSound = (Button) findViewById(R.id.btnOpenPacerSound);
                            btnOpenPacerSound.setText(getResources().getString(R.string.app_main_pacer_sound_on));
                        }
                        //revise the text to show the current bpm
                        TextView currentRS = findViewById(R.id.textCurrentBpm);
                        currentRS.setText(String.valueOf(rs));
                    }
                });

        customizeDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        dialog.dismiss();
                    }
                });
        // 显示
        customizeDialog.show();
    }

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
            mAudioPlayer.setWavData(pg.getInArray(), pg.getExArray());
            if(inWav != null){
                inWav.clear();
                exWav.clear();
            }
            inWav.addAll(pg.getInHaust());
            exWav.addAll(pg.getExHaust());
        }else {
            if(pacerPattern!=null) pacerPattern.PACEPATTERNUSED = -1;
        }
        drawPacerThread = new Thread(drawPacerRect, "DrawPacerThread");
        drawPacerThread.start();
    }

    /*--------------------------Pacer Sound--------------------------------------------------------------------*/
    private boolean OpenPacerSound(Context context, boolean soundOn){
        if(pacerPattern==null)
        {
            Toast.makeText(context, "先请打开节拍器", Toast.LENGTH_SHORT).show();
            return false;
        } else{
            if(soundOn) {
                pacerPattern.PACERSOUNDUSED = 0;
                mAudioPlayer.startPlayer();
            }else {
                pacerPattern.PACERSOUNDUSED = -1;
            }
            playThread.setWav(inWav,exWav);
            return true;
        }
    }

    private void PlayerInit(){
        if(mAudioPlayer == null)
            mAudioPlayer = new AudioPlayer();
        mAudioPlayer.startPlayer();
        playThread = new PlayThread(mAudioPlayer);
        playThread.start();
        playHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                int state = msg.getData().getInt("PacerState");
                int value = msg.getData().getInt("PacerValue");
                playThread.updatePlayer(state, value);
            }

        };
        drawPacerRect.setHandler(playHandler);
    }


    /*---------------------------------------------------------------------------*/
    private List<byte[]> testWav;
    private byte[] testArray;
    private boolean isStop = false;

    private void testPlayer(){
        int buffersize = mAudioPlayer.getBufferSize();
        PacerGenerator pg = new PacerGenerator(this,rs,(int)buffersize);
        pg.generation();
        testWav = new ArrayList<byte[]>();
        testWav.addAll(pg.getInHaust());
        testArray = pg.getInArray();
    }

    private void playTest(){
        mAudioPlayer.startPlayer();
        mAudioPlayer.play(testArray,0,testArray.length);
    }
}
