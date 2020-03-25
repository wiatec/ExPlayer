package com.ex.libplayer.util;

public class TimeUtils {

    public static String formatMediaTime(long millisSecond){
        int hour = (int)(millisSecond / 3600000L);
        int minute = (int)((millisSecond - (long)(hour * 3600000)) / 60000L);
        int second = (int)((millisSecond - (long)(hour * 3600000) - (long)(minute * '\uea60')) / 1000L);
        String sHour;
        if (hour < 10) {
            sHour = "0" + String.valueOf(hour);
        } else {
            sHour = String.valueOf(hour);
        }

        String sMinute;
        if (minute < 10) {
            sMinute = "0" + String.valueOf(minute);
        } else {
            sMinute = String.valueOf(minute);
        }

        String sSecond;
        if (second < 10) {
            sSecond = "0" + String.valueOf(second);
        } else {
            sSecond = String.valueOf(second);
        }

        return sHour + ":" + sMinute + ":" + sSecond;
    }
}
