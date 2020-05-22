package com.ex.libplayer.player;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import com.ex.libplayer.listener.OnPlayListener;

import java.util.Map;

public interface Player {

    int ENGINE_NATIVE = 0x001;
    int ENGINE_VLC = 0x002;
    int ENGINE_IJK = 0x003;
    int ENGINE_EXO = 0x004;

    // 正常播放模式
    int PLAY_MODE_NORMAL = 0;
    // 全屏播放模式
    int PLAY_MODE_FULL_SCREEN = 1;
    // 悬浮窗播放模式
    int PLAY_MODE_TINY_WINDOW = 2;

    void init(Context context, int engine, boolean isLive);
    void setUrl(String url);
    void setListener(OnPlayListener onPlayListener);
    void setDisplay(Surface surface, TextureView textureView);

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
    String getCurrentTime();
    String getTotalTime();
    int getProgress();

    void setHeaders(Map<String, String> headers);
    void setVolume(float volume);
    boolean isPlaying();

}
