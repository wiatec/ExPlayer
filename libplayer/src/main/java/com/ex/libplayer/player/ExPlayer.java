package com.ex.libplayer.player;

import android.content.Context;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.ex.libplayer.Constant;
import com.ex.libplayer.engine.Engine;
import com.ex.libplayer.engine.EngineExo;
import com.ex.libplayer.engine.EngineIjk;
import com.ex.libplayer.engine.EngineNative;
import com.ex.libplayer.engine.EngineVlc;
import com.ex.libplayer.listener.OnPlayListener;
import com.ex.libplayer.util.ExUtils;

import java.util.Map;

public class ExPlayer implements Player {

    private Engine mEngine;

    @Override
    public void init(Context context, int engine, boolean isLive) {
        if(engine == Player.ENGINE_NATIVE){
            Log.d(Constant.c.TAG, "init native engine");
            mEngine = new EngineNative();
            mEngine.init(context, isLive);
        }else if(engine == Player.ENGINE_VLC){
            Log.d(Constant.c.TAG, "init vlc engine");
            mEngine = new EngineVlc();
            mEngine.init(context, isLive);
        }else if(engine == Player.ENGINE_IJK){
            Log.d(Constant.c.TAG, "init ijk engine");
            mEngine = new EngineIjk();
            mEngine.init(context, isLive);
        }else if(engine == Player.ENGINE_EXO){
            Log.d(Constant.c.TAG, "init exo engine");
            mEngine = new EngineExo();
            mEngine.init(context, isLive);
        }
    }

    @Override
    public void setUrl(String url) {
        if(mEngine != null) {
            mEngine.setUrl(url);
        }
    }

    @Override
    public void setListener(OnPlayListener onPlayListener) {
        if(mEngine != null) {
            mEngine.setListener(onPlayListener);
        }
    }

    @Override
    public void setDisplay(Surface surface, TextureView textureView) {
        if(mEngine != null) {
            mEngine.setDisplay(surface, textureView);
        }
    }

    @Override
    public void start() {
        if(mEngine != null) {
            mEngine.start();
        }
    }

    @Override
    public void restart() {
        if(mEngine != null) {
            mEngine.restart();
        }
    }

    @Override
    public void resume() {
        if(mEngine != null) {
            mEngine.resume();
        }
    }

    @Override
    public void pause() {
        if(mEngine != null) {
            mEngine.pause();
        }
    }

    @Override
    public void stop() {
        if(mEngine != null) {
            mEngine.stop();
        }
    }

    @Override
    public void release() {
        if(mEngine != null) {
            mEngine.release();
        }
    }

    @Override
    public void seekTo(float duration) {
        if(mEngine != null) {
            mEngine.seekTo(duration);
        }
    }

    @Override
    public void rewind(float duration) {
        if(mEngine != null) {
            mEngine.rewind(duration);
        }
    }

    @Override
    public void forward(float duration) {
        if(mEngine != null) {
            mEngine.forward(duration);
        }
    }

    @Override
    public float getCurrentDuration() {
        if(mEngine != null) {
            return mEngine.getCurrentDuration();
        }
        return 0f;
    }

    @Override
    public float getPlayableDuration() {
        if(mEngine != null) {
           return mEngine.getPlayableDuration();
        }
        return 0f;
    }

    @Override
    public float getTotalDuration() {
        if(mEngine != null) {
            return mEngine.getTotalDuration();
        }
        return 0f;
    }

    @Override
    public String getCurrentTime() {
        return ExUtils.formatMediaTime((long)getCurrentDuration());
    }

    @Override
    public String getTotalTime() {
        return ExUtils.formatMediaTime((long)getTotalDuration());
    }

    @Override
    public int getProgress() {
        Float progress = (getCurrentDuration() / getTotalDuration()) * 100;
        return progress.intValue();
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        if(mEngine != null) {
            mEngine.setHeaders(headers);
        }
    }

    @Override
    public void setVolume(float volume) {
        if(mEngine != null) {
            mEngine.setVolume(volume);
        }
    }

    @Override
    public boolean isPlaying() {
        return mEngine != null && mEngine.isPlaying();
    }

}
