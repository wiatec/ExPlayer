package com.ex.libplayer.controller;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.ex.libplayer.player.Player;
import com.ex.libplayer.util.ExUtils;
import com.ex.libplayer.view.ExPlayView;

public class ExPlayerController extends FrameLayout implements Controller {

    private static final String INIT_TIME = "00:00:00";
    private static final String COLOR_PRIMARY = "#FF2196F3";
    private static final String COLOR_ACCENT = "#FFB1D3F7";

    private ExPlayView playView;
    private Context context;

    private int playMode = Player.PLAY_MODE_NORMAL;
    private int playStatus = Player.PLAY_STATE_IDLE;

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

    private LinearLayout loadingView;
    private ProgressBar pbLoading;
    private TextView tvStatus;

    private float touchDownPointX;
    private float touchUpPointX;

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
        ibtMore.setOnClickListener(v -> changeEngine(Player.ENGINE_EXO));
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
        ibtPlay.setOnClickListener(v -> playOrPause());
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
        ibtFullScreen.setOnClickListener(v -> fullScreenOrExit());
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

    @Override
    public void onBindPlayView(ExPlayView playView) {
        this.playView = playView;
        setTitle(playView.getTitle());
    }

    @Override
    public void onPlayInfoChanged(ExPlayInfo info) {
        if(info.getType() == ExPlayInfo.TYPE_PLAY_MODE){
            this.playMode = info.getPlayMode();
        }
        if(info.getType() == ExPlayInfo.TYPE_PLAY_STATUS) {
            this.playStatus = info.getPlayStatus();
        }
        updateUI(info);
    }

    private void setTitle(String title){
        if(!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
    }

    private void updateUI(ExPlayInfo info) {
        switch (info.getType()){
            case ExPlayInfo.TYPE_PLAY_MODE:
                onPlayModeUpdated();
                break;
            case ExPlayInfo.TYPE_PLAY_STATUS:
                onPlayStatusUpdated();
                break;
            case ExPlayInfo.TYPE_PROGRESS:
                onProgressUpdated(info);
                break;
            default:
                break;
        }
    }

    private void onPlayModeUpdated(){
        if (playMode == Player.PLAY_MODE_NORMAL) {
            ibtFullScreen.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_full_screen));
        } else if (playMode == Player.PLAY_MODE_FULL_SCREEN) {
            ibtFullScreen.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_full_screen_exit));
        }
    }

    private void onPlayStatusUpdated(){
        if(playStatus == Player.PLAY_STATE_IDLE ||
                playStatus == Player.PLAY_STATE_PREPARING){
            tvCurrentTime.setText(INIT_TIME);
            tvTotalTime.setText(INIT_TIME);
            progressBar.setProgress(0);
            ibtPlay.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_play));
            setLoadingViewVisibility(true);
        }else if(playStatus == Player.PLAY_STATE_PLAYING){
            ibtPlay.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_pause));
            setLoadingViewVisibility(false);
        }else if(playStatus == Player.PLAY_STATE_PAUSED){
            ibtPlay.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_play));
        }else if(playStatus == Player.PLAY_STATE_COMPLETED){
            ibtPlay.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_play));
            setLoadingViewVisibility(true);
            tvStatus.setText("play completed!");
            pbLoading.setVisibility(GONE);
        }else if(playStatus == Player.PLAY_STATE_ERROR){
            ibtPlay.setImageDrawable(context.getDrawable(R.drawable.lp_ic_action_play));
            setLoadingViewVisibility(true);
            tvStatus.setText("play error!");
            pbLoading.setVisibility(GONE);
        }
    }

    private void onProgressUpdated(ExPlayInfo info){
        tvCurrentTime.setText(ExUtils.formatMediaTime((long) info.getCurrentTime()));
        tvTotalTime.setText(ExUtils.formatMediaTime((long) info.getTotalTime()));
        float progress = (info.getCurrentTime() / info.getTotalTime()) * 100;
        progressBar.setProgress((int) progress);
    }

    private void changeEngine(int engine){
        playView.changeEngine(engine);
    }


    private void playOrPause(){
        if(playStatus == Player.PLAY_STATE_PLAYING){
            playView.pause();
        }else if(playStatus == Player.PLAY_STATE_PAUSED){
            playView.resume();
        }
    }

    private void fullScreenOrExit(){
        if(playMode == Player.PLAY_MODE_NORMAL){
            playView.enterFullScreen();
        }else if(playMode == Player.PLAY_MODE_FULL_SCREEN){
            playView.exitFullScreen();
        }
    }

    private void setMoreViewVisibility(boolean visible){

    }

    private void setControlViewVisibility(boolean visible){
        topView.setVisibility(visible? VISIBLE: View.GONE);
        bottomView.setVisibility(visible? VISIBLE: View.GONE);
    }

    private void setLoadingViewVisibility(boolean visible){
        loadingView.setVisibility(visible? VISIBLE: GONE);
        pbLoading.setVisibility(visible? VISIBLE: GONE);
    }

    public void setBtnFullScreenVisibility(boolean visible){
        ibtFullScreen.setVisibility(visible? VISIBLE: GONE);
    }

    public void setBtnMoreVisibility(boolean visibility){
        ibtMore.setVisibility(visibility? VISIBLE: GONE);
    }

}
