package com.ulfben.spaceshooter;
//Created by Ulf Benjaminsson (ulfben) on 2018-01-20.

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
public class Utils {
    public static float wrap(float val, final float min, final float max){
        if(val < min){
            val = max;
        }else if(val > max){
            val = min;
        }
        return val;
    }

    public static Bitmap flipBitmap(Bitmap source, boolean horizontally){
        Matrix matrix = new Matrix();
        int cx = source.getWidth()/2;
        int cy = source.getHeight()/2;
        if(horizontally){
            matrix.postScale(1, -1, cx, cy);
        }else{
            matrix.postScale(-1, 1, cx, cy);
        }
        return Bitmap.createBitmap(source, 0, 0,
                    source.getWidth(), source.getHeight(),
                    matrix, true);
    }

    public static Bitmap scaleToTargetHeight(Bitmap source, int height){
        float ratio = height / (float) source.getHeight();
        int newHeight = (int) (source.getHeight() * ratio);
        int newWidth = (int) (source.getWidth() * ratio);
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }
}
