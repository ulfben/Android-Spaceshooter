package com.ulfben.spaceshooter;
//Created by Ulf Benjaminsson (ulfben) on 2018-01-31.

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
public class JukeBox {
    public static final String TAG = "JukeBox";
    public static final int MAX_STREAMS = 4;
    private SoundPool mSoundPool;
    public static int CRASH = 0;

    public JukeBox(Context context){
        mSoundPool = createSoundPool();
        loadSFX(context.getAssets());
    }

    public void play(int id){
        float leftVolume = 1.0f;
        float rightVolume = 1.0f;
        int priority = 1;
        int loop = 0; //-1 = play forever, 0 = no loop, play once, 1, play twice
        float rate = 1.0f; //goes between 0.5f - 2.0f
        mSoundPool.play(id, leftVolume, rightVolume, priority, loop, rate);
    }

    public void destroy(){
        mSoundPool.release();
        mSoundPool = null;
    }

    private void loadSFX(AssetManager assetManager){
        try{
            AssetFileDescriptor descriptor;
            descriptor = assetManager.openFd("crash.wav");
            CRASH = mSoundPool.load(descriptor, 0);
        }catch(IOException e){
            Log.e(TAG, "Error loading SFX " + e.toString());
        }
    }

    private SoundPool createSoundPool(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            return createNewSoundPool();
        }
        return createOldSoundPool();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private SoundPool createNewSoundPool(){
        AudioAttributes attr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        return new SoundPool.Builder()
                .setAudioAttributes(attr)
                .setMaxStreams(MAX_STREAMS)
                .build();
    }

    @SuppressWarnings("deprecation")
    private SoundPool createOldSoundPool(){
        return new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
    }
}
