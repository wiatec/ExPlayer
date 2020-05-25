package com.ex.libplayer.engine;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.ex.libplayer.Constant;
import com.ex.libplayer.enu.EnumPlayStatus;
import com.ex.libplayer.listener.OnPlayListener;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.video.VideoListener;

import java.util.HashMap;
import java.util.Map;

public class EngineExo implements Engine{

    private Context context;
    private SimpleExoPlayer player;
    private String url;
    private OnPlayListener onPlayListener;
    private Map<String, String> headers;
    private boolean isPlaying = false;
    private Player.EventListener eventListener;
    private Surface surface;

    @Override
    public void init(Context context) {
        this.context = context;
        headers = new HashMap<>();
        headers.put("User-Agent", Constant.header.USER_AGENT);
        player = ExoPlayerFactory.newSimpleInstance(context);
        player.setPlayWhenReady(true);
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setDisplay(Surface surface, TextureView textureView) {
        this.surface = surface;
        player.setVideoSurface(surface);
    }

    @Override
    public void setListener(OnPlayListener onPlayListener) {
        this.onPlayListener = onPlayListener;
        eventListener = new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == ExoPlayer.STATE_IDLE) {
                    isPlaying = false;
                } else if (playbackState == ExoPlayer.STATE_BUFFERING) {
                    isPlaying = false;
                    if (onPlayListener != null) {
                        onPlayListener.onPlayerStatusChanged(EnumPlayStatus.BUFFERING);
                    }
                } else if (playbackState == ExoPlayer.STATE_READY) {
                    isPlaying = playWhenReady;
                    if (onPlayListener != null && playWhenReady) {
                        onPlayListener.onPlayerStatusChanged(EnumPlayStatus.PLAYING);
                    }
                } else if (playbackState == ExoPlayer.STATE_ENDED) {
                    isPlaying = false;
                    if (onPlayListener != null) {
                        onPlayListener.onPlayerStatusChanged(EnumPlayStatus.COMPLETED);
                    }
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                isPlaying = false;
                if (onPlayListener != null) {
                    onPlayListener.onPlayerStatusChanged(EnumPlayStatus.ERROR);
                }
            }
        };
        player.addListener(eventListener);
        player.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                if (onPlayListener != null) {
                    onPlayListener.onPlayerSizeChanged(width, height);
                }
            }

            @Override
            public void onRenderedFirstFrame() {
                isPlaying = true;
                if (onPlayListener != null) {
                    onPlayListener.onPlayerStatusChanged(EnumPlayStatus.PREPARED);
                }
            }
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
        try {
            realPlay();
        }catch (Exception e){
            Log.e(Constant.c.TAG, e.toString());
        }
    }

    @Override
    public void restart() {
        if(player == null || TextUtils.isEmpty(url)) return;
        Log.d(Constant.c.TAG, "restart play url: " + url);
        if(onPlayListener != null){
            onPlayListener.onPlayerStatusChanged(EnumPlayStatus.PREPARING);
        }
        try {
            player.stop(true);
            player.clearVideoSurface();
            player.setVideoSurface(surface);
            realPlay();
        }catch (Exception e){
            Log.e(Constant.c.TAG, e.toString());
        }
    }

    public void realPlay(){
        DefaultHttpDataSourceFactory dataSourceFactory = new
                DefaultHttpDataSourceFactory(Constant.header.USER_AGENT, null, 60000, 60000, true);
        dataSourceFactory.getDefaultRequestProperties().set(headers);
        MediaSource videoSource = url.split("\\?")[0].endsWith("m3u8")
                || url.split("\\?")[0].endsWith("pls")?
                new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url)) :
                new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
        player.prepare(videoSource);
        player.setPlayWhenReady(true);
    }

    @Override
    public void resume() {
        if(player != null){
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public void pause() {
        if(player != null){
            player.setPlayWhenReady(false);
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
            if(eventListener != null) {
                player.removeListener(eventListener);
            }
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
            player.setVolume(volume);
        }
    }

    @Override
    public boolean isPlaying() {
        return player != null && isPlaying;
    }

}
