package com.ex.libplayer.enu;

public enum  EnumPlayStatus {

    // 播放器未开始
    IDLE(0x001, "idle"),
    // 播放器准备中
    PREPARING(0x002, "preparing"),
    // 播放器准备就绪
    PREPARED(0x003, "prepared"),
    // 播放器播放中
    PLAYING(0x004, "playing"),
    // 播放器暂停
    PAUSED(0x005, "paused"),
    // 播放中数据不足开始缓冲
    BUFFERING(0x006, "buffing"),
    // 播放完成
    COMPLETED(0x007, "completed"),
    // 播放器出错
    ERROR(-1, "error"),
    ;

    private int status;
    private String des;

    EnumPlayStatus(int status, String des) {
        this.status = status;
        this.des = des;
    }

    public int getStatus() {
        return status;
    }

    public String getDes() {
        return des;
    }

    @Override
    public String toString() {
        return "EnumPlayStatus{" +
                "status=" + status +
                ", des='" + des + '\'' +
                '}';
    }
}
