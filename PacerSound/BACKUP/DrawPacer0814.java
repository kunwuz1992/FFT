package com.example.kw.pacersound.alg.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.example.kw.pacersound.R;
import com.example.kw.pacersound.alg.wav.AudioPlayer;
import com.example.kw.pacersound.recvdata.PACER;
import com.example.kw.pacersound.recvdata.PacerPattern;


public class DrawPacer0814 extends View implements Runnable {
    /**
     * Pacer
     */
    private RectF pacerRect;

    private Paint mPaint = new Paint();

    private DisplayMetrics dm;
    /**
     * zoom rate,pacer percent patter 100% of the value
     */
    private float scalePacer = 0.0f;
    private float maxValue=100f;
    /** current pacer height value */
    private int pacerAheadValue=0;
    private int pacerRefreshTime=100;
    private int pacerValue = 0;
    private int pacerIndex =0;
    protected boolean stop = false;
    protected boolean pause = false;
    private boolean inHalse = false;
    private byte[] SoundBuffer;
    //sound
    public AudioPlayer audioPlayer;
    public volatile boolean mIsTestingExit = true;


    public DrawPacer0814(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrawPacer0814(Context context, AttributeSet attrs,
                         int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public DrawPacer0814(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        WindowManager wmManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        dm = new DisplayMetrics();
        wmManager.getDefaultDisplay().getMetrics(dm);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(dm.density * 3);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        pacerRect = new RectF(0, 0, w, h);
        scalePacer = pacerRect.height() / maxValue;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pacerRect != null) {
            mPaint.setColor(getResources().getColor(R.color.color_main_yellow));
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(pacerRect, mPaint);
            DrawPacer(canvas);
        }
    }

    private void DrawPacer(Canvas canvas) {
        if(inHalse) {
            mPaint.setColor(getResources().getColor(R.color.color_main_red));
        }else
        {
            mPaint.setColor(getResources().getColor(R.color.color_main_green));
        }
        mPaint.setStyle(Paint.Style.FILL);
        if(pacerValue<0){pacerValue=0;}else if(pacerValue>maxValue){ pacerValue = (int)maxValue;}
        float topValue=getPacer(pacerValue);
        if(topValue<pacerRect.top + 5 ){topValue = pacerRect.top + 5;}
        else if(topValue> pacerRect.bottom - 5 ){topValue=pacerRect.bottom -5;}
        canvas.drawRect(pacerRect.left + 5, topValue, pacerRect.right - 5,
                pacerRect.bottom - 5, mPaint);
        if(inHalse)
            mPaint.setColor(Color.rgb(0xff, 0xff, 0xff));
        else
            mPaint.setColor(Color.rgb(0xff, 0x00, 0x00));
        mPaint.setTextSize(48f);
        int pacerValueTemp = inHalse?pacerValue:(100 - pacerValue);
        canvas.drawText(String.valueOf(pacerValueTemp),15,88, mPaint);
        canvas.drawText(inHalse?"吸气":"呼气",15,158, mPaint);
        canvas.drawText("Pacer",15,228, mPaint);
    }
    /**
     * calculate pacer height
     */
    private float getPacer(int d) {
        return pacerRect.bottom - scalePacer * d;
    }

    private void getCurrentPacerValue()
    {
        try {
            PacerPattern.PACERS data = PacerPattern.PACEPATTERN.get(PacerPattern.PACEPATTERNUSED);
            if (pacerIndex >= data.getPacers().size()) {
                pacerIndex = 0;
            }
            PACER pacer = data.getPacers().get(pacerIndex);
            inHalse =pacer.isInHallsing();
            if (inHalse) {
                pacerValue = pacerAheadValue + pacer.getPercentage() * pacer.getMmSecUsed() / pacerRefreshTime;
                if (pacerValue > 100) {
                    pacerValue = 100;
                }
                if(pacer.getPercentage()>0) SoundBuffer = PacerPattern.PACERWAV.get(pacerIndex);
            } else {
                pacerValue = pacerAheadValue - pacer.getPercentage() * pacer.getMmSecUsed() / pacerRefreshTime;
                if (pacerValue < 0) {
                    pacerValue = 0;
                }
                if(pacer.getPercentage()>0 && pacerIndex!=PacerPattern.PACERWAV.size())
                    SoundBuffer = PacerPattern.PACERWAV.get(pacerIndex);
            }
            Thread.sleep(pacer.getMmSecUsed());
            pacerAheadValue = pacerValue;
            pacerIndex ++;
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    @Override
    public void run() {
        synchronized (this) {
            while (!stop) {
                try {
                    if (pause) {
                        this.wait();
                    }
                    if (PacerPattern.PACEPATTERNUSED >= 0) {
                        getCurrentPacerValue();
                        PlaySound(SoundBuffer);
                        postInvalidate();
                        Thread.sleep(20);

                    } else {
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void Stop() {
        this.stop = true;
        StopPlay();
    }

    public void Pause() {
        this.pause = true;
    }

    public boolean isPause() {
        return this.pause;
    }

    public boolean isStop() {
        return this.stop;
    }

    public synchronized void Continue() {
        this.pause = false;
        this.notify();
    }

    /*-----------------------------------------------------*/
    public void StartPlay(){
        audioPlayer = new AudioPlayer();
        audioPlayer.startPlayer();
        mIsTestingExit = false;
    }

    public void StopPlay(){
        mIsTestingExit = true;
        if(audioPlayer != null)
            audioPlayer.stopPlayer();
    }

    private void PlaySound(byte[] buffer){
        if(!mIsTestingExit){
            audioPlayer.play(buffer, 0, buffer.length);
        }
    }
}
