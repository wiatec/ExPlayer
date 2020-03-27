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

    // 播放器出错
    int PLAY_STATE_ERROR = -1;
    // 播放器未开始
    int PLAY_STATE_IDLE = 0x001;
    // 播放器准备中
    int PLAY_STATE_PREPARING = 0x002;
    // 播放器准备就绪
    int PLAY_STATE_PREPARED = 0x003;
    // 播放器播放中
    int PLAY_STATE_PLAYING = 0x004;
    // 播放器暂停
    int PLAY_STATE_PAUSED = 0x005;
    // 播放中数据不足开始缓冲
    int PLAY_STATE_BUFFERING = 0x006;
    // 播放完成
    int PLAY_STATE_COMPLETED = 0x007;

    void init(Context context, int engine);
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
