package com.ulfben.spaceshooter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class Game extends SurfaceView implements Runnable{
    public static final String TAG = "Game";
    //GAME SETTINGS
    public static final int STAGE_WIDTH = 1280;
    public static final int STAGE_HEIGHT = 720;
    public static final int STAR_COUNT = 64;
    public static final int ENEMY_COUNT = 6;
    public static final long TARGET_FRAMERATE = 60;

    //UNIT CONSTANTS
    public static final long SECONDS_TO_NANOS = 1000000000;
    public static final long MILLIS_TO_NANOS = 1000000;
    public static final float NANOS_TO_MILLIS = 1.0f / MILLIS_TO_NANOS;
    public static final float NANOS_TO_SECONDS = 1.0f / SECONDS_TO_NANOS;
    public static final long MS_PER_FRAME = 1000/TARGET_FRAMERATE;
    public static final long NANOS_PER_FRAME = MS_PER_FRAME * MILLIS_TO_NANOS;
    public static final long SAMPLE_INTERVAL = (long) (1 * SECONDS_TO_NANOS);

    private boolean mIsRunning = false;
    private Thread mGameThread = null;
    private SurfaceHolder mHolder;
    private Paint mPaint;
    private Canvas mCanvas;
    private ArrayList<Entity> mEntities = new ArrayList<>();
    private Player mPlayer;
    private boolean mIsBoosting = false;

    //FPS-readout / rate limiting
    private long mLastSampleTime = 0;
    private long mFrameCount = 0;
    private float mAvgFramerate = 0f;
    private boolean mGameOver = false;
    private long mDistanceTraveled = 0;
    private long mLongestDistanceTraveled = 0;

    //highscore (includes sharedprefs)
        //update the start screen
    //sound effects

    public Game(final Context context){
        super(context);
        mHolder = getHolder();
        mHolder.setFixedSize(STAGE_WIDTH, STAGE_HEIGHT);
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        for(int i = 0; i < STAR_COUNT; i++){
            mEntities.add(new Star());
        }
        for(int i = 0; i < ENEMY_COUNT; i++){
            mEntities.add(new Enemy(context));
        }
        mPlayer = new Player(context);
        startGame();
    }

    private void startGame(){
        mPlayer.respawn();
        for(final Entity e : mEntities){
            e.respawn();
        }
        mGameOver = false;
        mDistanceTraveled = 0;
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

    //Q&D: stand in for an InputManager interface
    //We cheat and simply pass a reference to the Game-object
    //into our entities input() method.
    public float getPlayerSpeed(){ return mPlayer.getVelocityX();}
    public boolean isBoosting(){ return mIsBoosting; }

    private void input(){
        mPlayer.input(this);
        for (final Entity e : mEntities) {
            e.input(this);
        }
    }
    private void update(){
        if(mGameOver){
            return;
        }
        mPlayer.update();
        for (final Entity e : mEntities) {
            e.update();
            e.worldWrap(STAGE_WIDTH, STAGE_HEIGHT);
        }
        mDistanceTraveled += mPlayer.getVelocityX();
    }

    private void checkCollisions(){
        int ic = mEntities.size();
        Entity temp;
        for(int i = 0; i < ic; i++){
            temp = mEntities.get(i);
            if(mPlayer.isColliding(temp)){
                temp.onCollision(mPlayer);
                mPlayer.onCollision(temp);
            }
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
        mPlayer.render(mCanvas, mPaint);
        if(mGameOver) {
            drawGameOverHUD();
        }else {
            drawHUD();
        }
        mHolder.unlockCanvasAndPost(mCanvas);
    }

    private void drawGameOverHUD(){
        float scaleFactor = (float) Math.sqrt(mCanvas.getWidth() * mCanvas.getHeight()) / 250; //250 is arbitrary!
        int sizeInPixels = 30;
        float scaledSize = sizeInPixels * scaleFactor;
        mPaint.setTextSize(scaledSize);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        String result = "You traveled " + mDistanceTraveled + " km!";
        //TODO: check if record was set, update GUI to show
            //notes needed: "GAME OVER!", "Tap screen to restart!"
        mCanvas.drawText(result, STAGE_WIDTH/2, (STAGE_HEIGHT/2)-scaledSize, mPaint);
    }

    private void drawHUD(){
        float scaleFactor = (float) Math.sqrt(mCanvas.getWidth() * mCanvas.getHeight()) / 250; //250 is arbitrary!
        int sizeInPixels = 10;
        float scaledSize = sizeInPixels * scaleFactor;
        mPaint.setTextSize(scaledSize);
        mPaint.setColor(Color.YELLOW);
        mCanvas.drawText("Health: " + mPlayer.getHealth(), 10, (int)scaledSize, mPaint);
        mCanvas.drawText("Speed: " + mPlayer.getVelocityX(), STAGE_WIDTH/2, (int)scaledSize, mPaint);
        mCanvas.drawText("FPS: " + mAvgFramerate, 10, STAGE_HEIGHT-scaledSize, mPaint);
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
            checkCollisions();
            checkForGameOver();
            render();
            rateLimit(start);
        }
    }

    private void checkForGameOver(){
        if(mGameOver){
            return;
        }
        mGameOver = (mPlayer.getHealth() < 0);
        if(mDistanceTraveled > mLongestDistanceTraveled){
            //TODO: save high score!
        }
    }

    private void rateLimit(long startOfFrame){
        float millisRemaining = ((startOfFrame + NANOS_PER_FRAME) - System.nanoTime()) * NANOS_TO_MILLIS;
        if(millisRemaining > 1) {
            try {
                Thread.sleep((long) millisRemaining);
            } catch (InterruptedException e) {
                e.printStackTrace();
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

    @Override
    public boolean onTouchEvent(final MotionEvent event){
        switch(event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_UP:
                mIsBoosting = false;
                if(mGameOver) {
                    startGame();
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mIsBoosting = true;
                break;
            default:
                //no action.
                break;
        }
        return true;
    }
}
