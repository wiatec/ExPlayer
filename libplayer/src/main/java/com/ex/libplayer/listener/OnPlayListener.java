package com.ex.libplayer.listener;

public interface OnPlayListener {

    void onPlayerPrepared();
    void onPlayerPlaying();
    void onPlayerBuffering();
    void onPlayerPause();
    void onPlayerPositionChanged();
    void onPlayerCompleted();
    void onPlayerError();
}
