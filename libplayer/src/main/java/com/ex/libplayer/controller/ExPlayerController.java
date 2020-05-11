package com.ex.libplayer.controller;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ex.libplayer.R;
import com.ex.libplayer.entities.ExPlayInfo;
import com.ex.libplayer.enu.EnumPlayStatus;
import com.ex.libplayer.player.Player;
import com.ex.libplayer.util.ExUtils;
import com.ex.libplayer.view.ExPlayView;

import java.util.Timer;
import java.util.TimerTask;

public class ExPlayerController extends FrameLayout implements Controller {

    private static final String INIT_TIME = "00:00:00";
    private static final String COLOR_PRIMARY = "#FF2196F3";
    private static final String COLOR_ACCENT = "#FFB1D3F7";
    private static final int HIDE_DELAY = 15;

    private static final int MSG_UPDATE_PLAY_MODE = 1;
    private static final int MSG_UPDATE_PLAY_STATUS = 2;
    private static final int MSG_UPDATE_PROGRESS = 3;
    private static final int MSG_HIDE_CONTROL_VIEW = 4;

    private Context context;

    private ExPlayView playView;
    private int playMode = Player.PLAY_MODE_NORMAL;
    private EnumPlayStatus playStatus = EnumPlayStatus.IDLE;
    private ExPlayInfo exPlayInfo;

    private int padding = 8;
    private int margin = 8;
    private int ibtSize = 36;
    private int topViewHeight = 36;
    private int bottomViewHeight = 36;

    private FrameLayout topView;
    private FrameLayout tapView;
    private LinearLayout bottomView;
    private TextView tvTitle;
    private ImageButton ibtMore;
    private ImageButton ibtPlay;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private ProgressBar progressBar;
    private ImageButton ibtFullScreen;

    private float touchDownPointX;
    private float touchUpPointX;

    private LinearLayout loadingView;
    private ProgressBar pbLoading;
    private TextView tvStatus;
    private ImageButton ibtReplay;

    private LinearLayout moreView;
    private TextView tvEngine;
    private Button btEngineNative;
    private Button btEngineIjk;
    private Button btEngineExo;
    private Button btEngineVlc;

    private Timer timerAutoHideControlView;
    private int hiddenDelayTime = HIDE_DELAY;
    private Timer progressTimer;
    private Handler handler = new Handler(msg -> {
        switch (msg.what){
            case MSG_UPDATE_PLAY_MODE:
                onPlayModeUpdated();
                break;
            case MSG_UPDATE_PLAY_STATUS:
                onPlayStatusUpdated();
                break;
            case MSG_UPDATE_PROGRESS:
                onProgressUpdated();
                break;
            case MSG_HIDE_CONTROL_VIEW:
                setControlViewVisibility(false);
                break;
            default:
                break;
        }
        return true;
    });

    public ExPlayerController(@NonNull Context context) {
        this(context, null);
    }

    public ExPlayerController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExPlayerController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        padding = ExUtils.dp2px(context, padding);
        margin = ExUtils.dp2px(context, margin);
        ibtSize = ExUtils.dp2px(context, ibtSize);
        topViewHeight = ExUtils.dp2px(context, topViewHeight);
        bottomViewHeight = ExUtils.dp2px(context, bottomViewHeight);
        initTopControllerView();
        initTapControllerView();
        initBottomControllerView();
        initLoadingView();
    }

    // 初始化控制器头部UI
    private void initTopControllerView() {
        topView = new FrameLayout(context);
        topView.setBackgroundResource(R.color.lpColorT5);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.TOP;
        this.addView(this.topView, params);
        initTvTitle();
        initBtnMore();
    }

    // 初始化标题文本控件
    private void initTvTitle() {
        tvTitle = new TextView(context);
        tvTitle.setText("");
        tvTitle.setTextColor(Color.WHITE);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        params.setMargins(margin, margin, margin, margin);
        this.topView.addView(this.tvTitle, params);
    }

    // 初始化更多设置按钮
    private void initBtnMore(){
        ibtMore = new ImageButton(context);
        ibtMore.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_more));
        ibtMore.setBackgroundColor(Color.TRANSPARENT);
        ibtMore.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ibtMore.setCropToPadding(true);
        ibtMore.setPadding(padding, padding, padding, padding);
        LayoutParams params = new LayoutParams(ibtSize, ibtSize);
        params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        this.topView.addView(this.ibtMore, params);
        ibtMore.setOnClickListener(v -> {
            setMoreViewVisibility(true);
        });
    }

    // 初始化屏幕中间响应触摸动作的UI
    private void initTapControllerView() {
        tapView = new FrameLayout(context);
        tapView.setBackgroundColor(Color.TRANSPARENT);
        tapView.setClickable(true);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(0, topViewHeight, 0, bottomViewHeight);
        params.gravity = Gravity.CENTER;
        this.addView(this.tapView, params);
        tapView.setOnTouchListener((v, event) -> {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    touchDownPointX = event.getRawX();
                    break;
                case MotionEvent.ACTION_UP:
                    tapView.performClick();
                    touchUpPointX = event.getRawX();
                    float diff = touchUpPointX - touchDownPointX;
                    if(Math.abs(diff) <= 30f){
                        setControlViewVisibility(topView.getVisibility() == View.GONE);
                        setMoreViewVisibility(false);
                    }else if (diff > 30){
                        playView.forward();
                    }else if (diff < -30){
                        playView.rewind();
                    }
                    setMoreViewVisibility(false);
                    break;
                default:
                    break;
            }
            return true;
        });

    }

    // 初始化控制器底部UI
    private void initBottomControllerView() {
        bottomView = new LinearLayout(context);
        bottomView.setBackgroundResource(R.color.lpColorT5);
        bottomView.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        this.addView(this.bottomView, params);
        initBtnPlay();
        initTvCurrentTime();
        initProgressBar();
        initTvTotalTime();
        initBtnFullScreen();
    }

    // 初始化播放按钮
    private void initBtnPlay(){
        ibtPlay = new ImageButton(context);
        int padding = ExUtils.dp2px(context, 10);
        ibtPlay.setPadding(padding, padding, padding, padding);
        ibtPlay.setBackgroundColor(Color.TRANSPARENT);
        ibtPlay.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ibtPlay.setCropToPadding(true);
        ibtPlay.setImageDrawable(context.getDrawable(R.drawable.exo_icon_play));
        LayoutParams params = new LayoutParams(ibtSize, ibtSize);
        this.bottomView.addView(this.ibtPlay, params);
        ibtPlay.setOnClickListener(v -> {
            playOrPause();
            setControlViewVisibility(true);
            startAutoHideTimer();
        });
    }

    // 初始化当前时长控件
    private void initTvCurrentTime() {
        tvCurrentTime = new TextView(context);
        tvCurrentTime.setText(INIT_TIME);
        tvCurrentTime.setTextSize(12);
        tvCurrentTime.setTextColor(Color.WHITE);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(margin, 0, 0 , 0);
        this.bottomView.addView(this.tvCurrentTime, params);
    }

    // 初始化进度条
    private void initProgressBar() {
        progressBar = new ProgressBar(context,null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setProgress(0);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor(COLOR_PRIMARY)));
        progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        params.setMargins(margin, 0, margin , 0);
        this.bottomView.addView(this.progressBar, params);
    }

    // 初始化总时长文本控件
    private void initTvTotalTime() {
        tvTotalTime = new TextView(context);
        tvTotalTime.setText(INIT_TIME);
        tvTotalTime.setTextSize(12);
        tvTotalTime.setTextColor(Color.WHITE);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, margin , 0);
        this.bottomView.addView(this.tvTotalTime, params);
    }

    // 初始化全屏按钮
    private void initBtnFullScreen(){
        ibtFullScreen = new ImageButton(context);
        int padding = ExUtils.dp2px(context, 10);
        ibtFullScreen.setPadding(padding, padding, padding, padding);
        ibtFullScreen.setBackgroundColor(Color.TRANSPARENT);
        ibtFullScreen.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ibtFullScreen.setCropToPadding(true);
        ibtFullScreen.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_full_screen));
        LayoutParams params = new LayoutParams(ibtSize, ibtSize);
        params.setMargins(margin, 0, 0 , 0);
        this.bottomView.addView(this.ibtFullScreen, params);
        ibtFullScreen.setOnClickListener(v -> {
            fullScreenOrExit();
            setControlViewVisibility(true);
            startAutoHideTimer();
        });
    }

    // 初始化加载提示UI
    private void initLoadingView() {
        loadingView = new LinearLayout(context);
        loadingView.setBackgroundColor(Color.TRANSPARENT);
        loadingView.setGravity(Gravity.CENTER);
        loadingView.setOrientation(LinearLayout.VERTICAL);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        this.addView(this.loadingView, params);
        initPbLoading();
        initTvStatus();
        initBtnReplay();
    }

    private void initPbLoading(){
        pbLoading = new ProgressBar(context);
        pbLoading.setIndeterminateDrawable(context.getDrawable(R.drawable.lp_pb_loading));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ExUtils.dp2px(context, 36),
                ExUtils.dp2px(context, 36));
        this.loadingView.addView(this.pbLoading, params);
    }

    private void initTvStatus(){
        tvStatus = new TextView(context);
        tvStatus.setText("loading ...");
        tvStatus.setTextSize(12);
        tvStatus.setTextColor(Color.WHITE);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        this.loadingView.addView(this.tvStatus, params);
    }

    // 初始化重播按钮
    private void initBtnReplay(){
        ibtReplay = new ImageButton(context);
        int padding = ExUtils.dp2px(context, 10);
        ibtReplay.setPadding(padding, padding, padding, padding);
        ibtReplay.setBackgroundColor(Color.TRANSPARENT);
        ibtReplay.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ibtReplay.setCropToPadding(true);
        ibtReplay.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_replay));
        LayoutParams params = new LayoutParams(ibtSize, ibtSize);
        params.setMargins(margin, 0, 0 , 0);
        this.loadingView.addView(this.ibtReplay, params);
        ibtReplay.setOnClickListener(v -> {
            if(playView != null){
                playView.restart();
            }
        });
        ibtReplay.setVisibility(GONE);
    }

    // 初始化更多设置UI
    private void initMoreView() {
        moreView = new LinearLayout(context);
        moreView.setBackgroundResource(R.color.lpColorT5);
        moreView.setGravity(Gravity.CENTER);
        moreView.setOrientation(LinearLayout.HORIZONTAL);
        moreView.setPadding(padding, padding, padding, padding);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        this.addView(this.moreView, params);
        initTextEngine();
        initBtEngineNative();
        initBtEngineIjk();
        initBtEngineExo();
        initBtEngineVlc();
    }

    private void initTextEngine() {
        tvEngine = new TextView(context);
        tvEngine.setText("PLAYER:  ");
        tvEngine.setTextColor(Color.WHITE);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        this.moreView.addView(this.tvEngine, params);
    }

    private void initBtEngineNative() {
        btEngineNative = new Button(context);
        btEngineNative.setBackgroundColor(Color.TRANSPARENT);
        btEngineNative.setText("Native");
        btEngineNative.setTextColor(Color.WHITE);
        LayoutParams params = new LayoutParams(ExUtils.dp2px(context, 60),
                ExUtils.dp2px(context, 40));
        this.moreView.addView(this.btEngineNative, params);
        btEngineNative.setOnClickListener(v -> {
            changeEngine(Player.ENGINE_NATIVE);
            setMoreViewVisibility(false);
        });
    }

    private void initBtEngineIjk() {
        btEngineIjk = new Button(context);
        btEngineIjk.setBackgroundColor(Color.TRANSPARENT);
        btEngineIjk.setText("IJK");
        btEngineIjk.setTextColor(Color.WHITE);
        LayoutParams params = new LayoutParams(ExUtils.dp2px(context, 60),
                ExUtils.dp2px(context, 40));
        this.moreView.addView(this.btEngineIjk, params);
        btEngineIjk.setOnClickListener(v -> {
            changeEngine(Player.ENGINE_IJK);
            setMoreViewVisibility(false);
        });
    }

    private void initBtEngineExo() {
        btEngineExo = new Button(context);
        btEngineExo.setBackgroundColor(Color.TRANSPARENT);
        btEngineExo.setText("Exo");
        btEngineExo.setTextColor(Color.WHITE);
        LayoutParams params = new LayoutParams(ExUtils.dp2px(context, 60),
                ExUtils.dp2px(context, 40));
        this.moreView.addView(this.btEngineExo, params);
        btEngineExo.setOnClickListener(v -> {
            changeEngine(Player.ENGINE_EXO);
            setMoreViewVisibility(false);
        });
    }

    private void initBtEngineVlc() {
        btEngineVlc = new Button(context);
        btEngineVlc.setBackgroundColor(Color.TRANSPARENT);
        btEngineVlc.setText("Vlc");
        btEngineVlc.setTextColor(Color.WHITE);
        LayoutParams params = new LayoutParams(ExUtils.dp2px(context, 60),
                ExUtils.dp2px(context, 40));
        this.moreView.addView(this.btEngineVlc, params);
        btEngineVlc.setOnClickListener(v -> {
            changeEngine(Player.ENGINE_VLC);
            setMoreViewVisibility(false);
        });
    }

    @Override
    public void onBindPlayView(ExPlayView playView) {
        this.playView = playView;
        if(!TextUtils.isEmpty(playView.getTitle())) {
            tvTitle.setText(playView.getTitle());
        }
        startAutoHideTimer();
    }

    @Override
    public void onPlayInfoChanged(ExPlayInfo info) {
        if(info.getType() == ExPlayInfo.TYPE_PLAY_MODE){
            this.playMode = info.getPlayMode();
            handler.sendEmptyMessage(MSG_UPDATE_PLAY_MODE);
        }
        if(info.getType() == ExPlayInfo.TYPE_PLAY_STATUS) {
            this.playStatus = info.getPlayStatus();
            handler.sendEmptyMessage(MSG_UPDATE_PLAY_STATUS);
        }
        if(info.getType() == ExPlayInfo.TYPE_PROGRESS) {
            this.exPlayInfo = info;
            handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelAutoHideTimer();
        cancelProgressTimer();
        super.onDetachedFromWindow();
    }

    /**
     * 更新播放模式相关UI
     */
    private void onPlayModeUpdated(){
        if (playMode == Player.PLAY_MODE_NORMAL) {
            ibtFullScreen.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_full_screen));
        } else if (playMode == Player.PLAY_MODE_FULL_SCREEN) {
            ibtFullScreen.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_full_screen_exit));
        }
    }

    /**
     * 更新播放状态相关UI
     */
    private void onPlayStatusUpdated(){
        if(playStatus == EnumPlayStatus.IDLE ||
                playStatus == EnumPlayStatus.PREPARING){
            tvTitle.setText(playView.getTitle());
            tvCurrentTime.setText(INIT_TIME);
            tvTotalTime.setText(INIT_TIME);
            progressBar.setProgress(0);
            ibtPlay.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_play));
            setLoadingViewVisibility(true);
            ibtReplay.setVisibility(GONE);
        }else if(playStatus == EnumPlayStatus.BUFFERING){
            setLoadingViewVisibility(true);
            ibtReplay.setVisibility(GONE);
        }else if(playStatus == EnumPlayStatus.PREPARED){
            setLoadingViewVisibility(false);
            ibtReplay.setVisibility(GONE);
        }else if(playStatus == EnumPlayStatus.PLAYING){
            ibtPlay.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_pause));
            setLoadingViewVisibility(false);
            ibtReplay.setVisibility(GONE);
            startProgressTimer();
        }else if(playStatus == EnumPlayStatus.PAUSED){
            ibtPlay.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_play));
            cancelProgressTimer();
        }else if(playStatus == EnumPlayStatus.COMPLETED){
            if(ibtReplay.getVisibility() == GONE) {
                ibtPlay.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_play));
                setLoadingViewVisibility(true);
                tvStatus.setText("play completed!");
                pbLoading.setVisibility(GONE);
                ibtReplay.setVisibility(VISIBLE);
                cancelProgressTimer();
            }
        }else if(playStatus == EnumPlayStatus.ERROR){
            ibtPlay.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_play));
            setLoadingViewVisibility(true);
            tvStatus.setText("play error!");
            pbLoading.setVisibility(GONE);
            ibtReplay.setVisibility(VISIBLE);
            cancelProgressTimer();
        }
    }

    /**
     * 更新播放进度信息
     */
    private void onProgressUpdated(){
        if(playView != null) {
            tvCurrentTime.setText(ExUtils.formatMediaTime((long) playView.getCurrentDuration()));
            tvTotalTime.setText(ExUtils.formatMediaTime((long) playView.getTotalDuration()));
            float progress = (playView.getCurrentDuration() / playView.getTotalDuration()) * 100;
            progressBar.setProgress((int) progress);
        }
    }

    private void changeEngine(int engine){
        playView.changeEngine(engine);
    }

    private void playOrPause(){
        if(playStatus == EnumPlayStatus.PLAYING){
            playView.pause();
        }else if(playStatus == EnumPlayStatus.PAUSED){
            playView.resume();
        }
    }

    /**
     * 全屏切换
     */
    private void fullScreenOrExit(){
        if(playMode == Player.PLAY_MODE_NORMAL){
            playView.enterFullScreen();
        }else if(playMode == Player.PLAY_MODE_FULL_SCREEN){
            playView.exitFullScreen();
        }
    }

    private void setMoreViewVisibility(boolean visible){
        if(moreView == null){
            initMoreView();
        }
        moreView.setVisibility(visible? VISIBLE: GONE);
    }

    /**
     * 设置播放控制器的可见性
     */
    public void setControlViewVisibility(boolean visible){
        topView.setVisibility(visible? VISIBLE: View.GONE);
        bottomView.setVisibility(visible? VISIBLE: View.GONE);
        startAutoHideTimer();
    }

    /**
     * 设置loading view的可见性
     */
    private void setLoadingViewVisibility(boolean visible){
        loadingView.setVisibility(visible? VISIBLE: GONE);
        pbLoading.setVisibility(visible? VISIBLE: GONE);
        tvStatus.setText("loading ...");
    }

    /**
     * 开启控制界面自动隐藏定时器
     */
    private void startAutoHideTimer(){
        if(timerAutoHideControlView != null){
            timerAutoHideControlView.cancel();
            timerAutoHideControlView = null;
        }
        timerAutoHideControlView = new Timer();
        timerAutoHideControlView.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(MSG_HIDE_CONTROL_VIEW);
            }
        }, 1000 * hiddenDelayTime);
    }

    /**
     * 取消控制界面自动隐藏定时器
     */
    private void cancelAutoHideTimer(){
        if(timerAutoHideControlView != null){
            timerAutoHideControlView.cancel();
            timerAutoHideControlView = null;
        }
    }

    /**
     * 开始播放进度更新定时器
     */
    private void startProgressTimer() {
        if(progressTimer != null){
            progressTimer.cancel();
            progressTimer = null;
        }
        progressTimer = new Timer();
        progressTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
            }
        }, 10L, 500L);
    }

    /**
     * 取消播放进度更新定时器
     */
    private void cancelProgressTimer(){
        if(progressTimer != null){
            progressTimer.cancel();
            progressTimer = null;
        }
    }

    /**
     * 设置全屏按钮的显示和隐藏
     */
    public void setBtnFullScreenVisibility(boolean visible){
        ibtFullScreen.setVisibility(visible? VISIBLE: GONE);
    }

    /**
     * 设置more按钮的显示和隐藏
     */
    public void setBtnMoreVisibility(boolean visibility){
        ibtMore.setVisibility(visibility? VISIBLE: GONE);
    }

    /**
     * 设置自动隐藏controller view界面的延迟时间， 默认15秒
     */
    public void setHiddenDelayTime(int hiddenDelayTime) {
        this.hiddenDelayTime = hiddenDelayTime;
    }

    /**
     * 设置中间显示的播放状态提示文字
     */
    public void setTipText(String text){
        if(!TextUtils.isEmpty(text)) {
            tvStatus.setText(text);
        }
    }
}
