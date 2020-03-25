package com.ex.libplayer.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
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

public class PlayFragment extends Fragment implements SurfaceHolder.Callback, OnPlayListener, View.OnClickListener {

    private static final int MSG_HIDE_CONTROL_VIEW = 0x001;
    private static final int MSG_SHOW_CONTROL_VIEW = 0x002;
    private static final int MSG_HIDE_LOADING_VIEW = 0x003;

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

    private Handler handler;
    private Timer timer;
    private Timer statusTimer;

    private Player player;
    private int engine;

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
        initPlayer();
        initHandler();
    }

    private void initPlayer(){
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
        tvTitle.setText(title);
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
                            handler.sendEmptyMessage(MSG_HIDE_CONTROL_VIEW);
                            cancelTimer();
                        }else {
                            handler.sendEmptyMessage(MSG_SHOW_CONTROL_VIEW);
                            startTimer();
                        }
                    }else if (diff > 30){
                        forward();
                    }else if (diff < -30){
                        rewind();
                    }
                    break;
                default:
                    break;
            }
            return true;
        });
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.d(Constant.c.TAG, "onResume");
//        if(player != null) {
//            player.resume();
//        }
//    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(Constant.c.TAG, "onPause");
        if(player != null) {
            player.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Constant.c.TAG, "onDestroy");
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

    private void hideControlView() {
        handler.sendEmptyMessage(MSG_HIDE_CONTROL_VIEW);
    }

    private void showControlView() {
        handler.sendEmptyMessage(MSG_SHOW_CONTROL_VIEW);
    }

    @Override
    public void onPlayerPrepared() {
        Log.d(Constant.c.TAG, "onPlayerPrepared");
        if(onPlayListener != null){
            onPlayListener.onPlayerPrepared();
        }
    }

    @Override
    public void onPlayerPlaying() {
        Log.d(Constant.c.TAG, "onPlayerPlaying");
        if(onPlayListener != null){
            onPlayListener.onPlayerPlaying();
        }
        handler.sendEmptyMessage(MSG_HIDE_LOADING_VIEW);
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
    }

    @Override
    public void onPlayerError() {
        Log.d(Constant.c.TAG, "onPlayerError");
        if(onPlayListener != null){
            onPlayListener.onPlayerError();
        }
        handler.sendEmptyMessage(MSG_PLAY_ERROR);
    }

    // 点击屏幕显示控制器后自动隐藏的timer
    public void startTimer(){
        cancelTimer();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = handler.obtainMessage(MSG_HIDE_CONTROL_VIEW);
                message.sendToTarget();
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
                Message message = handler.obtainMessage(MSG_UPDATE_PLAY_INFO);
                message.sendToTarget();
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
}
