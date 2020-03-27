package com.ex.libplayer.entities;

/**
 * 播放器播放状态和信息改变时会改变的数据
 */
public class ExPlayInfo {

    public static final int TYPE_PLAY_MODE = 0;
    public static final int TYPE_PLAY_STATUS = 1;
    public static final int TYPE_PROGRESS = 2;

    // 改变类型
    private int type;
    private int playMode;
    private int playStatus;
    private float currentTime;
    private float totalTime;

    public static ExPlayInfo valueOfMode(int playMode){
        ExPlayInfo info = new ExPlayInfo();
        info.setType(TYPE_PLAY_MODE);
        info.setPlayMode(playMode);
        return info;
    }

    public static ExPlayInfo valueOfStatus(int playStatus){
        ExPlayInfo info = new ExPlayInfo();
        info.setType(TYPE_PLAY_STATUS);
        info.setPlayStatus(playStatus);
        return info;
    }

    public static ExPlayInfo valueOfProgress(float currentTime, float totalTime){
        ExPlayInfo info = new ExPlayInfo();
        info.setType(TYPE_PROGRESS);
        info.setCurrentTime(currentTime);
        info.setTotalTime(totalTime);
        return info;

    }

    public static int getTypePlayMode() {
        return TYPE_PLAY_MODE;
    }

    public static int getTypePlayStatus() {
        return TYPE_PLAY_STATUS;
    }

    public static int getTypeProgress() {
        return TYPE_PROGRESS;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPlayMode() {
        return playMode;
    }

    public void setPlayMode(int playMode) {
        this.playMode = playMode;
    }

    public int getPlayStatus() {
        return playStatus;
    }

    public void setPlayStatus(int playStatus) {
        this.playStatus = playStatus;
    }

    public float getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(float currentTime) {
        this.currentTime = currentTime;
    }

    public float getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(float totalTime) {
        this.totalTime = totalTime;
    }

    @Override
    public String toString() {
        return "ExPlayInfo{" +
                "type=" + type +
                ", playMode=" + playMode +
                ", playStatus=" + playStatus +
                ", currentTime=" + currentTime +
                ", totalTime=" + totalTime +
                '}';
    }
}
