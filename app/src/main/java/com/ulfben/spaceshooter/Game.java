package com.ulfben.spaceshooter;
import android.content.Context;
import android.content.SharedPreferences;
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
    public static final String PREFS = "com.ulfben.spaceshooter";
    public static final String LONGEST_DIST = "longest_distance";

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
    private SharedPreferences mPrefs;
    private SurfaceHolder mHolder;
    private Paint mPaint;
    private Canvas mCanvas;
    private ArrayList<Entity> mEntities = new ArrayList<>();
    private Player mPlayer;
    private boolean mIsBoosting = false;

    private boolean mGameOver = false;
    private long mDistanceTraveled = 0;
    private long mLongestDistanceTraveled = 0;

    //TODO: make Frame class
    //FPS-readout / rate limiting
    private long mLastSampleTime = 0;
    private long mFrameCount = 0;
    private float mAvgFramerate = 0f;

    private JukeBox mJukeBox;

    public Game(final Context context){
        super(context);
        mJukeBox = new JukeBox(context);
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
        mPrefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        startGame();
    }

    private void startGame(){
        mPlayer.respawn();
        for(final Entity e : mEntities){
            e.respawn();
        }
        mGameOver = false;
        mDistanceTraveled = 0;
        mLongestDistanceTraveled = mPrefs.getLong(LONGEST_DIST, 0);
        mJukeBox.play(JukeBox.CRASH);
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
    public void onDestroy() {
        mJukeBox.destroy();
    }

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
        //TODO: pick better win-condition, or balance this
            //time alive? enemies overtaken?
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

    //TODO: make UI class
    private void drawGameOverHUD(){
        float scaleFactor = (float) Math.sqrt(mCanvas.getWidth() * mCanvas.getHeight()) / 250; //250 is arbitrary!
        int sizeInPixels = 30;
        float scaledSize = sizeInPixels * scaleFactor;
        mPaint.setTextSize(scaledSize);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        String result = "You traveled " + mDistanceTraveled + " km!";
        if(mDistanceTraveled > mLongestDistanceTraveled) {
            result = "New record: " + mDistanceTraveled + "!";
        }
        //TODO: more notes needed: "GAME OVER!", "Tap screen to restart!"
        mCanvas.drawText(result, STAGE_WIDTH/2, (STAGE_HEIGHT/2)-scaledSize, mPaint);
    }
    //TODO: make UI class
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

    //TODO: maybe use SurfaceHolder callbacks to avoid this
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
            long start = System.nanoTime(); //TODO: make Frame class
            onEnterFrame(); //TODO: make Frame class
            input();
            update();
            checkCollisions();
            checkForGameOver();
            render();
            rateLimit(start); //TODO: make Frame class
        }
    }

    private void checkForGameOver(){
        if(mGameOver){
            return;
        }
        mGameOver = (mPlayer.getHealth() < 0);
        if(mDistanceTraveled > mLongestDistanceTraveled){
            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putLong(LONGEST_DIST, mDistanceTraveled);
            edit.apply();
        }
    }

    //TODO: make Frame class
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

    //TODO: make Frame class
    private void onEnterFrame(){
        mFrameCount++;
        long timeSinceLast = System.nanoTime()-mLastSampleTime;
        if(timeSinceLast < SAMPLE_INTERVAL) {
            return;
        }
        mAvgFramerate = mFrameCount / (timeSinceLast* NANOS_TO_SECONDS);
        mLastSampleTime = System.nanoTime();
        mFrameCount = 0;
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
