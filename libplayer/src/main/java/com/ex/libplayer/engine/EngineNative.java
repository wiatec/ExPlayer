package com.ex.libplayer.engine;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;


import com.ex.libplayer.Constant;
import com.ex.libplayer.enu.EnumPlayStatus;
import com.ex.libplayer.listener.OnPlayListener;
import com.ex.libplayer.player.Player;

import java.util.HashMap;
import java.util.Map;


public class EngineNative implements Engine{

    private Context context;
    private MediaPlayer player;
    private Surface surface;
    private String url;
    private OnPlayListener onPlayListener;
    private Map<String, String> headers;

    @Override
    public void init(Context context, boolean isLive) {
        this.context = context;
        headers = new HashMap<>();
        headers.put("User-Agent", Constant.header.USER_AGENT);
        player = new MediaPlayer();
        player.setScreenOnWhilePlaying(true);
        player.setLooping(true);
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setDisplay(Surface surface, TextureView textureView) {
        this.surface = surface;
        player.setSurface(surface);
    }

    @Override
    public void setListener(OnPlayListener onPlayListener) {
        this.onPlayListener = onPlayListener;
        player.setOnPreparedListener(iMediaPlayer -> {
            iMediaPlayer.start();
            if(onPlayListener != null){
                onPlayListener.onPlayerSizeChanged(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight());
                onPlayListener.onPlayerStatusChanged(EnumPlayStatus.PREPARED);

            }
        });
        player.setOnInfoListener((mp, what, extra) -> {
            if(onPlayListener != null){
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    onPlayListener.onPlayerStatusChanged(EnumPlayStatus.PLAYING);
                }
                if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START){
                    onPlayListener.onPlayerStatusChanged(EnumPlayStatus.BUFFERING);
                }
                if(what == MediaPlayer.MEDIA_INFO_BUFFERING_END){
                    onPlayListener.onPlayerStatusChanged(EnumPlayStatus.PLAYING);
                }
            }
            return true;
        });
        player.setOnSeekCompleteListener(MediaPlayer::start);
        player.setOnCompletionListener(mp -> {
            if(onPlayListener != null){
                onPlayListener.onPlayerStatusChanged(EnumPlayStatus.COMPLETED);
            }
        });
        player.setOnErrorListener((mp, what, extra) -> {
            if (onPlayListener != null) {
                onPlayListener.onPlayerStatusChanged(EnumPlayStatus.ERROR);
            }
            return true;
        });
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    @Override
    public void start() {
        if(player == null || TextUtils.isEmpty(url)) return;
        Log.d(Constant.c.TAG, "start play url: " + url);
        if(onPlayListener != null){
            onPlayListener.onPlayerStatusChanged(EnumPlayStatus.PREPARING);
        }
        new Thread(() -> {
            try {
                player.setDataSource(context, Uri.parse(url), headers);
                player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                player.prepareAsync();
            }catch (Exception e){
                Log.e(Constant.c.TAG, e.toString());
            }
        }).start();
    }

    @Override
    public void restart() {
        if(player == null || TextUtils.isEmpty(url)) return;
        Log.d(Constant.c.TAG, "restart play url: " + url);
        if(onPlayListener != null){
            onPlayListener.onPlayerStatusChanged(EnumPlayStatus.PREPARING);
        }
        new Thread(() -> {
            try {
                player.reset();
                player.setSurface(surface);
                player.setDataSource(context, Uri.parse(url), headers);
                player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                player.prepareAsync();
            }catch (Exception e){
                Log.e(Constant.c.TAG, e.toString());
            }
        }).start();
    }

    @Override
    public void resume() {
        if(player != null){
            player.start();
            if(onPlayListener != null){
                onPlayListener.onPlayerStatusChanged(EnumPlayStatus.PLAYING);
            }
        }
    }

    @Override
    public void pause() {
        if(player != null && player.isPlaying()){
            player.pause();
            if(onPlayListener != null){
                onPlayListener.onPlayerStatusChanged(EnumPlayStatus.PAUSED);
            }
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
            player.stop();
            player.release();
            player = null;
        }
    }

    @Override
    public void seekTo(float duration) {
        if(player != null && getTotalDuration() > 0){
            player.seekTo((int)duration);
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
            return player.getCurrentPosition();
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
            return player.getDuration();
        }
        return 0f;
    }

    @Override
    public void setVolume(float volume) {
        if(player != null){
            player.setVolume(volume, volume);
        }
    }

    @Override
    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

}
