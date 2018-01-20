package com.ulfben.spaceshooter;
//Created by Ulf Benjaminsson (ulfben) on 2018-01-20.

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
public class Star extends Entity {
    public Star(){
        super( mDice.nextInt(Game.STAGE_WIDTH),
                mDice.nextInt(Game.STAGE_HEIGHT));
        mWidth = 10;
        mHeight = 10;
        mVelocityX = -mDice.nextFloat();
    }

    @Override
    public void render(final Canvas canvas, final Paint paint){
        paint.setColor(Color.WHITE);
        canvas.drawCircle(mX, mY, mWidth, paint);
    }
}
