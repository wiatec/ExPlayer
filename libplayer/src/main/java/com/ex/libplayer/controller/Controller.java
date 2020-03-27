package com.ex.libplayer.controller;

import com.ex.libplayer.entities.ExPlayInfo;
import com.ex.libplayer.view.ExPlayView;

public interface Controller {

    /**
     * play view与controller绑定
     */
    void onBindPlayView(ExPlayView playView);

    /**
     * 播放信息改变回调
     */
    void onPlayInfoChanged(ExPlayInfo info);
}
