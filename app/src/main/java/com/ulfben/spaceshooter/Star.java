package com.ulfben.spaceshooter;
//Created by Ulf Benjaminsson (ulfben) on 2018-01-20.

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
public class Star extends Entity {
    protected static final float STAR_SIZE = 5;
    protected float mPlayerSpeed = 0.0f;
    public Star(){
        super( mDice.nextInt(Game.STAGE_WIDTH),
                mDice.nextInt(Game.STAGE_HEIGHT));
        mWidth = STAR_SIZE;
        mHeight = STAR_SIZE;
        mVelocityX = -mDice.nextFloat();
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
    public void render(final Canvas canvas, final Paint paint){
        paint.setColor(Color.WHITE);
        canvas.drawCircle(mX, mY, mWidth, paint);
    }
}
