package com.ex.libplayer.engine;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import com.ex.libplayer.listener.OnPlayListener;

import java.util.Map;

public interface Engine {

    void init(Context context);

    void setUrl(String url);
    void setDisplay(SurfaceView surfaceView);
    void setListener(OnPlayListener onPlayListener);
    void setHeaders(Map<String, String> headers);

    void start();
    void restart();
    void resume();
    void pause();
    void stop();
    void release();

    void seekTo(float duration);
    void rewind(float duration);
    void forward(float duration);
    float getCurrentDuration();
    float getPlayableDuration();
    float getTotalDuration();

    void setVolume(float volume);
    boolean isPlaying();
}
