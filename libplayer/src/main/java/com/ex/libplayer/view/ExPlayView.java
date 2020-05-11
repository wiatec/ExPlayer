package com.ex.libplayer.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ex.libplayer.Constant;
import com.ex.libplayer.controller.ExPlayerController;
import com.ex.libplayer.engine.Engine;
import com.ex.libplayer.engine.EngineExo;
import com.ex.libplayer.engine.EngineIjk;
import com.ex.libplayer.engine.EngineNative;
import com.ex.libplayer.engine.EngineVlc;
import com.ex.libplayer.entities.ExPlayInfo;
import com.ex.libplayer.enu.EnumPlayStatus;
import com.ex.libplayer.listener.OnPlayListener;
import com.ex.libplayer.player.Player;
import com.ex.libplayer.util.ExUtils;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * 为保持全屏效果正确，需要在AndroidManifest.xml中给使用此view的activity添加
 *   android:configChanges="orientation|keyboardHidden|screenSize"
 */
public class ExPlayView extends FrameLayout implements TextureView.SurfaceTextureListener, OnPlayListener {


    private Context context;
    // 存放视频播放器和控制器的容器
    private FrameLayout container;
    // 视频播放器
    private TextureView textureView;
    // 视频控制器
    private ExPlayerController controller;
    private SurfaceTexture surfaceTexture;

    // 当前使用的播放引擎类型
    private int engine = Player.ENGINE_NATIVE;
    // 播放引擎
    private Engine mEngine;
    private OnPlayListener onPlayListener;
    private float lastPlayPosition;

    private String url;
    private String title;
    private Map<String, String> headers;
    private int playMode = Player.PLAY_MODE_NORMAL;
    private EnumPlayStatus playStatus = EnumPlayStatus.IDLE;
    private boolean autoStartPlay;
    private boolean loop;
    private int jumpDuration = 20;

    public ExPlayView(@NonNull Context context) {
        this(context, null);
    }

    public ExPlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExPlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        headers = new HashMap<>();
        headers.put("User-Agent", Constant.header.USER_AGENT);
        initContainer();
    }

    private void initContainer() {
        this.container = new FrameLayout(context);
        this.container.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(this.container, params);
    }

    public void setDataSource(String url) {
        setDataSource(url, null, null);
    }

    public void setDataSource(String url, String title) {
        setDataSource(url, title, null);
    }

    public void setDataSource(String url, Map<String, String> headers) {
        setDataSource(url, null, headers);
    }

    public void setDataSource(String url, String title, Map<String, String> headers) {
        this.url = url;
        this.title = title;
        if(headers != null && headers.size() > 0) {
            this.headers.putAll(headers);
        }
    }

    /**
     * 设置播放控制器
     */
    public void setController(ExPlayerController controller) {
        this.controller = controller;
        controller.onBindPlayView(this);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.container.addView(this.controller, params);
    }

    public void prepare(){
        prepare(this.engine, true);
    }

    public void prepare(boolean autoStartPlay){
        prepare(this.engine, autoStartPlay);
    }

    public void prepare(int engine){
        prepare(engine, true);
    }

    public void prepare(int engine, boolean autoStartPlay){
        this.engine = engine;
        if(engine == Player.ENGINE_NATIVE){
            Log.d(Constant.c.TAG, "init native engine");
            mEngine = new EngineNative();
            mEngine.init(context);
        }else if(engine == Player.ENGINE_VLC){
            Log.d(Constant.c.TAG, "init vlc engine");
            mEngine = new EngineVlc();
            mEngine.init(context);
        }else if(engine == Player.ENGINE_IJK){
            Log.d(Constant.c.TAG, "init ijk engine");
            mEngine = new EngineIjk();
            mEngine.init(context);
        }else if(engine == Player.ENGINE_EXO){
            Log.d(Constant.c.TAG, "init exo engine");
            mEngine = new EngineExo();
            mEngine.init(context);
        }
        playStatus = EnumPlayStatus.IDLE;
        mEngine.setUrl(url);
        mEngine.setHeaders(headers);
        mEngine.setListener(this);
        this.autoStartPlay = autoStartPlay;
        initTextureView();
    }

    private void initTextureView() {
        if (textureView == null) {
            textureView = new TextureView(context);
            textureView.setSurfaceTextureListener(this);
        }
        container.removeView(textureView);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        container.addView(textureView, 0,  params);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (this.surfaceTexture == null) {
            Log.d(Constant.c.TAG, "onSurfaceTextureAvailable");
            this.surfaceTexture = surface;
            mEngine.setDisplay(new Surface(surfaceTexture), this.textureView);
            if(autoStartPlay) {
                start();
            }
        } else {
            Log.d(Constant.c.TAG, "onSurfaceTextureAvailable1");
            textureView.setSurfaceTexture(surfaceTexture);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onPlayerSizeChanged(int videoWidth, int videoHeight) {
        Log.d(Constant.c.TAG, "onPlayerSizeChanged");
        if(onPlayListener != null){
            onPlayListener.onPlayerSizeChanged(videoWidth, videoHeight);
        }
//        if(textureView != null && videoWidth > 0 && videoHeight > 0) {
//            int width = textureView.getWidth();
//            float rate = Float.parseFloat(videoHeight+"") / Float.parseFloat(videoWidth + "");
//            float height = width * rate;
//            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, (int)height);
//            layoutParams.gravity = Gravity.CENTER;
//            textureView.setLayoutParams(layoutParams);
//        }
    }

    @Override
    public void onPlayerStatusChanged(EnumPlayStatus status) {
        Log.d(Constant.c.TAG, "onPlayerStatusChanged: " + status.getDes());
        changePlayStatus(status);
        if(onPlayListener != null){
            onPlayListener.onPlayerStatusChanged(status);
        }
    }

    @Override
    public void onPlayerEngineChanged(int mode) {
        if(onPlayListener != null){
            onPlayListener.onPlayerEngineChanged(mode);
        }
    }

    /**
     * 开始播放
     */
    public void start() {
        if(playStatus != EnumPlayStatus.IDLE){
            Log.d(Constant.c.TAG, "PLAY_STATE_IDLE ERROR: " + playStatus);
            return;
        }
        if(mEngine != null){
            changePlayStatus(EnumPlayStatus.PREPARING);
            mEngine.start();
        }
    }

    /**
     * 暂停后开始播放
     */
    public void resume() {
        if(playStatus != EnumPlayStatus.PAUSED){
            Log.d(Constant.c.TAG, "PLAY_STATE_PAUSED ERROR: " + playStatus);
            return;
        }
        if(mEngine != null){
            mEngine.resume();
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if(playStatus != EnumPlayStatus.PLAYING && playStatus != EnumPlayStatus.BUFFERING){
            Log.d(Constant.c.TAG, "PLAY_STATE_PLAYING OR PLAY_STATE_BUFFERING ERROR: " + playStatus);
            return;
        }
        if(mEngine != null){
            mEngine.pause();
        }
    }

    /**
     * 重新开始播放
     */
    public void restart() {
        if(mEngine != null){
            lastPlayPosition = 0;
            mEngine.setUrl(url);
            mEngine.setHeaders(headers);
            mEngine.restart();
        }
    }

    /**
     * 快进
     */
    public void forward(){
        if(mEngine != null){
            lastPlayPosition = 0f;
            mEngine.forward(jumpDuration * 1000);
        }
    }

    /**
     * 快退
     */
    public void rewind(){
        if(mEngine != null){
            lastPlayPosition = 0f;
            mEngine.rewind(jumpDuration * 1000);
        }
    }

    /**
     * 释放播放器资源
     */
    public void release(){
        if(mEngine != null) {
            lastPlayPosition = 0;
            mEngine.release();
            mEngine = null;
            playStatus = EnumPlayStatus.IDLE;
        }
    }

    /**
     * 切换播放引擎
     */
    public void changeEngine(int engine){
        if(onPlayListener != null){
            onPlayListener.onPlayerEngineChanged(engine);
        }
        lastPlayPosition = mEngine.getCurrentDuration();
        mEngine.release();
        mEngine = null;
        this.engine = engine;
        this.surfaceTexture = null;
        prepare();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void enterFullScreen(){
        if (playMode == Player.PLAY_MODE_FULL_SCREEN) return;
        Log.d(Constant.c.TAG, "enterFullScreen");
        ExUtils.hideActionBar(context);
        ExUtils.findActivity(context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.removeView(container);
        ViewGroup contentView = ExUtils.findActivity(context)
                .findViewById(android.R.id.content);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentView.addView(container, params);
        changePlayMode(Player.PLAY_MODE_FULL_SCREEN);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void exitFullScreen(){
        if (playMode == Player.PLAY_MODE_FULL_SCREEN) {
            Log.d(Constant.c.TAG, "exitFullScreen");
            ExUtils.showActionBar(context);
            ExUtils.findActivity(context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            ViewGroup contentView = ExUtils.findActivity(context)
                    .findViewById(android.R.id.content);
            contentView.removeView(container);
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            this.addView(container, params);
            changePlayMode(Player.PLAY_MODE_NORMAL);
        }
    }

    private void changePlayStatus(EnumPlayStatus playStatus){
        this.playStatus = playStatus;
        if(controller != null) {
            controller.onPlayInfoChanged(ExPlayInfo.valueOfStatus(playStatus));
        }
        if(playStatus == EnumPlayStatus.PREPARED){
            if(lastPlayPosition > 0){
                mEngine.seekTo(lastPlayPosition);
            }
        }else if(playStatus == EnumPlayStatus.COMPLETED){
            lastPlayPosition = 0;
            if(loop){
                restart();
            }
        }else if(playStatus == EnumPlayStatus.ERROR){
            lastPlayPosition = 0;
        }
    }

    private void changePlayMode(int playMode){
        this.playMode = playMode;
        if(controller != null) {
            controller.onPlayInfoChanged(ExPlayInfo.valueOfMode(playMode));
        }
    }

    /**
     * 设置播放状态监听
     */
    public void setOnPlayListener(OnPlayListener onPlayListener) {
        this.onPlayListener = onPlayListener;
    }

    /**
     * 获取视频标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取当前播放长度
     */
    public float getCurrentDuration(){
        return mEngine.getCurrentDuration();
    }

    /**
     * 获取视频总长度
     */
    public float getTotalDuration(){
        return mEngine.getTotalDuration();
    }

    /**
     * 设置播放结束后是否循环播放，默认关闭
     */
    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    /**
     * 设置快进或快退时的移动间隔，默认20秒
     */
    public void setJumpDuration(int jumpDuration) {
        this.jumpDuration = jumpDuration;
    }
}
