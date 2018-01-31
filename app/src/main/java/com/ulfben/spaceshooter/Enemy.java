package com.ulfben.spaceshooter;
//Created by Ulf Benjaminsson (ulfben) on 2018-01-25.

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
public class Enemy extends Entity {
    public static final String TAG = "Enemy";
    public static final float ENEMY_MAX_SPEED = 6;
    public static final int ENEMY_HEIGHT = 72; //pixels
    protected float mPlayerSpeed = 0.0f;
    Bitmap mBitmap;

    public Enemy(Context context){
        super();
        int select = mDice.nextInt(3);
        int resourceId = R.drawable.tm_2;
        switch(select){
            case 0:
                resourceId = R.drawable.tm_2;
                break;
            case 1:
                resourceId = R.drawable.tm_3;
                break;
            case 2:
                resourceId = R.drawable.tm_4;
                break;
            default:
                Log.w(TAG, "Enemy resource ID out of range. Rolled: "+select);
                break;
        }
        Bitmap temp = BitmapFactory.decodeResource(context.getResources(), resourceId);
        mBitmap = Utils.scaleToTargetHeight(temp, ENEMY_HEIGHT);
        mBitmap = Utils.flipBitmap(mBitmap, false);
        if(temp != mBitmap){
            temp.recycle();
        }
        respawn();
    }

    public void respawn(){
        mHeight = mBitmap.getHeight();
        mWidth = mBitmap.getWidth();
        mY = mDice.nextInt(Game.STAGE_HEIGHT- (int) mHeight);
        mX = Game.STAGE_WIDTH + mDice.nextInt((int)mWidth);
        mVelocityX = -1 + -mDice.nextInt((int) ENEMY_MAX_SPEED);
    }

    @Override
    public void onCollision(Entity that){
        respawn();
    }

    @Override
    public void input(Game game){
        mPlayerSpeed = game.getPlayerSpeed() * -1;
    }

    @Override
    public void update(){
        super.update();
        mX += mPlayerSpeed;
    }

    @Override
    public void worldWrap(final float width, final float height){
        if(mX < -mWidth){
            respawn();
        }
    }

    @Override
    public void render(final Canvas canvas, final Paint paint) {
        canvas.drawBitmap(mBitmap, mX, mY, paint);
    }
}