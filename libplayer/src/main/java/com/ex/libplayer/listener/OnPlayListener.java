package com.ex.libplayer.listener;

public interface OnPlayListener {

    void onPlayerSizeChanged(int videoWidth, int videoHeight);
    void onPlayerEngineChanged(int mode);
    void onPlayerStatusChanged(int status);
}
