package com.ex.libplayer.engine;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceView;

import com.ex.libplayer.Constant;
import com.ex.libplayer.listener.OnPlayListener;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.HashMap;
import java.util.Map;

public class EngineVlc implements Engine, MediaPlayer.EventListener {

    private LibVLC libVLC;
    private MediaPlayer vlcPlayer;
    private String url;
    private OnPlayListener onPlayListener;
    private Map<String, String> headers;

    @Override
    public void init(Context context) {
        headers = new HashMap<>();
        headers.put("User-Agent", Constant.header.USER_AGENT);
        libVLC = new LibVLC(context);
        vlcPlayer = new MediaPlayer(libVLC);
        vlcPlayer.setEventListener(this);
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setDisplay(SurfaceView surfaceView) {
        vlcPlayer.getVLCVout().setVideoView(surfaceView);
        vlcPlayer.getVLCVout().attachViews();
        vlcPlayer.getVLCVout().setWindowSize(surfaceView.getWidth(), surfaceView.getHeight());
    }

    @Override
    public void setListener(OnPlayListener onPlayListener) {
        this.onPlayListener = onPlayListener;
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }


    @Override
    public void start() {
        if(vlcPlayer == null) return;
        Log.d(Constant.c.TAG, "start play url: " + url);
        Media media = new Media(libVLC, Uri.parse(url));
        media.addOption("--network-caching=300");
        vlcPlayer.setMedia(media);
        vlcPlayer.play();
    }

    @Override
    public void restart() {
        if(vlcPlayer == null) return;
        Log.d(Constant.c.TAG, "restart play url: " + url);
        Media media = new Media(libVLC, Uri.parse(url));
        media.addOption("--network-caching=300");
        vlcPlayer.setMedia(media);
        vlcPlayer.play();
    }

    @Override
    public void resume() {
        if(vlcPlayer != null){
            vlcPlayer.play();
        }
    }

    @Override
    public void pause() {
        if(vlcPlayer != null){
            vlcPlayer.pause();
        }
    }

    @Override
    public void stop() {
        if(vlcPlayer != null){
            vlcPlayer.stop();
        }
    }

    @Override
    public void release() {
        if(vlcPlayer != null){
            vlcPlayer.release();
        }
    }

    @Override
    public void seekTo(float duration) {
        if(vlcPlayer != null){
            vlcPlayer.setTime((long) duration);
        }
    }

    @Override
    public void rewind(float duration) {
        float currentDuration = getCurrentDuration();
        float targetDuration = currentDuration - duration;
        if(targetDuration < 0){
            targetDuration = 0;
        }
        seekTo(targetDuration);
    }

    @Override
    public void forward(float duration) {
        float currentDuration = getCurrentDuration();
        float targetDuration = currentDuration + duration;
        float totalDuration = getTotalDuration();
        if(targetDuration > totalDuration){
            targetDuration = totalDuration;
        }
        seekTo(targetDuration);
    }

    @Override
    public float getCurrentDuration() {
        if(vlcPlayer != null){
            return vlcPlayer.getTime();
        }
        return 0f;
    }

    @Override
    public float getPlayableDuration() {
        return getCurrentDuration();
    }

    @Override
    public float getTotalDuration() {
        if(vlcPlayer != null){
            return vlcPlayer.getLength();
        }
        return 0f;
    }

    @Override
    public void setVolume(float volume) {
        if(vlcPlayer != null){
            vlcPlayer.setVolume((int) volume);
        }
    }

    @Override
    public boolean isPlaying() {
        return vlcPlayer != null && vlcPlayer.isPlaying();
    }


    @Override
    public void onEvent(MediaPlayer.Event event) {
//        Log.d(Constant.c.TAG, event.type + "");
        switch (event.type){
            case MediaPlayer.Event.Playing:
                if(onPlayListener != null){
                    onPlayListener.onPlayerPrepared();
                    onPlayListener.onPlayerPlaying();
                }
                break;
            case MediaPlayer.Event.Buffering:
                if(onPlayListener != null){
                    onPlayListener.onPlayerBuffering();
                }
                break;
            case MediaPlayer.Event.Paused:
                if(onPlayListener != null){
                    onPlayListener.onPlayerPause();
                }
                break;
            case MediaPlayer.Event.Stopped:
                if(onPlayListener != null){
                    onPlayListener.onPlayerCompleted();
                }
                break;
            case MediaPlayer.Event.PositionChanged:
                if(onPlayListener != null){
                    onPlayListener.onPlayerPositionChanged();
                }
                break;
            case MediaPlayer.Event.EncounteredError:
                if(onPlayListener != null){
                    onPlayListener.onPlayerError();
                }
                break;
            default:
                break;
        }
    }
}
