package com.ex.libplayer.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class ExUtils {

    public static Activity findActivity(Context context) {
        if (context == null) return null;
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return findActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    private static AppCompatActivity getAppCompActivity(Context context) {
        if (context == null) return null;
        if (context instanceof AppCompatActivity) {
            return (AppCompatActivity) context;
        } else if (context instanceof ContextThemeWrapper) {
            return getAppCompActivity(((ContextThemeWrapper) context).getBaseContext());
        }
        return null;
    }

    public static void showActionBar(Context context) {
        ActionBar ab = getAppCompActivity(context).getSupportActionBar();
        if (ab != null) {
            ab.setShowHideAnimationEnabled(false);
            ab.show();
        }
        findActivity(context)
                .getWindow()
                .clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static void hideActionBar(Context context) {
        ActionBar ab = getAppCompActivity(context).getSupportActionBar();
        if (ab != null) {
            ab.setShowHideAnimationEnabled(false);
            ab.hide();
        }
        findActivity(context)
                .getWindow()
                .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);

    }

    public static String formatMediaTime(long millisSecond){
        int hour = (int)(millisSecond / 3600000L);
        int minute = (int)((millisSecond - (long)(hour * 3600000)) / 60000L);
        int second = (int)((millisSecond - (long)(hour * 3600000) - (long)(minute * 60000)) / 1000L);
        String sHour;
        if (hour < 10) {
            sHour = "0" + hour;
        } else {
            sHour = String.valueOf(hour);
        }

        String sMinute;
        if (minute < 10) {
            sMinute = "0" + minute;
        } else {
            sMinute = String.valueOf(minute);
        }

        String sSecond;
        if (second < 10) {
            sSecond = "0" + second;
        } else {
            sSecond = String.valueOf(second);
        }

        return sHour + ":" + sMinute + ":" + sSecond;
    }

}
