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
    private MediaPlayer player;
    private String url;
    private OnPlayListener onPlayListener;
    private Map<String, String> headers;

    @Override
    public void init(Context context) {
        headers = new HashMap<>();
        headers.put("User-Agent", Constant.header.USER_AGENT);
        libVLC = new LibVLC(context);
        player = new MediaPlayer(libVLC);
        player.setEventListener(this);
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setDisplay(SurfaceView surfaceView) {
        player.getVLCVout().setVideoView(surfaceView);
        player.getVLCVout().attachViews();
        player.getVLCVout().setWindowSize(surfaceView.getWidth(), surfaceView.getHeight());
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
        if(player == null) return;
        Log.d(Constant.c.TAG, "start play url: " + url);
        Media media = new Media(libVLC, Uri.parse(url));
        media.addOption("--network-caching=300");
        player.setMedia(media);
        player.play();
    }

    @Override
    public void restart() {
        if(player == null) return;
        Log.d(Constant.c.TAG, "restart play url: " + url);
        Media media = new Media(libVLC, Uri.parse(url));
        media.addOption("--network-caching=300");
        player.setMedia(media);
        player.play();
    }

    @Override
    public void resume() {
        if(player != null){
            player.play();
        }
    }

    @Override
    public void pause() {
        if(player != null){
            player.pause();
        }
    }

    @Override
    public void stop() {
        if(player != null){
            player.stop();
        }
    }

    @Override
    public void release() {
        if(player != null){
            player.release();
            player = null;
        }
    }

    @Override
    public void seekTo(float duration) {
        if(player != null && getTotalDuration() > 0){
            player.setTime((long) duration);
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
        if(player != null){
            return player.getTime();
        }
        return 0f;
    }

    @Override
    public float getPlayableDuration() {
        return getCurrentDuration();
    }

    @Override
    public float getTotalDuration() {
        if(player != null){
            return player.getLength();
        }
        return 0f;
    }

    @Override
    public void setVolume(float volume) {
        if(player != null){
            player.setVolume((int) volume);
        }
    }

    @Override
    public boolean isPlaying() {
        return player != null && player.isPlaying();
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
