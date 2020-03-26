package com.ex.libplayer.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ex.libplayer.Constant;
import com.ex.libplayer.R;
import com.ex.libplayer.listener.OnPlayListener;
import com.ex.libplayer.player.ExPlayer;
import com.ex.libplayer.player.Player;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class PlayFragment extends Fragment implements SurfaceHolder.Callback, OnPlayListener, View.OnClickListener {

    private static final int MSG_HIDE_CONTROL_VIEW = 0x001;
    private static final int MSG_SHOW_CONTROL_VIEW = 0x002;
    private static final int MSG_HIDE_LOADING_VIEW = 0x003;
    private static final int MSG_SHOW_LOADING_VIEW = 0x004;
    private static final int MSG_SHOW_MORE_VIEW = 0x005;
    private static final int MSG_HIDE_MORE_VIEW = 0x006;

    private static final int MSG_SET_ICON_PLAY = 0x013;
    private static final int MSG_SET_ICON_PAUSE = 0x014;
    private static final int MSG_PLAY_COMPLETED = 0x015;
    private static final int MSG_PLAY_ERROR = 0x016;
    private static final int MSG_UPDATE_PLAY_INFO = 0x017;

    private Context context;
    private SurfaceView surfaceView;

    private LinearLayout controlTopView;
    private LinearLayout controlBottomView;
    private FrameLayout tapView;
    private TextView tvTitle;
    private ImageButton ibtMore;
    private ImageButton ibtPlay;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private ProgressBar pbDuration;
    private ImageButton ibtFullScreen;
    private float touchDownPointX;
    private float touchUpPointX;

    private LinearLayout loadingView;
    private ProgressBar pbLoading;
    private TextView tvLoading;

    private LinearLayout moreView;
    private Button btP1;
    private Button btP2;
    private Button btP3;
    private Button btP4;

    private Handler handler;
    private Timer timer;
    private Timer statusTimer;

    private Player player;
    private int engine;
    private float currentPlayPosition;

    private String title = "";
    private String url = "";
    private boolean showButtonFullScreen = true;
    private boolean showControlViewWhenInit = true;
    private OnPlayListener onPlayListener;

    public PlayFragment() {
        this(Player.ENGINE_NATIVE);
    }

    public PlayFragment(int engine) {
        this.engine = engine;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        initPlayer(context, engine);
        initHandler();
    }

    private void initPlayer(Context context, int engine){
        player = new ExPlayer();
        player.init(context, engine);
        player.setUrl(url);
        player.setListener(this);
    }

    private void initHandler() {
        handler = new Handler(context.getMainLooper(), msg -> {
            switch (msg.what){
                case MSG_HIDE_CONTROL_VIEW:
                    controlTopView.setVisibility(View.GONE);
                    controlBottomView.setVisibility(View.GONE);
                    break;
                case MSG_SHOW_CONTROL_VIEW:
                    controlTopView.setVisibility(View.VISIBLE);
                    controlBottomView.setVisibility(View.VISIBLE);
                    break;
                case MSG_HIDE_LOADING_VIEW:
                    loadingView.setVisibility(View.GONE);
                    break;
                case MSG_SHOW_LOADING_VIEW:
                    pbLoading.setVisibility(View.VISIBLE);
                    loadingView.setVisibility(View.VISIBLE);
                    tvLoading.setText("loading ...");
                    break;
                case MSG_HIDE_MORE_VIEW:
                    moreView.setVisibility(View.GONE);
                    break;
                case MSG_SHOW_MORE_VIEW:
                    moreView.setVisibility(View.VISIBLE);
                    break;
                case MSG_SET_ICON_PLAY:
                    ibtPlay.setImageResource(R.drawable.lp_ic_action_play);
                    break;
                case MSG_SET_ICON_PAUSE:
                    ibtPlay.setImageResource(R.drawable.lp_ic_action_pause);
                    break;
                case MSG_PLAY_COMPLETED:
                    pbLoading.setVisibility(View.GONE);
                    tvLoading.setText("play completed!");
                    break;
                case MSG_PLAY_ERROR:
                    pbLoading.setVisibility(View.GONE);
                    tvLoading.setText("play error!");
                    break;
                case MSG_UPDATE_PLAY_INFO:
                    currentPlayPosition = player.getCurrentDuration();
                    tvCurrentTime.setText(player.getCurrentTime());
                    tvTotalTime.setText(player.getTotalTime());
                    pbDuration.setProgress(player.getProgress());
                    break;
                default:
                    break;
            }
            return true;
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lp_fragment_play, container, false);
        initControlView(view);
        surfaceView = view.findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initControlView(View view) {
        controlTopView = view.findViewById(R.id.control_top_view);
        controlBottomView = view.findViewById(R.id.control_bottom_view);
        controlTopView.setVisibility(showControlViewWhenInit? View.VISIBLE: View.GONE);
        controlBottomView.setVisibility(showControlViewWhenInit? View.VISIBLE: View.GONE);
        tapView = view.findViewById(R.id.tap_view);
        tvTitle = view.findViewById(R.id.tv_title);
        ibtMore = view.findViewById(R.id.ibt_more);
        tvTitle.setText(title);
        ibtMore.setOnClickListener(this);
        pbDuration = view.findViewById(R.id.pb_duration);
        ibtPlay = view.findViewById(R.id.ibt_play);
        tvCurrentTime = view.findViewById(R.id.tv_current_time);
        tvTotalTime = view.findViewById(R.id.tv_total_time);
        ibtFullScreen = view.findViewById(R.id.ibt_full_screen);
        ibtFullScreen.setVisibility(showButtonFullScreen? View.VISIBLE: View.GONE);

        loadingView = view.findViewById(R.id.loading_view);
        pbLoading = view.findViewById(R.id.pb_loading);
        tvLoading = view.findViewById(R.id.tv_loading);

        tapView.setOnClickListener(this);
        ibtPlay.setOnClickListener(this);
        ibtFullScreen.setOnClickListener(this);
        tapView.setOnTouchListener((v, event) -> {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    touchDownPointX = event.getRawX();
                    break;
                case MotionEvent.ACTION_UP:
                    touchUpPointX = event.getRawX();
                    float diff = touchUpPointX - touchDownPointX;
                    if(Math.abs(diff) <= 30f){
                        if(controlTopView.getVisibility() == View.VISIBLE){
                            hideControlView();
                            cancelTimer();
                        }else {
                            showControlView();
                            startTimer();
                        }
                    }else if (diff > 30){
                        forward();
                    }else if (diff < -30){
                        rewind();
                    }
                    hideMoreView();
                    break;
                default:
                    break;
            }
            return true;
        });

        moreView = view.findViewById(R.id.more_view);
        btP1 = view.findViewById(R.id.bt_p1);
        btP2 = view.findViewById(R.id.bt_p2);
        btP3 = view.findViewById(R.id.bt_p3);
        btP4 = view.findViewById(R.id.bt_p4);
        btP1.setOnClickListener(this);
        btP2.setOnClickListener(this);
        btP3.setOnClickListener(this);
        btP4.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(player != null) {
            player.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player != null) {
            player.stop();
            player.release();
        }
        cancelTimer();
        cancelStatusTimer();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        player.setDisplay(surfaceView);
        player.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    private void hideControlView() {
        handler.sendEmptyMessage(MSG_HIDE_CONTROL_VIEW);
    }

    private void showControlView() {
        handler.sendEmptyMessage(MSG_SHOW_CONTROL_VIEW);
    }

    private void showLoadingView() {
        handler.sendEmptyMessage(MSG_SHOW_LOADING_VIEW);
    }

    private void hideLoadingView() {
        handler.sendEmptyMessage(MSG_HIDE_LOADING_VIEW);
    }

    private void showMoreView() {
        handler.sendEmptyMessage(MSG_SHOW_MORE_VIEW);
    }

    private void hideMoreView() {
        handler.sendEmptyMessage(MSG_HIDE_MORE_VIEW);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() ==  R.id.tap_view) {
            if (controlTopView.getVisibility() == View.VISIBLE) {
                hideControlView();
                cancelTimer();
            } else {
                showControlView();
                startTimer();
            }
        }else if(v.getId() == R.id.ibt_more) {
            showMoreView();
            cancelTimer();
            hideControlView();
            hideLoadingView();
        }else if(v.getId() == R.id.bt_p1) {
            hideMoreView();
            if(engine != Player.ENGINE_NATIVE) {
                engine = Player.ENGINE_NATIVE;
                changeEngine(engine);
            }
        }else if(v.getId() == R.id.bt_p2) {
            hideMoreView();
            if(engine != Player.ENGINE_VLC) {
                engine = Player.ENGINE_VLC;
                changeEngine(engine);
            }
        }else if(v.getId() == R.id.bt_p3) {
            hideMoreView();
            if(engine != Player.ENGINE_IJK) {
                engine = Player.ENGINE_IJK;
                changeEngine(engine);
            }
        }else if(v.getId() == R.id.bt_p4) {
            hideMoreView();
            if(engine != Player.ENGINE_EXO) {
                engine = Player.ENGINE_EXO;
                changeEngine(engine);
            }
        }else if(v.getId() == R.id.ibt_play) {
            if(player != null && player.isPlaying()){
                player.pause();
                handler.sendEmptyMessage(MSG_SET_ICON_PLAY);
                cancelTimer();
            }else if(player != null && !player.isPlaying()){
                player.resume();
                handler.sendEmptyMessage(MSG_SET_ICON_PAUSE);
                startTimer();
            }
        }else if(v.getId() == R.id.ibt_full_screen) {
            startTimer();
        }
    }

    @Override
    public void onPlayerSizeChanged(int videoWidth, int videoHeight) {
        Log.d(Constant.c.TAG, "onPlayerSizeChanged");
        if(surfaceView != null && videoWidth > 0 && videoHeight > 0) {
            int width = surfaceView.getWidth();
            float rate = Float.parseFloat(videoHeight+"") / Float.parseFloat(videoWidth + "");
            float height = width * rate;
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, (int)height);
            layoutParams.gravity = Gravity.CENTER;
            surfaceView.setLayoutParams(layoutParams);
        }
        if(onPlayListener != null){
            onPlayListener.onPlayerSizeChanged(videoWidth, videoHeight);
        }
    }

    @Override
    public void onPlayerPrepared() {
        Log.d(Constant.c.TAG, "onPlayerPrepared");
        if(onPlayListener != null){
            onPlayListener.onPlayerPrepared();
        }
        if(currentPlayPosition > 0){
            player.seekTo(currentPlayPosition);
        }
    }

    @Override
    public void onPlayerPlaying() {
        Log.d(Constant.c.TAG, "onPlayerPlaying");
        if(onPlayListener != null){
            onPlayListener.onPlayerPlaying();
        }
        showControlView();
        hideLoadingView();
        handler.sendEmptyMessage(MSG_SET_ICON_PAUSE);
        startTimer();
        startStatusTimer();
    }

    @Override
    public void onPlayerBuffering() {
        Log.d(Constant.c.TAG, "onPlayerBuffering");
        if(onPlayListener != null){
            onPlayListener.onPlayerBuffering();
        }
    }

    @Override
    public void onPlayerPause() {
        Log.d(Constant.c.TAG, "onPlayerPause");
        if(onPlayListener != null){
            onPlayListener.onPlayerPause();
        }
    }

    @Override
    public void onPlayerPositionChanged() {
        Log.d(Constant.c.TAG, "onPlayerPositionChanged");
        if(onPlayListener != null){
            onPlayListener.onPlayerPositionChanged();
        }
    }

    @Override
    public void onPlayerCompleted() {
        Log.d(Constant.c.TAG, "onPlayerCompleted");
        if(onPlayListener != null){
            onPlayListener.onPlayerCompleted();
        }
        handler.sendEmptyMessage(MSG_PLAY_COMPLETED);
        currentPlayPosition = 0;
    }

    @Override
    public void onPlayerError() {
        Log.d(Constant.c.TAG, "onPlayerError");
        if(onPlayListener != null){
            onPlayListener.onPlayerError();
        }
        handler.sendEmptyMessage(MSG_PLAY_ERROR);
        currentPlayPosition = 0;
    }

    // 点击屏幕显示控制器后自动隐藏的timer
    public void startTimer(){
        cancelTimer();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                hideControlView();
            }
        }, 1000L * 15);
    }

    public void cancelTimer(){
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }

    // 每500毫秒更新一次播放信息的timer
    public void startStatusTimer(){
        cancelStatusTimer();
        statusTimer = new Timer();
        statusTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(player != null && player.isPlaying()) {
                    Message message = handler.obtainMessage(MSG_UPDATE_PLAY_INFO);
                    message.sendToTarget();
                }
            }
        }, 10L, 500L);
    }

    public void cancelStatusTimer(){
        if(statusTimer != null){
            statusTimer.cancel();
            statusTimer = null;
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
        if(tvTitle != null && !TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
    }

    public void setControlViewVisibilityWhenInit(boolean visibility){
        showControlViewWhenInit = visibility;
    }

    public void setButtonFullScreenVisibility(boolean visibility){
        if(ibtFullScreen != null) {
            ibtFullScreen.setVisibility(visibility ? View.VISIBLE : View.GONE);
        }else{
            showButtonFullScreen = visibility;
        }
    }

    public void setOnPlayListener(OnPlayListener onPlayListener) {
        this.onPlayListener = onPlayListener;
    }

    public void restart(){
        if(player != null){
            player.restart();
            currentPlayPosition = 0f;
            showControlView();
            showLoadingView();
        }
    }

    public void forward(){
        forward(15);
    }

    public void forward(int second){
        if(player != null && player.isPlaying()){
            player.forward(1000 * second);
        }
    }

    public void rewind(){
        rewind(15);
    }

    public void rewind(int second){
        if(player != null && player.isPlaying()){
            player.rewind(1000 * second);
        }
    }

    public void changeEngine(int engine){
        player.release();
        player = null;
        initPlayer(context, engine);
        player.setDisplay(surfaceView);
        player.start();
        showLoadingView();
    }
}
