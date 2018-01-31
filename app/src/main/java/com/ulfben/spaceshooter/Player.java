package com.ulfben.spaceshooter;
//Created by Ulf Benjaminsson (ulfben) on 2018-01-26.

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public class Player extends Entity {
    public static final String TAG = "Player";
    public static final int MAX_HEALTH = 3;
    public static final int MIN_SPEED = 1;
    public static final int MAX_SPEED = 20;
    public static final float ACCEL = 2;
    public static final int GRAVITY = 6;
    public static final int HEIGHT = 72;

    private boolean mIsBoosting = false;
    private Bitmap mBitmap;
    private int mHealth = MAX_HEALTH;

    public Player(Context context){
        super();
        Bitmap temp = BitmapFactory.decodeResource(context.getResources(), R.drawable.tm_1);
        mBitmap = Utils.scaleToTargetHeight(temp, HEIGHT);
        respawn();
    }

    @Override
    public void input(Game game){
        mIsBoosting = game.isBoosting();
    }

    public void respawn(){
        mHeight = mBitmap.getHeight();
        mWidth = mBitmap.getWidth();
        mY = Game.STAGE_HEIGHT/2 - mHeight/2;
        mX = 10;
        mIsBoosting = false;
        mVelocityX = MIN_SPEED;
        mHealth = MAX_HEALTH;
    }

    @Override
    public void update(){
        if(mIsBoosting){
            mVelocityX += ACCEL;
            mVelocityY -= ACCEL;
        }else{
            mVelocityX -= ACCEL;
            mVelocityY += ACCEL;
        }
        mVelocityX = Utils.clamp(mVelocityX, MIN_SPEED, MAX_SPEED);
        mVelocityY = Utils.clamp(mVelocityY, -MAX_SPEED, GRAVITY);
        mY += mVelocityY;
        mY += GRAVITY;
        mY = Utils.clamp(mY, 0, Game.STAGE_HEIGHT-mHeight);
    }

    @Override
    public void onCollision(Entity that){
        if(!(that instanceof Enemy)){
            return;
        }
        mHealth--;
        if(mHealth < 0){
            //TODO: game over man!
            Log.d(TAG, "Player is dead");
        }
    }

    @Override
    public void render(final Canvas canvas, final Paint paint) {
        canvas.drawBitmap(mBitmap, mX, mY, paint);
    }

    public int getHealth(){return mHealth;}
}
