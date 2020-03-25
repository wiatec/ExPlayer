package com.ex.libplayer.engine;

import android.content.Context;
import android.view.SurfaceView;

import com.ex.libplayer.listener.OnPlayListener;

import java.util.Map;

public interface Engine {

    void init(Context context);

    void setDisplay(SurfaceView surfaceView);

    void setUrl(String url);

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

    void setListener(OnPlayListener onPlayListener);
    void setHeaders(Map<String, String> headers);
    void setVolume(float volume);
    boolean isPlaying();
}
