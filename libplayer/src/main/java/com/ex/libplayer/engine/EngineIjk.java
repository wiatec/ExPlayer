package com.ex.libplayer.engine;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceView;

import com.ex.libplayer.Constant;
import com.ex.libplayer.listener.OnPlayListener;

import java.util.HashMap;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class EngineIjk implements Engine{

    private Context context;
    private IjkMediaPlayer player;
    private SurfaceView surfaceView;
    private String url;
    private OnPlayListener onPlayListener;
    private Map<String, String> headers;

    @Override
    public void init(Context context) {
        this.context = context;
        headers = new HashMap<>();
        headers.put("User-Agent", Constant.header.USER_AGENT);
        player = new IjkMediaPlayer();
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "crypto,file,http,https,tcp,tls,udp");
        player.setScreenOnWhilePlaying(true);
        player.setLooping(true);
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setDisplay(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        player.setDisplay(surfaceView.getHolder());
    }

    @Override
    public void setListener(OnPlayListener onPlayListener) {
        this.onPlayListener = onPlayListener;
        player.setOnPreparedListener(iMediaPlayer -> {
            iMediaPlayer.start();
            if(onPlayListener != null){
                onPlayListener.onPlayerSizeChanged(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight());
                onPlayListener.onPlayerPrepared();
                onPlayListener.onPlayerPlaying();

            }
        });
        player.setOnInfoListener((mp, what, extra) -> {
            if(onPlayListener != null && what == MediaPlayer.MEDIA_INFO_BUFFERING_START){
                onPlayListener.onPlayerBuffering();
            }
            if(onPlayListener != null && what == MediaPlayer.MEDIA_INFO_BUFFERING_END){
                onPlayListener.onPlayerPlaying();
            }
            return true;
        });
        player.setOnSeekCompleteListener(mp -> {
            mp.start();
            if(onPlayListener != null){
                onPlayListener.onPlayerPositionChanged();
            }
        });
        player.setOnCompletionListener(mp -> {
            if(onPlayListener != null){
                onPlayListener.onPlayerCompleted();
            }
        });
        player.setOnErrorListener((mp, what, extra) -> {
            if(what == MediaPlayer.MEDIA_ERROR_TIMED_OUT ||
                    what == MediaPlayer.MEDIA_ERROR_UNKNOWN ||
                    what == MediaPlayer.MEDIA_ERROR_IO ||
                    what == MediaPlayer.MEDIA_ERROR_MALFORMED ||
                    what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ||
                    what == MediaPlayer.MEDIA_ERROR_SERVER_DIED ||
                    what == MediaPlayer.MEDIA_ERROR_UNSUPPORTED) {
                if (onPlayListener != null) {
                    onPlayListener.onPlayerError();
                }
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
        if(player == null) return;
        Log.d(Constant.c.TAG, "start play url: " + url);
        try {
            player.setDataSource(context, Uri.parse(url), headers);
            player.prepareAsync();
        }catch (Exception e){
            Log.e(Constant.c.TAG, e.toString());
        }
    }

    @Override
    public void restart() {
        if(player == null) return;
        Log.d(Constant.c.TAG, "restart play url: " + url);
        try {
            player.reset();
            player.setDisplay(surfaceView.getHolder());
            player.setDataSource(context, Uri.parse(url), headers);
            player.prepareAsync();
        }catch (Exception e){
            Log.e(Constant.c.TAG, e.toString());
        }
    }

    @Override
    public void resume() {
        if(player != null){
            player.start();
        }
    }

    @Override
    public void pause() {
        if(player != null && player.isPlaying()){
            player.pause();
            if(onPlayListener != null){
                onPlayListener.onPlayerPause();
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
