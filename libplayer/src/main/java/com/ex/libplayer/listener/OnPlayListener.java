package com.ex.libplayer.listener;

import com.ex.libplayer.enu.EnumPlayStatus;

public interface OnPlayListener {

    void onPlayerSizeChanged(int videoWidth, int videoHeight);
    void onPlayerEngineChanged(int mode);
    void onPlayerStatusChanged(EnumPlayStatus status);
}
