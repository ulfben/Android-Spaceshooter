package com.ulfben.spaceshooter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

//Created by Ulf Benjaminsson (ulfben) on 2018-01-19.
/*
*   Game - for rendering and managing the core loop
*       Entity list

  *   Player -> Entity
  *   Enemy -> Entity
*     Star -> Entity
*
* */
public class Game extends SurfaceView implements Runnable{
    public static final String TAG = "Game";
    private boolean mIsRunning = false;
    private Thread mGameThread = null;
    public static final int STAGE_WIDTH = 1280;
    public static final int STAGE_HEIGHT = 720;
    public static final int STAR_COUNT = 64;
    private SurfaceHolder mHolder;
    private Paint mPaint;
    private Canvas mCanvas;

    private ArrayList<Entity> mEntities = new ArrayList<>();

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
        if(!mHolder.getSurface().isValid()){
           return;
        }
        mCanvas = mHolder.lockCanvas();
        if(mCanvas == null){
            return;
        }
        mCanvas.drawColor(Color.BLACK); //clear buffer

        for (final Entity e : mEntities) {
            e.render(mCanvas, mPaint);
        }

        mHolder.unlockCanvasAndPost(mCanvas);
    }

    @Override
    public void run() {
        while(mIsRunning){
            input();
            update();
            //collision check / collision reaction
            //check win / loss
            render();
            //limit framerate / sleep thread
        }
    }
}
