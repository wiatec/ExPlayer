package com.ex.libplayer.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
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
import com.ex.libplayer.listener.OnPlayListener;
import com.ex.libplayer.player.Player;
import com.ex.libplayer.util.ExUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * 为保持全屏效果正确，需要在AndroidManifest.xml中给使用此view的activity添加
 *   android:configChanges="orientation|keyboardHidden|screenSize"
 */
public class ExPlayView extends FrameLayout implements TextureView.SurfaceTextureListener, OnPlayListener {

    private static final int MSG_UPDATE_PLAY_MODE = 0x001;
    private static final int MSG_UPDATE_PLAY_STATUS = 0x002;
    private static final int MSG_UPDATE_PROGRESS = 0x003;

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

    private String url;
    private String title;
    private Map<String, String> headers;
    private int playMode = Player.PLAY_MODE_NORMAL;
    private int playStatus = Player.PLAY_STATE_IDLE;

    private Timer progressTimer;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MSG_UPDATE_PLAY_MODE:
                    if(controller != null) {
                        controller.onPlayInfoChanged(ExPlayInfo.valueOfMode(playMode));
                    }
                    break;
                case MSG_UPDATE_PLAY_STATUS:
                    if(controller != null) {
                        controller.onPlayInfoChanged(ExPlayInfo.valueOfStatus(playStatus));
                    }
                    break;
                case MSG_UPDATE_PROGRESS:
                    if(controller != null) {
                        controller.onPlayInfoChanged(ExPlayInfo.valueOfProgress(mEngine.getCurrentDuration(), mEngine.getTotalDuration()));
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    });

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

    private void startProgressTimer() {
        if(progressTimer != null){
            progressTimer.cancel();
            progressTimer = null;
        }
        progressTimer = new Timer();
        progressTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(mEngine != null && mEngine.isPlaying()) {
                    handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
                }
            }
        }, 10L, 500L);
    }

    public void cancelProgressTimer(){
        if(progressTimer != null){
            progressTimer.cancel();
            progressTimer = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelProgressTimer();
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

    public void setController(ExPlayerController controller) {
        this.controller = controller;
        controller.onBindPlayView(this);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.container.addView(this.controller, params);
    }

    public void prepare(){
        prepare(this.engine);
    }

    public void prepare(int engine){
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
        playStatus = Player.PLAY_STATE_IDLE;
        mEngine.setUrl(url);
        mEngine.setHeaders(headers);
        mEngine.setListener(this);
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
            this.surfaceTexture = surface;
            mEngine.setDisplay(new Surface(surfaceTexture), this.textureView);
            start();
            startProgressTimer();
        } else {
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
    public void onPlayerStatusChanged(int status) {
        Log.d(Constant.c.TAG, "onPlayerStatusChanged: " + status);
        if(onPlayListener != null){
            onPlayListener.onPlayerStatusChanged(status);
        }
        playStatus = status;
        handler.sendEmptyMessage(MSG_UPDATE_PLAY_STATUS);
    }

    @Override
    public void onPlayerEngineChanged(int mode) {
        if(onPlayListener != null){
            onPlayListener.onPlayerEngineChanged(mode);
        }
    }

    public void start() {
        if(playStatus != Player.PLAY_STATE_IDLE){
            Log.d(Constant.c.TAG, "PLAY_STATE_IDLE ERROR: " + playStatus);
            return;
        }
        if(mEngine != null){
            playStatus = Player.PLAY_STATE_PREPARING;
            handler.sendEmptyMessage(MSG_UPDATE_PLAY_STATUS);
            mEngine.start();
        }
    }

    public void resume() {
        if(playStatus != Player.PLAY_STATE_PAUSED){
            Log.d(Constant.c.TAG, "PLAY_STATE_PAUSED ERROR: " + playStatus);
            return;
        }
        if(mEngine != null){
            mEngine.resume();
        }
    }

    public void pause() {
        if(playStatus != Player.PLAY_STATE_PLAYING && playStatus != Player.PLAY_STATE_BUFFERING){
            Log.d(Constant.c.TAG, "PLAY_STATE_PLAYING OR PLAY_STATE_BUFFERING ERROR: " + playStatus);
            return;
        }
        if(mEngine != null){
            mEngine.pause();
        }
    }

    public void restart() {
        if(mEngine != null){
            mEngine.setUrl(url);
            mEngine.setHeaders(headers);
            mEngine.restart();
        }
    }

    public void forward(){
        if(mEngine != null){
            mEngine.forward(15 * 1000);
        }
    }

    public void rewind(){
        if(mEngine != null){
            mEngine.rewind(15 * 1000);
        }
    }

    public void release(){
        if(mEngine != null) {
            mEngine.release();
            mEngine = null;
            playStatus = Player.PLAY_STATE_IDLE;
        }
        cancelProgressTimer();
    }

    public void changeEngine(int engine){
        if(onPlayListener != null){
            onPlayListener.onPlayerEngineChanged(engine);
        }
        mEngine.release();
        mEngine = null;
        this.engine = engine;
        this.surfaceTexture = null;
        cancelProgressTimer();
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
        playMode = Player.PLAY_MODE_FULL_SCREEN;
        handler.sendEmptyMessage(MSG_UPDATE_PLAY_MODE);
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
            playMode = Player.PLAY_MODE_NORMAL;
            handler.sendEmptyMessage(MSG_UPDATE_PLAY_MODE);
        }
    }


    public void setOnPlayListener(OnPlayListener onPlayListener) {
        this.onPlayListener = onPlayListener;
    }

    public String getTitle() {
        return title;
    }
}
