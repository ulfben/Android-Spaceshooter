package com.ulfben.spaceshooter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class Game extends SurfaceView implements Runnable{
    public static final String TAG = "Game";
    private boolean mIsRunning = false;
    private Thread mGameThread = null;
    public static final int STAGE_WIDTH = 1920;
    public static final int STAGE_HEIGHT = 1080;
    public static final int STAR_COUNT = 64;
    private SurfaceHolder mHolder;
    private Paint mPaint;
    private Canvas mCanvas;

    private ArrayList<Entity> mEntities = new ArrayList<>();

    public static final long SECONDS_TO_NANOS = 1000000000;
    public static final long MILLIS_TO_NANOS = 1000000;
    public static final float NANOS_TO_MILLIS = 1.0f / MILLIS_TO_NANOS;
    public static final float NANOS_TO_SECONDS = 1.0f / SECONDS_TO_NANOS;
    public static final long TARGET_FRAMERATE = 60;
    public static final long MS_PER_FRAME = 1000/TARGET_FRAMERATE;
    public static final long NANOS_PER_FRAME = MS_PER_FRAME * MILLIS_TO_NANOS;
    public static final long SAMPLE_INTERVAL = (long) (1* SECONDS_TO_NANOS);
    private long mLastSampleTime = 0;
    private long mFrameCount = 0;
    private float mAvgFramerate = 0f;

    public Game(final Context context){
        super(context);
        mHolder = getHolder();
        mHolder.setFixedSize(STAGE_WIDTH, STAGE_HEIGHT);
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        for(int i = 0; i < STAR_COUNT; i++){
            mEntities.add(new Star());
        }
    }

    public void onResume() {
        Log.d(TAG, "OnResume");
        mIsRunning = true;
        mGameThread = new Thread(this);
        mGameThread.start();
    }
    public void onPause() {
        Log.d(TAG, "OnPause");
        mIsRunning = false;
        try{
            mGameThread.join();
        } catch (InterruptedException e) {
            Log.d(TAG, Log.getStackTraceString(e.getCause()));
        }
    }
    public void onDestroy() {}

    private void input(){
        //poll input systems
        for (final Entity e : mEntities) {
            e.input();
        }
    }
    private void update(){
        for (final Entity e : mEntities) {
            e.update();
            e.worldWrap(STAGE_WIDTH, STAGE_HEIGHT);
        }
    }
    private void render(){
        if(!lockAndAcquireCanvas()) {
            return;
        }
        mCanvas.drawColor(Color.BLACK); //clear buffer

        for (final Entity e : mEntities) {
            e.render(mCanvas, mPaint);
        }

        float scaleFactor = (float) Math.sqrt(mCanvas.getWidth() * mCanvas.getHeight()) / 250; //250 is arbitrary!
        int sizeInPixels = 20;
        float scaledSize = sizeInPixels * scaleFactor;
        mPaint.setTextSize(scaledSize);
        mPaint.setColor(Color.YELLOW);
        mCanvas.drawText("FPS: " + mAvgFramerate, 10, (int)scaledSize, mPaint);

        mHolder.unlockCanvasAndPost(mCanvas);
    }

    private boolean lockAndAcquireCanvas() {
        if(!mHolder.getSurface().isValid()){
            return false;
        }
        mCanvas = mHolder.lockCanvas();
        return (mCanvas != null);
    }

    @Override
    public void run() {
        while(mIsRunning){
            long start = System.nanoTime();
            onEnterFrame();
            input();
            update();
            //collision check / collision reaction
            //check win / loss
            render();

            //limit framerate / sleep thread
            //TODO: refactor
            float millisRemaining = ((start + NANOS_PER_FRAME) - System.nanoTime()) * NANOS_TO_MILLIS;
            if(millisRemaining > 1) {
                try {
                    Thread.sleep((long) millisRemaining);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //TODO: make class
    private void onEnterFrame(){
        mFrameCount++;
        long timeSinceLast = System.nanoTime()-mLastSampleTime;
        if(timeSinceLast < SAMPLE_INTERVAL) {
            return;
        }
        mAvgFramerate = mFrameCount / (timeSinceLast* NANOS_TO_SECONDS);
        mLastSampleTime = System.nanoTime();
        mFrameCount = 0;
        Log.d(TAG, "FPS: " + mAvgFramerate);
    }
}
